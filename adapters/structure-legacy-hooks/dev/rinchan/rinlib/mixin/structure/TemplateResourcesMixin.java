package dev.rinchan.rinlib.mixin.structure;

import com.mojang.datafixers.DataFixer;
import dev.rinchan.rinlib.minecraft.StructureLifecycle;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureTemplateManager.class)
abstract class TemplateResourcesMixin {
    @Shadow private ResourceManager resourceManager;
    @Shadow @Final private DataFixer fixerUpper;
    @Unique private StructureLifecycle.Preparation rinlib$preparation;

    @Inject(method = {"<init>", "onResourceManagerReload"}, at = @At("RETURN"))
    private void rinlib$prepareStructures(CallbackInfo callback) {
        if (rinlib$preparation == null) rinlib$preparation = StructureLifecycle.create();
        if (rinlib$preparation != null) resourceManager = rinlib$preparation.prepare(resourceManager, fixerUpper);
    }
}
