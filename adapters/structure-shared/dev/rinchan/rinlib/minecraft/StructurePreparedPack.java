package dev.rinchan.rinlib.minecraft;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;

/** One already-resolved structure layer; delegated resources retain vanilla ownership. */
abstract class StructurePreparedPack implements PackResources {
    protected final PackResources disk;
    private final Map<String, Resource> delegated;

    StructurePreparedPack(PackResources disk, Map<String, Resource> delegated) {
        this.disk = disk;
        this.delegated = Map.copyOf(delegated);
    }

    protected IoSupplier<InputStream> delegated(PackType type, String key) {
        Resource resource = type == PackType.SERVER_DATA ? delegated.get(key) : null;
        return resource == null ? null : resource::open;
    }

    protected void listDelegated(PackType type, String namespace, String path,
                                 BiConsumer<String, IoSupplier<InputStream>> output) {
        if (type != PackType.SERVER_DATA) return;
        String prefix = namespace + ':';
        delegated.forEach((key, resource) -> {
            if (!key.startsWith(prefix)) return;
            String resourcePath = key.substring(prefix.length());
            if (path.isEmpty() || resourcePath.equals(path) || resourcePath.startsWith(path + '/')) {
                output.accept(key, resource::open);
            }
        });
    }

    @Override public Set<String> getNamespaces(PackType type) {
        Set<String> namespaces = new HashSet<>(disk.getNamespaces(type));
        if (type == PackType.SERVER_DATA) {
            delegated.keySet().forEach(key -> namespaces.add(key.substring(0, key.indexOf(':'))));
        }
        return Set.copyOf(namespaces);
    }

    @Override public IoSupplier<InputStream> getRootResource(String... path) { return disk.getRootResource(path); }
    @Override public void close() { disk.close(); }
}
