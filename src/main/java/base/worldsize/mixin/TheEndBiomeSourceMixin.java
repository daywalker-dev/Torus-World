package base.worldsize.mixin;

import base.worldsize.TorusChunkGenerator;
import base.worldsize.WorldSize;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TheEndBiomeSource.class)
public abstract class TheEndBiomeSourceMixin {

    @Unique
    private static final ThreadLocal<Boolean> worldsize$wrapping = ThreadLocal.withInitial(() -> false);

    @Inject(method = "getNoiseBiome", at = @At("HEAD"), cancellable = true)
    private void wrapBiomeCoordinates(int quartX, int quartY, int quartZ, Climate.Sampler sampler,
                                      CallbackInfoReturnable<Holder<Biome>> cir) {
        if (TorusChunkGenerator.isTorusActive() && !worldsize$wrapping.get()) {
            int quartSize = WorldSize.WORLD_SIZE_BLOCKS >> 2;
            int wrappedX = Math.floorMod(quartX, quartSize);
            int wrappedZ = Math.floorMod(quartZ, quartSize);

            if (wrappedX != quartX || wrappedZ != quartZ) {
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