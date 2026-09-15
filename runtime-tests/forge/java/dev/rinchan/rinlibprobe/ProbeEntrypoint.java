package dev.rinchan.rinlibprobe;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
@Mod("rinlib_probe")
public final class ProbeEntrypoint {
 public ProbeEntrypoint() { MinecraftForge.EVENT_BUS.addListener(this::started); }
 private void started(FMLServerStartedEvent event) { new RuntimeProbe().run(event.getServer()); }
}
