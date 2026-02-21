package base.worldsize;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A chunk generator that delegates to NoiseBasedChunkGenerator to create torus.json-world terrain.
 *
 * FIX #4: NoiseBasedChunkGenerator is final in 1.21.11 and cannot be extended.
 * Instead, we extend ChunkGenerator directly and DELEGATE to an internal
 * NoiseBasedChunkGenerator instance. This gives us our own codec/type registration
 * while reusing all vanilla noise generation logic.
 *
 * The key insight for torus.json wrapping: we use Mixins to wrap at the lowest level —
 * inside NoiseChunk.blockX()/blockZ() and BiomeSource.getNoiseBiome().
 * This ensures ALL systems (noise, biomes, surface rules, carvers, etc.)
 * see wrapped coordinates consistently.
 *
 * This generator class exists primarily for:
 * 1. Registration as a custom generator type (so we can detect torus.json worlds)
 * 2. Codec serialization for world presets
 * 3. A marker that tells our mixins "this world should wrap"
 */
public class TorusChunkGenerator extends ChunkGenerator {

    public static final MapCodec<TorusChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(gen -> gen.delegate.getBiomeSource()),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(gen -> gen.delegate.generatorSettings())
            ).apply(instance, TorusChunkGenerator::new)
    );

    /** The vanilla noise generator we delegate all actual generation to. */
    private final NoiseBasedChunkGenerator delegate;

    /** Thread-local flag: is the current world a torus.json world? */
    private static final ThreadLocal<Boolean> IS_TORUS_WORLD = ThreadLocal.withInitial(() -> false);

    public TorusChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource);
        this.delegate = new NoiseBasedChunkGenerator(biomeSource, settings);
        WorldSize.LOGGER.info("[WorldSize] TorusChunkGenerator created with world size {}x{} blocks",
                WorldSize.WORLD_SIZE_BLOCKS, WorldSize.WORLD_SIZE_BLOCKS);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    // =========================================================================
    // Delegation methods — each sets the torus.json flag so mixins know to wrap
    // =========================================================================

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            Blender blender,
            RandomState randomState,
            StructureManager structureManager,
            ChunkAccess chunkAccess
    ) {
        IS_TORUS_WORLD.set(true);
        try {
            return delegate.fillFromNoise(blender, randomState, structureManager, chunkAccess);
        } finally {
            IS_TORUS_WORLD.set(false);
        }
    }

    @Override
    public void buildSurface(
            WorldGenRegion region,
            StructureManager structureManager,
            RandomState randomState,
            ChunkAccess chunkAccess
    ) {
        IS_TORUS_WORLD.set(true);
        try {
            delegate.buildSurface(region, structureManager, randomState, chunkAccess);
        } finally {
            IS_TORUS_WORLD.set(false);
        }
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
        delegate.spawnOriginalMobs(worldGenRegion);
    }

    @Override
    public void applyCarvers(
            WorldGenRegion region,
            long seed,
            RandomState randomState,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunkAccess
    ) {
        IS_TORUS_WORLD.set(true);
        try {
            delegate.applyCarvers(region, seed, randomState, biomeManager, structureManager, chunkAccess);
        } finally {
            IS_TORUS_WORLD.set(false);
        }
    }

//    @Override
//    public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor level, RandomState randomState) {
//        // Wrap the input coordinates for height queries
//        int wrappedX = WorldSize.wrapBlock(x);
//        int wrappedZ = WorldSize.wrapBlock(z);
//        IS_TORUS_WORLD.set(true);
//        try {
//            return delegate.getBaseHeight(wrappedX, wrappedZ, heightmap, level, randomState);
//        } finally {
//            IS_TORUS_WORLD.set(false);
//        }
//    }
//
//    @Override
//    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
//        int wrappedX = WorldSize.wrapBlock(x);
//        int wrappedZ = WorldSize.wrapBlock(z);
//        IS_TORUS_WORLD.set(true);
//        try {
//            return delegate.getBaseColumn(wrappedX, wrappedZ, level, randomState);
//        } finally {
//            IS_TORUS_WORLD.set(false);
//        }
//    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor level, RandomState randomState) {
        IS_TORUS_WORLD.set(true);
        try {
            return delegate.getBaseHeight(x, z, heightmap, level, randomState);
        } finally {
            IS_TORUS_WORLD.set(false);
        }
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        IS_TORUS_WORLD.set(true);
        try {
            return delegate.getBaseColumn(x, z, level, randomState);
        } finally {
            IS_TORUS_WORLD.set(false);
        }
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunkAccess, StructureManager structureManager) {
        IS_TORUS_WORLD.set(true);
        try {
            delegate.applyBiomeDecoration(level, chunkAccess, structureManager);
        } finally {
            IS_TORUS_WORLD.set(false);
        }
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        delegate.addDebugScreenInfo(info, randomState, pos);
    }

    @Override
    public int getGenDepth() {
        return delegate.getGenDepth();
    }

    @Override
    public int getSeaLevel() {
        return delegate.getSeaLevel();
    }

    @Override
    public int getMinY() {
        return delegate.getMinY();
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(
            RandomState randomState,
            Blender blender,
            StructureManager structureManager,
            ChunkAccess chunkAccess
    ) {
        IS_TORUS_WORLD.set(true);
        try {
            return delegate.createBiomes(randomState, blender, structureManager, chunkAccess);
        } finally {
            IS_TORUS_WORLD.set(false);
        }
    }

    /**
     * Check if the current thread is generating for a torus.json world.
     * Called by mixins to decide whether to wrap coordinates.
     */
    public static boolean isTorusActive() {
        return IS_TORUS_WORLD.get();
    }

    /**
     * Manually activate/deactivate torus.json mode (for mixin use).
     */
    public static void setTorusActive(boolean active) {
        IS_TORUS_WORLD.set(active);
    }
}