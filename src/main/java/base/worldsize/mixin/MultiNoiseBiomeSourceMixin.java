package base.worldsize.mixin;

import base.worldsize.TorusChunkGenerator;
import base.worldsize.WorldSize;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Wraps the biome source coordinate lookups for MultiNoiseBiomeSource.
 *
 * BiomeSource.getNoiseBiome() takes quart coordinates (block >> 2).
 * We wrap these to ensure biomes repeat on the torus grid.
 *
 * RECURSION GUARD: We use a ThreadLocal boolean to prevent infinite recursion
 * when we call getNoiseBiome() with wrapped coordinates. This is separate from
 * the torus-active flag (which is a global volatile boolean). The recursion guard
 * works correctly as a ThreadLocal because the recursive call happens on the
 * SAME thread, synchronously.
 */
@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {

    @Unique
    private static final ThreadLocal<Boolean> worldsize$wrapping = ThreadLocal.withInitial(() -> false);

    @Inject(method = "getNoiseBiome", at = @At("HEAD"), cancellable = true)
    private void wrapBiomeCoordinates(int quartX, int quartY, int quartZ, Climate.Sampler sampler,
                                      CallbackInfoReturnable<Holder<Biome>> cir) {
        if (TorusChunkGenerator.isTorusActive() && !worldsize$wrapping.get()) {
            int quartSize = WorldSize.WORLD_SIZE_BLOCKS >> 2; // quart coords = blocks / 4
            int wrappedX = Math.floorMod(quartX, quartSize);
            int wrappedZ = Math.floorMod(quartZ, quartSize);

            if (wrappedX != quartX || wrappedZ != quartZ) {
                // Use ThreadLocal recursion guard (same thread, synchronous call)
                worldsize$wrapping.set(true);
                try {
                    Holder<Biome> result = ((BiomeSource)(Object)this).getNoiseBiome(wrappedX, quartY, wrappedZ, sampler);
                    cir.setReturnValue(result);
                } finally {
                    worldsize$wrapping.set(false);
                }
            }
        }
    }
}