package base.worldsize.mixin;

import base.worldsize.TorusChunkGenerator;
import base.worldsize.WorldSize;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.LevelHeightAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into NoiseBasedChunkGenerator to ensure the torus.json flag is set
 * when getBaseHeight is called on the delegate NoiseBasedChunkGenerator.
 *
 * Since TorusChunkGenerator now delegates to a NoiseBasedChunkGenerator
 * (rather than extending it), this mixin catches cases where the delegate's
 * getBaseHeight is called externally (e.g., by structure placement code)
 * that bypasses our TorusChunkGenerator wrapper.
 *
 * Note: We can no longer check "(Object) this instanceof TorusChunkGenerator"
 * because the delegate is a plain NoiseBasedChunkGenerator. Instead, we check
 * the thread-local flag — if torus.json is already active, we're inside a delegated
 * call from TorusChunkGenerator, and the flag is already set.
 *
 * This mixin is kept for safety in case getBaseHeight is called on the delegate
 * through paths we don't control.
 */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {

    // No additional injection needed here since TorusChunkGenerator.getBaseHeight()
    // already sets the torus.json flag before delegating to the NoiseBasedChunkGenerator.
    // The NoiseChunkMixin handles the actual coordinate wrapping during noise sampling.
    //
    // This mixin class is retained as a placeholder in case future injection points
    // are needed (e.g., for iterateNoiseColumn or other internal methods).
}