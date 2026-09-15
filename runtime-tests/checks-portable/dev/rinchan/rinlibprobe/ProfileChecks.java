package dev.rinchan.rinlibprobe;
import dev.rinchan.rinlib.minecraft.KeepInventoryProjection;
import dev.rinchan.rinlib.payment.DenominatedPayment;
import net.minecraft.server.MinecraftServer;
import java.util.List;
import static dev.rinchan.rinlibprobe.RuntimeProbe.require;
public final class ProfileChecks {
    public static void run(MinecraftServer server) {
        var plan = DenominatedPayment.plan(10, List.of(new DenominatedPayment.Stack(0, 2, 9))).orElseThrow();
        require(plan.changeUnits() == 8 && plan.removals().get(0).itemCount() == 2, "payment conserves value");
        require(!KeepInventoryProjection.isActive(), "projection initially inactive");
        try (var scope = KeepInventoryProjection.enter()) {
            require(KeepInventoryProjection.isActive(), "projection active in scope");
        }
        require(!KeepInventoryProjection.isActive(), "projection clears on close");

    }
}
