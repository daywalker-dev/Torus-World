package base.worldsize.mixin;

import base.worldsize.TorusChunkGenerator;
import base.worldsize.WorldSize;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Wraps feature placement origins (trees, ores, lakes, etc.) to match torus coordinates.
 */
@Mixin(FeaturePlaceContext.class)
public abstract class FeaturePlaceContextMixin {

    @Inject(method = "origin", at = @At("RETURN"), cancellable = true)
    private void wrapOrigin(CallbackInfoReturnable<BlockPos> cir) {
        if (TorusChunkGenerator.isTorusActive()) {
            BlockPos pos = cir.getReturnValue();
            int wrappedX = WorldSize.wrapBlock(pos.getX());
            int wrappedZ = WorldSize.wrapBlock(pos.getZ());
            if (wrappedX != pos.getX() || wrappedZ != pos.getZ()) {
                cir.setReturnValue(new BlockPos(wrappedX, pos.getY(), wrappedZ));
            }
        }
    }
}