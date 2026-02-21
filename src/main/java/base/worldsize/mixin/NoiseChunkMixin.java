package base.worldsize.mixin;

import base.worldsize.TorusChunkGenerator;
import base.worldsize.WorldSize;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * THIS IS THE CRITICAL MIXIN that makes terrain wrapping actually work.
 *
 * NoiseChunk implements DensityFunction.FunctionContext, which provides blockX(), blockY(), blockZ()
 * to ALL density functions during terrain generation. By wrapping blockX() and blockZ() here,
 * every noise function, every biome lookup, every density calculation will see wrapped coordinates.
 *
 * This is why previous approaches failed - they tried to wrap at the ChunkGenerator level
 * (too high) or at individual noise functions (too scattered). The NoiseChunk is the single
 * bottleneck where ALL coordinate queries pass through during terrain shape generation.
 *
 * In Mojang mappings (1.21.11):
 *   NoiseChunk.blockX() returns this.cellStartBlockX + this.inCellX
 *   NoiseChunk.blockZ() returns this.cellStartBlockZ + this.inCellZ
 *
 * We intercept the return value and apply modulo wrapping.
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
}