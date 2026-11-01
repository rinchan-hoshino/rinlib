package dev.rinchan.rinlib.minecraft;

import java.util.function.Supplier;
import net.minecraft.server.MinecraftServer;

/** @deprecated Import {@code dev.rinchan.rinlib.api.FunctionExecution} instead. */
@Deprecated
public final class FunctionExecution {
    private FunctionExecution() {}

    public static <T> T isolated(MinecraftServer server, Supplier<T> action) {
        return dev.rinchan.rinlib.api.FunctionExecution.isolated(server, action);
    }
}
