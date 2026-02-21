package base.worldsize.mixin;

import base.worldsize.TorusChunkGenerator;
import base.worldsize.WorldSize;
// FIX #2: WorldGenRegion is in net.minecraft.server.level package
import net.minecraft.server.level.WorldGenRegion;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for WorldGenRegion.
 *
 * WorldGenRegion is used during decoration/feature placement.
 * The primary wrapping is handled by NoiseChunkMixin and BiomeSourceMixin,
 * but this mixin exists as the required target from worldsize.mixins.json.
 *
 * Note: The actual wrapping heavy-lifting is in NoiseChunkMixin (for terrain shape)
 * and BiomeSourceMixin (for biome placement). This class is kept as a named
 * anchor point referenced in the mixins JSON.
 */
@Mixin(WorldGenRegion.class)
public abstract class WorldSizeMixin {
    // The actual coordinate wrapping is handled by:
    // - NoiseChunkMixin: wraps blockX()/blockZ() for all density function evaluation
    // - BiomeSourceMixin: wraps biome coordinate lookups
    // - NoiseBasedChunkGeneratorMixin: ensures torus.json flag for height queries
    //
    // WorldGenRegion doesn't need direct injection because it delegates
    // to the chunk generator and biome source, which are already wrapped.
}


























//package base.worldsize.mixin;
//
//import net.minecraft.server.level.ServerLevel;
//import org.spongepowered.asm.mixin.Mixin;
//
//@Mixin(ServerLevel.class)
//public class WorldSizeMixin {
//    // This Mixin currently does nothing.
//}
