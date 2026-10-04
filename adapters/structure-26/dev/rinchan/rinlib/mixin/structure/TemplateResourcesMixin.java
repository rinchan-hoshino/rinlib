package dev.rinchan.rinlib.mixin.structure;

import com.mojang.datafixers.DataFixer;
import dev.rinchan.rinlib.minecraft.StructureLifecycle;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.loader.ResourceManagerTemplateSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResourceManagerTemplateSource.class)
abstract class TemplateResourcesMixin {
    @Shadow private ResourceManager resourceManager;
    @Unique private DataFixer rinlib$fixer;
    @Unique private StructureLifecycle.Preparation rinlib$preparation;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void rinlib$prepareInitial(DataFixer fixer, HolderGetter<Block> blocks, ResourceManager resources,
                                       FileToIdConverter converter, CallbackInfo callback) {
        rinlib$fixer = fixer;
        rinlib$preparation = StructureLifecycle.create();
        rinlib$prepare();
    }

    @Inject(method = "setResourceManager", at = @At("RETURN"))
    private void rinlib$prepareReload(ResourceManager resources, CallbackInfo callback) {
        rinlib$prepare();
    }

    @Unique private void rinlib$prepare() {
        if (rinlib$preparation != null) resourceManager = rinlib$preparation.prepare(resourceManager, rinlib$fixer);
    }
}
