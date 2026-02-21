package base.worldsize.mixin;

import base.worldsize.TorusChunkGenerator;
import base.worldsize.WorldSize;
import net.minecraft.world.level.biome.*;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.level.biome.FixedBiomeSource;

/**
 * Wraps the biome source coordinate lookups.
 *
 * BiomeSource.getNoiseBiome() takes quart coordinates (block >> 2).
 * We wrap these to ensure biomes repeat on the torus.json grid.
 *
 * Note: In modern Minecraft, biome coordinates are at 1/4 block resolution (quart coords).
 * So we need to wrap at WORLD_SIZE_BLOCKS / 4 = WORLD_SIZE_BLOCKS >> 2.
 */

@Mixin(FixedBiomeSource.class)
public abstract class FixedBiomeSourceMixin {

    @Inject(method = "getNoiseBiome", at = @At("HEAD"), cancellable = true)
    private void wrapBiomeCoordinates(int quartX, int quartY, int quartZ, Climate.Sampler sampler,
                                      CallbackInfoReturnable<Holder<Biome>> cir) {
        if (TorusChunkGenerator.isTorusActive()) {
            int quartSize = WorldSize.WORLD_SIZE_BLOCKS >> 2; // quart coords
            int wrappedX = Math.floorMod(quartX, quartSize);
            int wrappedZ = Math.floorMod(quartZ, quartSize);

            if (wrappedX != quartX || wrappedZ != quartZ) {
                // Call the method again with wrapped coordinates, but disable torus.json
                // to prevent infinite recursion
                TorusChunkGenerator.setTorusActive(false);
                try {
                    Holder<Biome> result = ((BiomeSource)(Object)this).getNoiseBiome(wrappedX, quartY, wrappedZ, sampler);
                    cir.setReturnValue(result);
                } finally {
                    TorusChunkGenerator.setTorusActive(true);
                }
            }
        }
    }
}