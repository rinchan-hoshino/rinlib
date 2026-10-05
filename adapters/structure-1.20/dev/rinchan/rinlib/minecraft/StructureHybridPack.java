package dev.rinchan.rinlib.minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;

final class StructureHybridPack extends StructurePreparedPack {
    StructureHybridPack(PackResources disk, Map<String, Resource> delegated) { super(disk, delegated); }
    @Override public IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
        var fallback = delegated(type, id.toString());
        return fallback != null ? fallback : disk.getResource(type, id);
    }
    @Override public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        disk.listResources(type, namespace, path, (id, resource) -> {
            if (delegated(type, id.toString()) == null) output.accept(id, resource);
        });
        listDelegated(type, namespace, path, (key, resource) -> output.accept(new ResourceLocation(key), resource));
    }
    @Override public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) throws IOException {
        return disk.getMetadataSection(serializer);
    }
    @Override public String packId() { return disk.packId(); }
    @Override public boolean isBuiltin() { return disk.isBuiltin(); }
}
