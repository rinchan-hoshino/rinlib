package dev.rinchan.rinlib.minecraft;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;

final class StructurePlatform {
    private StructurePlatform() { }
    static String directory() { return "structure"; }
    static int dataVersion() { return SharedConstants.getCurrentVersion().dataVersion().version(); }
    static String minecraftVersion() { return SharedConstants.getCurrentVersion().name(); }
    static CompoundTag readCompressed(byte[] bytes, long budget) throws IOException {
        return NbtIo.readCompressed(new ByteArrayInputStream(bytes), NbtAccounter.create(budget));
    }
    static PathPackResources pack(Path root, String id) {
        return new PathPackResources(new PackLocationInfo(id, Component.literal(id), PackSource.BUILT_IN, Optional.empty()), root);
    }
    static String packMetadata() {
        var format = SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA);
        String value = "[" + format.major() + "," + format.minor() + "]";
        return "{\"pack\":{\"description\":\"Prepared structure resources\",\"min_format\":" + value + ",\"max_format\":" + value + "}}\n";
    }
}
