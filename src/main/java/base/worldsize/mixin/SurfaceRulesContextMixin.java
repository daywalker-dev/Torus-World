package base.worldsize.mixin;

import base.worldsize.TorusChunkGenerator;
import base.worldsize.WorldSize;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wraps SurfaceRules.Context X/Z coordinates for torus worlds.
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$Context")
public abstract class SurfaceRulesContextMixin {

    // Shadow the protected fields
    @Shadow
    protected int blockX;

    @Shadow
    protected int blockZ;

    /**
     * Wrap coordinates whenever updateXZ is called
     */
    @Inject(method = "updateXZ", at = @At("HEAD"))
    private void wrapUpdateXZ(int x, int z, CallbackInfo ci) {
        if (TorusChunkGenerator.isTorusActive()) {
            int quartSize = WorldSize.WORLD_SIZE_BLOCKS >> 2;
            this.blockX = Math.floorMod(x, quartSize);
            this.blockZ = Math.floorMod(z, quartSize);
        }
    }
}











//
//
//package base.worldsize.mixin;
//
//import base.worldsize.TorusChunkGenerator;
//import base.worldsize.WorldSize;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
///**
// * Wraps block coordinates during surface rule evaluation.
// * Uses fully-qualified target for protected inner class.
// */
//@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$Context")
//public abstract class SurfaceRulesContextMixin {
//
//    @Inject(method = "blockX", at = @At("RETURN"), cancellable = true)
//    private void wrapBlockX(CallbackInfoReturnable<Integer> cir) {
//        if (TorusChunkGenerator.isTorusActive()) {
//            cir.setReturnValue(WorldSize.wrapBlock(cir.getReturnValue()));
//        }
//    }
//
//    @Inject(method = "blockZ", at = @At("RETURN"), cancellable = true)
//    private void wrapBlockZ(CallbackInfoReturnable<Integer> cir) {
//        if (TorusChunkGenerator.isTorusActive()) {
//            cir.setReturnValue(WorldSize.wrapBlock(cir.getReturnValue()));
//        }
//    }
//}
