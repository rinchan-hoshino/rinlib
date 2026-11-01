package dev.rinchan.rinlib.api;

import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackType;

/** Current server-data pack format, including the minor format introduced by newer Minecraft. */
public final class PackFormats {
    private PackFormats() {}

    public static int serverDataMajor() {
        return SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).major();
    }

    public static int serverDataMinor() {
        return SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).minor();
    }

    public static String serverDataMetadata(String description) {
        var pack = new JsonObject();
        pack.addProperty("description", description);
        var format = new com.google.gson.JsonArray();
        format.add(serverDataMajor());
        format.add(serverDataMinor());
        pack.add("min_format", format);
        pack.add("max_format", format.deepCopy());
        var root = new JsonObject();
        root.add("pack", pack);
        return root.toString() + "\n";
    }
}
