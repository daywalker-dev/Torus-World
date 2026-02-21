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
 *
 * Surface rules receive block coordinates from the chunk's real world position.
 * Even though NoiseChunk now has wrapped startBlockX/Z, the SurfaceRules.Context
 * updateXZ() is called with the REAL chunk block coordinates. We need to wrap
 * these so that surface decoration (grass, sand, etc.) matches the wrapped terrain.
 *
 * KEY: Inject at TAIL (not HEAD), because the original method sets blockX/blockZ
 * from its parameters. Injecting at HEAD would get overwritten.
 *
 * Uses WORLD_SIZE_BLOCKS (block coordinates), NOT WORLD_SIZE_BLOCKS >> 2 (quart).
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$Context")
public abstract class SurfaceRulesContextMixin {

    @Shadow
    protected int blockX;

    @Shadow
    protected int blockZ;

    @Inject(method = "updateXZ", at = @At("TAIL"))
    private void wrapUpdateXZ(int x, int z, CallbackInfo ci) {
        if (TorusChunkGenerator.isTorusActive()) {
            this.blockX = WorldSize.wrapBlock(this.blockX);
            this.blockZ = WorldSize.wrapBlock(this.blockZ);
        }
    }
}