package dev.rinchan.rinlib.minecraft;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;

final class StructurePlatform {
    private StructurePlatform() { }
    static String directory() { return "structures"; }
    static int dataVersion() { return SharedConstants.getCurrentVersion().getDataVersion().getVersion(); }
    static String minecraftVersion() { return SharedConstants.getCurrentVersion().getName(); }
    static CompoundTag readCompressed(byte[] bytes, long budget) throws IOException {
        try (var input = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)))) {
            return NbtIo.read(input, new NbtAccounter(budget));
        }
    }
    static PathPackResources pack(Path root, String id) {
        return new PathPackResources(id, root, false);
    }
    static String packMetadata() {
        return "{\"pack\":{\"description\":\"Prepared structure resources\",\"pack_format\":"
            + SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA) + "}}\n";
    }
}
