package dev.rinchan.rinlib.minecraft;

import com.mojang.datafixers.DataFixer;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.IdentityHashMap;
import java.util.function.Supplier;
import net.minecraft.server.packs.resources.ResourceManager;

/** A preparation belongs to one vanilla structure resource source. */
public final class StructureLifecycle {
    private static Supplier<Preparation> factory;
    private static final Set<Preparation> ACTIVE = Collections.newSetFromMap(new IdentityHashMap<>());

    private StructureLifecycle() { }

    public static synchronized void register(Supplier<Preparation> preparationFactory) {
        if (factory != null) throw new IllegalStateException("Structure preparation is already registered");
        factory = java.util.Objects.requireNonNull(preparationFactory);
    }

    public static synchronized Preparation create() {
        if (factory == null) return null;
        Preparation preparation = java.util.Objects.requireNonNull(factory.get());
        ACTIVE.add(preparation);
        return preparation;
    }

    public static void closeAll() {
        List<Preparation> preparations;
        synchronized (StructureLifecycle.class) {
            preparations = List.copyOf(ACTIVE);
            ACTIVE.clear();
        }
        for (Preparation preparation : preparations) preparation.close();
    }

    /** Called once after a player has joined; ordinary players receive no messages. */
    public static void onOperatorLogin(boolean operator, java.util.function.Consumer<net.minecraft.network.chat.Component> send) {
        if (!operator) return;
        List<Preparation> preparations;
        synchronized (StructureLifecycle.class) { preparations = List.copyOf(ACTIVE); }
        var notices = new java.util.LinkedHashSet<net.minecraft.network.chat.Component>();
        for (Preparation preparation : preparations) {
            var notice = preparation.operatorNotice();
            if (notice != null) notices.add(notice);
        }
        if (notices.isEmpty()) return;
        var combined = net.minecraft.network.chat.Component.empty();
        boolean first = true;
        for (var notice : notices) {
            if (!first) combined.append("\n");
            combined.append(notice);
            first = false;
        }
        send.accept(combined);
    }

    public interface Preparation extends AutoCloseable {
        ResourceManager prepare(ResourceManager resources, DataFixer fixer);
        @Override void close();
        default net.minecraft.network.chat.Component operatorNotice() { return null; }
    }
}
