package dev.rinchan.rinlib.minecraft;

import dev.rinchan.rinlib.mixin.command.CommandContextAccessor;
import java.util.function.Supplier;
import net.minecraft.server.MinecraftServer;

/** Runs function callbacks to completion, including calls made from another function. */
public final class FunctionExecution {
    private FunctionExecution() {}

    public static <T> T isolated(MinecraftServer server, Supplier<T> action) {
        if (!server.isSameThread()) {
            throw new IllegalStateException("Function callbacks must run on the server thread");
        }
        var contexts = CommandContextAccessor.rinlib$currentContext();
        var previous = contexts.get();
        contexts.remove();
        try {
            return action.get();
        } finally {
            if (previous == null) contexts.remove();
            else contexts.set(previous);
        }
    }
}
