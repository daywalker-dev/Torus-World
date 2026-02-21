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
 * FIXES from previous version:
 * 1. Inject at TAIL, not HEAD. The original updateXZ() method sets blockX/blockZ
 *    from its parameters. If we inject at HEAD, the original method overwrites
 *    our wrapped values immediately after. At TAIL, we wrap AFTER the original
 *    method has finished setting the fields.
 *
 * 2. Use WORLD_SIZE_BLOCKS, not WORLD_SIZE_BLOCKS >> 2. The updateXZ method
 *    receives BLOCK coordinates, not quart coordinates. The previous code
 *    incorrectly divided by 4, which would wrap at 256 instead of 1024.
 */
@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$Context")
public abstract class SurfaceRulesContextMixin {

    @Shadow
    protected int blockX;

    @Shadow
    protected int blockZ;

    /**
     * Wrap coordinates AFTER the original updateXZ has set them.
     * Using TAIL ensures we modify the already-set values.
     */
    @Inject(method = "updateXZ", at = @At("TAIL"))
    private void wrapUpdateXZ(int x, int z, CallbackInfo ci) {
        if (TorusChunkGenerator.isTorusActive()) {
            this.blockX = WorldSize.wrapBlock(this.blockX);
            this.blockZ = WorldSize.wrapBlock(this.blockZ);
        }
    }
}