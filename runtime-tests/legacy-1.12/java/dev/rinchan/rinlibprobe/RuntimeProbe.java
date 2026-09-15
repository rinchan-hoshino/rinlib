package dev.rinchan.rinlibprobe;

import dev.rinchan.rinlib.state.SharedOwnerState;
import dev.rinchan.rinlib.state.StickyBoolean;
import net.minecraft.server.MinecraftServer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RuntimeProbe {
    static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
        System.out.println("RINLIB_ASSERT_PASS " + message);
    }

    public void run(MinecraftServer server) {
        server.addScheduledTask(() -> {
            try {
                Map<UUID, String> values = new HashMap<>();
                UUID actor = new UUID(0, 100);
                values.put(actor, "actor");
                require(SharedOwnerState.OWNER.equals(new UUID(0, 1)), "stable shared owner UUID");
                require(SharedOwnerState.put(values, "shared").equals("shared"), "put returns inserted value");
                require(SharedOwnerState.contains(values) && SharedOwnerState.get(values).equals("shared"), "shared map read");
                require(SharedOwnerState.remove(values).equals("shared") && !SharedOwnerState.contains(values), "shared map removal");
                require(values.size() == 1 && values.get(actor).equals("actor"), "actor entries stay independent");
                require(StickyBoolean.next(true, false) && StickyBoolean.next(false, true) && !StickyBoolean.next(false, false), "sticky state truth table");
                Files.write(Paths.get("rinlib-runtime.pass"), "pass\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                System.out.println("RINLIB_RUNTIME_PASS");
            } catch (Throwable error) {
                error.printStackTrace();
                System.out.println("RINLIB_RUNTIME_FAIL");
            } finally {
                server.initiateShutdown();
            }
        });
    }
}
