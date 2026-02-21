package base.worldsize;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A chunk generator that delegates to NoiseBasedChunkGenerator to create torus-world terrain.
 *
 * NoiseBasedChunkGenerator is final in 1.21.11 and cannot be extended.
 * Instead, we extend ChunkGenerator directly and DELEGATE to an internal
 * NoiseBasedChunkGenerator instance.
 *
 * KEY FIX: The previous implementation used a ThreadLocal<Boolean> to signal
 * torus mode to mixins. This FAILED because fillFromNoise() and createBiomes()
 * run asynchronously on background threads via CompletableFuture.supplyAsync().
 * The ThreadLocal was set on the calling thread but was never visible on the
 * worker thread where NoiseChunk.blockX()/blockZ() actually execute.
 *
 * NEW APPROACH: We use a static volatile boolean that is set to true once a
 * TorusChunkGenerator is constructed and remains true for the server's lifetime.
 * This works on ALL threads — main, worker, async — because it's a shared flag.
 *
 * Consequence: All dimensions (Overworld, Nether, End) will have torus wrapping.
 * This is consistent behavior for a torus world.
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

    /**
     * GLOBAL FLAG: Is a torus world active?
     *
     * Set to true when a TorusChunkGenerator is constructed. This is a volatile
     * boolean (not ThreadLocal) so it is visible on ALL threads, including the
     * background worker threads used by fillFromNoise() and createBiomes().
     *
     * This is the critical fix — ThreadLocal does NOT propagate to
     * CompletableFuture worker threads.
     */
    private static volatile boolean TORUS_ACTIVE = false;

    public TorusChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource);
        this.delegate = new NoiseBasedChunkGenerator(biomeSource, settings);

        // Set the global flag — from this point, all noise generation will wrap coordinates
        TORUS_ACTIVE = true;

        WorldSize.LOGGER.info("[WorldSize] TorusChunkGenerator created — torus wrapping is now ACTIVE globally");
        WorldSize.LOGGER.info("[WorldSize] World size: {}x{} blocks ({}x{} chunks)",
                WorldSize.WORLD_SIZE_BLOCKS, WorldSize.WORLD_SIZE_BLOCKS,
                WorldSize.WORLD_SIZE_CHUNKS, WorldSize.WORLD_SIZE_CHUNKS);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    // =========================================================================
    // Delegation methods — no ThreadLocal manipulation needed anymore
    // =========================================================================

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            Blender blender,
            RandomState randomState,
            StructureManager structureManager,
            ChunkAccess chunkAccess
    ) {
        // No ThreadLocal needed — TORUS_ACTIVE is a global volatile boolean
        // visible on ALL threads including the async worker threads
        return delegate.fillFromNoise(blender, randomState, structureManager, chunkAccess);
    }

    @Override
    public void buildSurface(
            WorldGenRegion region,
            StructureManager structureManager,
            RandomState randomState,
            ChunkAccess chunkAccess
    ) {
        delegate.buildSurface(region, structureManager, randomState, chunkAccess);
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
        delegate.applyCarvers(region, seed, randomState, biomeManager, structureManager, chunkAccess);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor level, RandomState randomState) {
        return delegate.getBaseHeight(x, z, heightmap, level, randomState);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        return delegate.getBaseColumn(x, z, level, randomState);
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunkAccess, StructureManager structureManager) {
        delegate.applyBiomeDecoration(level, chunkAccess, structureManager);
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
        // No ThreadLocal needed — works on async threads automatically
        return delegate.createBiomes(randomState, blender, structureManager, chunkAccess);
    }

    /**
     * Check if torus wrapping is active.
     * Called by mixins to decide whether to wrap coordinates.
     *
     * Returns the global volatile boolean — visible on ALL threads.
     */
    public static boolean isTorusActive() {
        return TORUS_ACTIVE;
    }

    /**
     * Deactivate torus mode (e.g., on server shutdown).
     * Optional — the flag is automatically reset when the JVM restarts.
     */
    public static void deactivate() {
        TORUS_ACTIVE = false;
    }
}