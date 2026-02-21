//package base.worldsize;
//
//import com.mojang.serialization.MapCodec;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import net.minecraft.core.Holder;
//import net.minecraft.world.level.biome.Biome;
//import net.minecraft.world.level.biome.BiomeSource;
//import net.minecraft.world.level.biome.Climate;
//
//import java.util.stream.Stream;
//
///**
// * BiomeSource wrapper that applies modulo wrapping to X and Z coordinates.
// * This ensures biomes repeat seamlessly across the torus.json boundary.
// */
//public class WrappedBiomeSource extends BiomeSource {
//
//    public static final MapCodec<WrappedBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance ->
//            instance.group(
//                    BiomeSource.CODEC.fieldOf("delegate").forGetter(source -> source.delegate),
//                    com.mojang.serialization.Codec.INT.fieldOf("world_size_blocks").forGetter(source -> source.worldSizeBlocks)
//            ).apply(instance, WrappedBiomeSource::new)
//    );
//
//    private final BiomeSource delegate;
//    private final int worldSizeBlocks;
//
//    public WrappedBiomeSource(BiomeSource delegate, int worldSizeBlocks) {
//        this.delegate = delegate;
//        this.worldSizeBlocks = worldSizeBlocks;
//    }
//
//    @Override
//    protected MapCodec<? extends BiomeSource> codec() {
//        return CODEC;
//    }
//
//    /**
//     * Make this public so it's accessible.
//     */
//    @Override
//    public Stream<Holder<Biome>> collectPossibleBiomes() {
//        return delegate.collectPossibleBiomes();
//    }
//
//    /**
//     * CRITICAL: This is where biome lookups happen during terrain generation.
//     * We wrap X and Z before delegating to ensure seamless biome repetition.
//     *
//     * Note: Coordinates are in "biome coordinates" (1/4 block scale), so we
//     * divide worldSizeBlocks by 4 for the wrapping.
//     */
//    @Override
//    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
//        int biomeWorldSize = worldSizeBlocks >> 2; // Divide by 4 for biome coordinates
//        int wrappedX = Math.floorMod(x, biomeWorldSize);
//        int wrappedZ = Math.floorMod(z, biomeWorldSize);
//        return delegate.getNoiseBiome(wrappedX, y, wrappedZ, sampler);
//    }
//}