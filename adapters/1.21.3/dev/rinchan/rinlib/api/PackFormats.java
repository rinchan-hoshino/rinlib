package dev.rinchan.rinlib.api;

import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackType;

/** Current server-data pack format, including the minor format introduced by newer Minecraft. */
public final class PackFormats {
    private PackFormats() {}

    public static int serverDataMajor() {
        return SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA);
    }

    public static int serverDataMinor() {
        return 0;
    }

    public static String serverDataMetadata(String description) {
        var pack = new JsonObject();
        pack.addProperty("description", description);
        pack.addProperty("pack_format", serverDataMajor());
        var root = new JsonObject();
        root.add("pack", pack);
        return root.toString() + "\n";
    }
}
