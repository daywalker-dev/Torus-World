package base.worldsize;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldSize implements ModInitializer {

	public static final String MOD_ID = "worldsize";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * World size in chunks. The world wraps every WORLD_SIZE_CHUNKS chunks on both X and Z axes.
	 * 64 chunks = 1024 blocks.
	 */
	public static final int WORLD_SIZE_CHUNKS = 64;
	public static final int WORLD_SIZE_BLOCKS = WORLD_SIZE_CHUNKS * 16; // 1024

	@Override
	public void onInitialize() {
		LOGGER.info("[WorldSize] Initializing Torus World mod - world size: {}x{} blocks ({}x{} chunks)",
				WORLD_SIZE_BLOCKS, WORLD_SIZE_BLOCKS, WORLD_SIZE_CHUNKS, WORLD_SIZE_CHUNKS);

		// Register the custom chunk generator
		Registry.register(
				BuiltInRegistries.CHUNK_GENERATOR,
				Identifier.fromNamespaceAndPath(MOD_ID, "torus"),
				TorusChunkGenerator.CODEC
		);

		// Register player teleportation on tick to wrap players at world boundaries
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				double x = player.getX();
				double z = player.getZ();
				double newX = wrapCoordinate(x);
				double newZ = wrapCoordinate(z);

				if (newX != x || newZ != z) {
					// FIX #1: serverLevel() does not exist in 1.21.11 Mojang mappings.
					// Use level() inherited from Entity, then cast to ServerLevel.
					ServerLevel serverLevel = (ServerLevel) player.level();
					player.teleportTo(
							serverLevel,
							newX, player.getY(), newZ,
							java.util.Set.of(),
							player.getYRot(), player.getXRot(),
							false
					);
					LOGGER.debug("[WorldSize] Wrapped player {} from ({}, {}) to ({}, {})",
							player.getName().getString(), x, z, newX, newZ);
				}
			}
		});

		LOGGER.info("[WorldSize] Torus World mod initialized successfully");
	}

	/**
	 * Wraps a block coordinate into the range [0, WORLD_SIZE_BLOCKS).
	 * Uses Java's floorMod for correct negative number handling.
	 */
	public static int wrapBlock(int coord) {
		return Math.floorMod(coord, WORLD_SIZE_BLOCKS);
	}

	/**
	 * Wraps a chunk coordinate into the range [0, WORLD_SIZE_CHUNKS).
	 */
	public static int wrapChunk(int chunkCoord) {
		return Math.floorMod(chunkCoord, WORLD_SIZE_CHUNKS);
	}

	/**
	 * Wraps a double coordinate (for entity positions) into [0, WORLD_SIZE_BLOCKS).
	 */
	public static double wrapCoordinate(double coord) {
		double wrapped = coord % WORLD_SIZE_BLOCKS;
		if (wrapped < 0) wrapped += WORLD_SIZE_BLOCKS;
		return wrapped;
	}
}







//
//
//
//
//
//
//# Prompt: Create Minecraft 1.21.11 Torus World Generator mod (Wrapping Not Working)
//
//## Context
//I want a Fabric mod for Minecraft 1.21.11 (Mojang mappings) that creates a torus.json world generator.
//
//
//## What I've Tried
//		1. Wrapping coordinates in `getBaseHeight()` and `getBaseColumn()` - compiles but doesn't wrap at runtime
//		2. Creating a `WrappedChunkAccess` class to present wrapped chunk positions to the delegate - compiles but doesn't wrap at runtime
//		3. Wrapping in `fillFromNoise()` by creating a wrapped chunk and copying data back - compiles but doesn't wrap at runtime
//
//		## The Problem
//The terrain generation doesn't actually repeat. When I explore to coordinates beyond `worldSizeBlocks`, I see new/different terrain instead of the wrapped terrain from the beginning of the world.
//
//		## Key Requirements
//- Minecraft 1.21.11 with **Mojang mappings** (not Yarn)
//		- Fabric mod
//- Must use official APIs (no private method access)
//- Seamless wrapping on X and Z axes (like a torus.json)
//- World size: configurable (e.g., 64 chunks = 1024 blocks)
//
//## API Constraints (1.21.11)
//```java
//// ChunkGenerator method signatures (6 params, NOT 7):
//void applyCarvers(WorldGenRegion, long, RandomState, BiomeManager, StructureManager, ChunkAccess)
//
//// fillFromNoise (4 params, NOT 5):
//CompletableFuture<ChunkAccess> fillFromNoise(Blender, RandomState, StructureManager, ChunkAccess)
//
//// setBlockState signature:
//BlockState setBlockState(BlockPos, BlockState, @UpdateFlags int)
//
//// ProtoChunk constructor:
//ProtoChunk(ChunkPos, UpgradeData, LevelHeightAccessor, PalettedContainerFactory, BlendingData)
//
//// ResourceLocation was renamed:
//import net.minecraft.resources.Identifier;
//
//## What I Need
//A working solution that:
//		1. Actually wraps terrain generation at runtime (not just compiles)
//2. Uses only public/stable APIs from 1.21.11
//		3. Handles biomes, structures, and noise generation correctly
//
//		## Possible Approaches to Investigate
//1. **Mixin approach**: Inject into noise sampling or biome lookup methods
//2. **Custom BiomeSource**: Wrap the BiomeSource to apply coordinate wrapping
//3. **Custom NoiseRouter**: Wrap density functions to apply modulo to coordinates
//4. **WorldGenRegion wrapper**: Intercept chunk access at a higher level
//5. **Different delegation point**: Maybe fillFromNoise isn't where wrapping should happen
//
//		## Debug Questions
//- At what point in the generation pipeline are coordinates "locked in"?
//		- Does the vanilla generator cache chunks based on position?
//		- Are we wrapping too late (after noise has already been sampled)?
//		- Should we wrap at the BiomeSource level instead of ChunkGenerator level?
//
//		## Files I Have
//- TorusChunkGenerator.java - extends ChunkGenerator, wraps coordinates
//- WorldSize.java - ModInitializer, registers the generator
//- fabric.mod.json - mod metadata
//- torus_world_preset.json - world preset configuration
//
//## Expected Behavior
//When I walk to X=1024 in a 1024-block world, I should seamlessly wrap to X=0 and see the same terrain that exists at the spawn point.
//
//## Actual Behavior
//When I walk to X=1024, I see completely new terrain - no terrain wrapping occurs.