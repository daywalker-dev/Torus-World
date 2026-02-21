package base.worldsize.mixin;

import base.worldsize.TorusChunkGenerator;
import base.worldsize.WorldSize;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * THIS IS THE CRITICAL MIXIN that makes terrain wrapping actually work.
 *
 * PREVIOUS APPROACH (BROKEN - ArrayIndexOutOfBoundsException in Aquifer):
 * -----------------------------------------------------------------------
 * We wrapped the return values of blockX()/blockZ(). This caused:
 *   ArrayIndexOutOfBoundsException: Index 386 out of bounds for length 315
 *     at Aquifer$NoiseBasedAquifer.computeSubstance
 *
 * WHY IT CRASHED:
 * NoiseChunk is constructed for a specific chunk at specific world coordinates.
 * Internally, Aquifer$NoiseBasedAquifer allocates fixed-size arrays based on
 * the chunk's cell grid (derived from cellStartBlockX/Z). When our mixin
 * changed blockX() from 1030 to 6 (wrapped), the Aquifer tried to compute a
 * grid index relative to the original cellStartBlockX=1024. The wrapped
 * coordinate 6 was outside the grid's range → array index out of bounds.
 *
 * THE FIX:
 * --------
 * Instead of wrapping blockX()/blockZ() AFTER construction, we wrap the
 * startBlockX/startBlockZ coordinates DURING NoiseChunk construction.
 *
 * NoiseChunk.forChunk() is the static factory that creates a NoiseChunk from
 * a ChunkAccess. It extracts the chunk's start block position and passes it
 * to the NoiseChunk constructor. We intercept those parameters and wrap them.
 *
 * This means the entire NoiseChunk "thinks" it's at the wrapped position:
 * - Aquifer arrays are allocated for the wrapped grid position (no OOB)
 * - blockX()/blockZ() naturally return wrapped values (cellStartBlockX + inCellX)
 * - All density functions see wrapped coordinates automatically
 * - Chunk at world position (1024, 0) generates terrain identical to (0, 0)
 *
 * The actual block data still gets written to the correct ChunkAccess at the
 * real world position — only the NOISE SAMPLING sees wrapped coordinates.
 *
 * NoiseChunk constructor params (Mojang mappings 1.21.11):
 *   (int cellCountXZ, RandomState, int startBlockX, int startBlockZ,
 *    NoiseSettings, BeardifierOrMarker, NoiseGeneratorSettings,
 *    FluidPicker, Blender)
 *
 * startBlockX is at index 2, startBlockZ is at index 3.
 */
@Mixin(NoiseChunk.class)
public abstract class NoiseChunkMixin {

    /**
     * Wrap startBlockX (constructor param index 2) in forChunk's call to new NoiseChunk(...).
     */
    @ModifyArg(
            method = "forChunk",
            at = @At(value = "INVOKE", target = "<init>"),
            index = 2
    )
    private static int wrapStartBlockX(int startBlockX) {
        if (TorusChunkGenerator.isTorusActive()) {
            return WorldSize.wrapBlock(startBlockX);
        }
        return startBlockX;
    }

    /**
     * Wrap startBlockZ (constructor param index 3) in forChunk's call to new NoiseChunk(...).
     */
    @ModifyArg(
            method = "forChunk",
            at = @At(value = "INVOKE", target = "<init>"),
            index = 3
    )
    private static int wrapStartBlockZ(int startBlockZ) {
        if (TorusChunkGenerator.isTorusActive()) {
            return WorldSize.wrapBlock(startBlockZ);
        }
        return startBlockZ;
    }
}