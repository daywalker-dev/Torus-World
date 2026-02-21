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