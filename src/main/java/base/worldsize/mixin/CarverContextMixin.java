//package base.worldsize.mixin;
//
//import base.worldsize.TorusChunkGenerator;
//import base.worldsize.WorldSize;
//import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
///**
// * Wraps block coordinates during cave/ravine/underwater carver evaluation.
// */
//@Mixin(CarverContext.class)
//public abstract class CarverContextMixin {
//
//    @Inject(method = "getBlockX", at = @At("RETURN"), cancellable = true)
//    private void wrapBlockX(CallbackInfoReturnable<Integer> cir) {
//        if (TorusChunkGenerator.isTorusActive()) {
//            cir.setReturnValue(WorldSize.wrapBlock(cir.getReturnValue()));
//        }
//    }
//
//    @Inject(method = "getBlockZ", at = @At("RETURN"), cancellable = true)
//    private void wrapBlockZ(CallbackInfoReturnable<Integer> cir) {
//        if (TorusChunkGenerator.isTorusActive()) {
//            cir.setReturnValue(WorldSize.wrapBlock(cir.getReturnValue()));
//        }
//    }
//}
