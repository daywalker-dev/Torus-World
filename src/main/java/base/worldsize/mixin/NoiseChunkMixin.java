package base.worldsize.mixin;

import base.worldsize.TorusChunkGenerator;
import base.worldsize.WorldSize;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkPos;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * THIS IS THE CRITICAL MIXIN that makes terrain wrapping actually work.
 *
 * HOW IT WORKS:
 * NoiseChunk.forChunk() is the static factory that creates a NoiseChunk from
 * a ChunkAccess. It calls chunk.getPos() to extract the chunk's position, then
 * uses getMinBlockX()/getMinBlockZ() to get start block coordinates which are
 * passed to the NoiseChunk constructor.
 *
 * We intercept the getPos() call and return a WRAPPED ChunkPos. This means the
 * entire NoiseChunk "thinks" it's at the wrapped position:
 * - Aquifer arrays are allocated for the wrapped grid position (no OOB)
 * - blockX()/blockZ() naturally return wrapped values (cellStartBlockX + inCellX)
 * - All density functions see wrapped coordinates automatically
 * - Chunk at world position (1024, 0) generates terrain identical to (0, 0)
 *
 * The actual block data still gets written to the correct ChunkAccess at the
 * real world position — only the NOISE SAMPLING sees wrapped coordinates.
 *
 * BUG FIX (from previous version):
 * --------------------------------
 * The previous mixin used @ModifyArg with `target = "<init>"` WITHOUT a class
 * qualifier. This is AMBIGUOUS because NoiseChunk.forChunk() contains MULTIPLE
 * constructor invocations — not just `new NoiseChunk(...)`, but also helper
 * objects like `new Aquifer.FluidStatus(...)` and potentially others.
 *
 * The bare "<init>" matched constructor calls indiscriminately. The @ModifyArg
 * with index=2 then corrupted parameters of the WRONG constructor (likely an
 * Aquifer.FluidStatus or FluidPicker-related object), causing:
 *   ArrayIndexOutOfBoundsException: Index -130 out of bounds for length 315
 *
 * THE FIX: Instead of targeting <init> (fragile), we use @Redirect on the
 * chunk.getPos() call in forChunk(). This returns a wrapped ChunkPos, so ALL
 * position-dependent code in forChunk() (including startBlockX, startBlockZ,
 * FluidPicker setup, etc.) consistently sees wrapped coordinates. This is both
 * safer (unambiguous target) and more thorough (wraps everything, not just
 * two constructor params).
 */
@Mixin(NoiseChunk.class)
public abstract class NoiseChunkMixin {

    /**
     * Redirect chunk.getPos() inside forChunk() to return a wrapped ChunkPos.
     *
     * This ensures all position-derived values in forChunk() use wrapped coordinates:
     * - startBlockX / startBlockZ (passed to NoiseChunk constructor)
     * - Any FluidPicker or Beardifier position computations
     * - Aquifer grid setup coordinates
     *
     * The @Redirect targets ChunkAccess.getPos(), which is a specific, unambiguous
     * method — unlike "<init>" which could match any constructor call.
     *
     * If forChunk() calls getPos() multiple times, ALL calls are redirected,
     * ensuring complete position consistency throughout the method.
     */
    @Redirect(
            method = "forChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getPos()Lnet/minecraft/world/level/chunk/ChunkPos;"
            )
    )
    private static ChunkPos wrapChunkPosInForChunk(ChunkAccess chunk) {
        if (TorusChunkGenerator.isTorusActive()) {
            ChunkPos real = chunk.getPos();
            int wrappedChunkX = WorldSize.wrapChunk(real.x);
            int wrappedChunkZ = WorldSize.wrapChunk(real.z);
            if (wrappedChunkX != real.x || wrappedChunkZ != real.z) {
                return new ChunkPos(wrappedChunkX, wrappedChunkZ);
            }
            return real;
        }
        return chunk.getPos();
    }
}