package dev.rinchan.rinlib.minecraft;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/** Stable structure resource operations across Minecraft releases. */
public final class StructureResources {
    private StructureResources() { }

    public static Map<String, Resource> list(ResourceManager resources) {
        Map<String, Resource> result = new TreeMap<>();
        resources.listResources(directory(), key -> key.getPath().endsWith(".nbt"))
            .forEach((key, resource) -> result.put(key.toString(), resource));
        return result;
    }

    public static String directory() { return StructurePlatform.directory(); }
    public static int dataVersion() { return StructurePlatform.dataVersion(); }
    public static String minecraftVersion() { return StructurePlatform.minecraftVersion(); }
    public static String packMetadata() { return StructurePlatform.packMetadata(); }

    public static CompoundTag readCompressed(byte[] bytes, long maximumHeapBytes) throws IOException {
        if (maximumHeapBytes < 1) throw new IllegalArgumentException("NBT memory budget must be positive");
        return StructurePlatform.readCompressed(bytes, maximumHeapBytes);
    }

    public static MultiPackResourceManager overlay(Path root, String id) {
        return new MultiPackResourceManager(PackType.SERVER_DATA, List.of(pack(root, id)));
    }

    public static MultiPackResourceManager overlay(Path root, String id, Map<String, Resource> delegated) {
        PackResources disk = pack(root, id);
        PackResources prepared = delegated.isEmpty() ? disk : new StructureHybridPack(disk, delegated);
        return new MultiPackResourceManager(PackType.SERVER_DATA, List.of(prepared));
    }

    public static PackResources pack(Path root, String id) { return StructurePlatform.pack(root, id); }

    public static Path resourcePath(Path root, String key) {
        int separator = key.indexOf(':');
        if (separator < 1 || separator == key.length() - 1) {
            throw new IllegalArgumentException("Invalid resource key: " + key);
        }
        Path data = root.resolve("data").normalize();
        Path path = data.resolve(key.substring(0, separator)).resolve(key.substring(separator + 1)).normalize();
        if (!path.startsWith(data)) throw new IllegalArgumentException("Resource escaped pack root: " + key);
        return path;
    }
}
