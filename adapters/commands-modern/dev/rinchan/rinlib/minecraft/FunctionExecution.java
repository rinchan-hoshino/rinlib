package dev.rinchan.rinlib.minecraft;

import java.util.function.Supplier;
import net.minecraft.server.MinecraftServer;

/** @deprecated 改用 dev.rinchan.rinlib.api.FunctionExecution */
@Deprecated(forRemoval = true)
public final class FunctionExecution {
    private FunctionExecution() {}

    public static <T> T isolated(MinecraftServer server, Supplier<T> action) {
        return dev.rinchan.rinlib.api.FunctionExecution.isolated(server, action);
    }
}
