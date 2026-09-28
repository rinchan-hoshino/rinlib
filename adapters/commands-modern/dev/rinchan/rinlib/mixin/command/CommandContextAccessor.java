package dev.rinchan.rinlib.mixin.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.execution.ExecutionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Commands.class)
public interface CommandContextAccessor {
    @Accessor("CURRENT_EXECUTION_CONTEXT")
    static ThreadLocal<ExecutionContext<CommandSourceStack>> rinlib$currentContext() {
        throw new AssertionError("Mixin accessor was not applied");
    }
}
