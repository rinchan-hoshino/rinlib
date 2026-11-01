package dev.rinchan.rinlibprobe;

import dev.rinchan.rinlib.api.FunctionExecution;
import dev.rinchan.rinlib.api.MinecraftData;
import dev.rinchan.rinlib.api.MobEffectState;
import dev.rinchan.rinlib.api.PlayerContext;
import dev.rinchan.rinlib.mixin.command.CommandContextAccessor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static dev.rinchan.rinlibprobe.RuntimeProbe.require;

final class ApiChecks {
    static void run(MinecraftServer server, ServerPlayer player) {
        require(PlayerContext.name(player).equals("RinLibProbe"), "stable API reads the player name");
        var source = PlayerContext.functionSource(player);
        require(source.hasPermission(2) && !source.hasPermission(3), "function source has exactly level two");
        require(!MobEffectState.add(player, "INVALID ID", 10, 0), "stable effect API rejects malformed IDs");
        require(!MobEffectState.add(player, "example:missing", 10, 0), "stable effect API rejects absent effects");
        require(MobEffectState.add(player, "minecraft:strength", 60, 2), "stable effect API resolves a real effect");

        var data = MinecraftData.of(player.registryAccess());
        var stack = new ItemStack(Items.STONE, 3);
        var restored = data.loadItem(data.saveItem(stack)).orElseThrow();
        require(restored.is(Items.STONE) && restored.getCount() == 3, "stable item codec round trips through registries");

        var contexts = CommandContextAccessor.rinlib$currentContext();
        var previous = contexts.get();
        try (var sentinel = new ExecutionContext<CommandSourceStack>(1000, 1000, InactiveProfiler.INSTANCE)) {
            contexts.set(sentinel);
            int value = FunctionExecution.isolated(server, () -> {
                require(contexts.get() == null, "callback runs independently of the enclosing function context");
                return 41;
            });
            require(value == 41 && contexts.get() == sentinel, "callback result and enclosing context are preserved");
            var expected = new IllegalStateException("fixture failure");
            try {
                FunctionExecution.isolated(server, () -> { throw expected; });
                throw new AssertionError("callback failure was swallowed");
            } catch (IllegalStateException actual) {
                require(actual == expected && contexts.get() == sentinel, "throwing callbacks restore the exact previous context");
            }
            var outputId = ResourceLocation.parse("rinlib_probe:callback");
            server.getCommandStorage().set(outputId, new CompoundTag());
            require(FunctionExecution.executeTag(player, "rinlib_probe:callbacks") == 2,
                    "string-ID function tag executes every callback synchronously");
            require(contexts.get() == sentinel, "function tag restores an enclosing context");
            var output = server.getCommandStorage().get(outputId);
            require(output.getInt("first") == 11 && output.getInt("second") == 22,
                    "both function outputs are visible before the API returns");
            require(FunctionExecution.executeTag(player, "rinlib_probe:missing") == 0,
                    "a missing function tag is empty");
        } finally {
            if (previous == null) contexts.remove();
            else contexts.set(previous);
        }
        try {
            CompletableFuture.runAsync(() -> FunctionExecution.isolated(server, () -> null)).join();
            throw new AssertionError("off-thread callback was accepted");
        } catch (CompletionException error) {
            require(error.getCause() instanceof IllegalStateException, "off-thread function callbacks are rejected");
        }
    }
}
