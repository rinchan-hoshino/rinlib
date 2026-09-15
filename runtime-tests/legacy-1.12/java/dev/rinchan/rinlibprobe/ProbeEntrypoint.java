package dev.rinchan.rinlibprobe;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
@Mod(modid="rinlib_probe", name="RinLib runtime probe", version="1.0.0")
public final class ProbeEntrypoint {
 @Mod.EventHandler
 public void started(FMLServerStartedEvent event) {
  new RuntimeProbe().run(FMLCommonHandler.instance().getMinecraftServerInstance());
 }
}
