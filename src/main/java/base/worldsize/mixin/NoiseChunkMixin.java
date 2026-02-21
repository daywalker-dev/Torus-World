package base.worldsize.mixin;

import base.worldsize.TorusChunkGenerator;
import base.worldsize.WorldSize;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * THIS IS THE CRITICAL MIXIN that makes terrain wrapping actually work.
 *
 * NoiseChunk implements DensityFunction.FunctionContext, which provides blockX(), blockY(), blockZ()
 * to ALL density functions during terrain generation. By wrapping blockX() and blockZ() here,
 * every noise function, every density calculation will see wrapped coordinates.
 *
 * WHY THE PREVIOUS VERSION FAILED:
 * The old code checked a ThreadLocal<Boolean> flag. But NoiseBasedChunkGenerator.fillFromNoise()
 * runs its work on background threads via CompletableFuture.supplyAsync(). ThreadLocal values
 * do NOT propagate to worker threads — so blockX()/blockZ() on the worker thread always saw
 * the flag as false and never wrapped.
 *
 * THE FIX:
 * TorusChunkGenerator now uses a static volatile boolean (not ThreadLocal). Once a torus world
 * is created, the flag is true globally on ALL threads. This mixin's check now works correctly
 * on the async worker threads where noise computation actually happens.
 *
 * In Mojang mappings (1.20.4 and 1.21.11):
 *   NoiseChunk.blockX() returns this.cellStartBlockX + this.inCellX
 *   NoiseChunk.blockZ() returns this.cellStartBlockZ + this.inCellZ
 */
@Mixin(NoiseChunk.class)
public abstract class NoiseChunkMixin {

    @Inject(method = "blockX", at = @At("RETURN"), cancellable = true)
    private void wrapBlockX(CallbackInfoReturnable<Integer> cir) {
        if (TorusChunkGenerator.isTorusActive()) {
            int original = cir.getReturnValue();
            int wrapped = WorldSize.wrapBlock(original);
            if (wrapped != original) {
                cir.setReturnValue(wrapped);
            }
        }
    }

    @Inject(method = "blockZ", at = @At("RETURN"), cancellable = true)
    private void wrapBlockZ(CallbackInfoReturnable<Integer> cir) {
        if (TorusChunkGenerator.isTorusActive()) {
            int original = cir.getReturnValue();
            int wrapped = WorldSize.wrapBlock(original);
            if (wrapped != original) {
                cir.setReturnValue(wrapped);
            }
        }
    }

    /**
     * Wrap the X parameter of preliminarySurfaceLevel(int x, int z).
     * This method receives raw block coordinates that bypass blockX()/blockZ().
     * Used for surface height estimation during decoration.
     */
    @ModifyVariable(method = "preliminarySurfaceLevel", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int wrapPrelimSurfaceX(int x) {
        if (TorusChunkGenerator.isTorusActive()) {
            return WorldSize.wrapBlock(x);
        }
        return x;
    }

    /**
     * Wrap the Z parameter of preliminarySurfaceLevel(int x, int z).
     */
    @ModifyVariable(method = "preliminarySurfaceLevel", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private int wrapPrelimSurfaceZ(int z) {
        if (TorusChunkGenerator.isTorusActive()) {
            return WorldSize.wrapBlock(z);
        }
        return z;
    }
}