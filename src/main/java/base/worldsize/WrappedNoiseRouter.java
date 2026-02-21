//package base.worldsize;
//
//import net.minecraft.world.level.levelgen.NoiseRouter;
//import net.minecraft.world.level.levelgen.DensityFunction;
//
///**
// * Wraps all DensityFunctions in a NoiseRouter with coordinate wrapping.
// * This ensures that ALL noise sampling (terrain, caves, ore veins, etc.) uses wrapped coordinates.
// */
//public class WrappedNoiseRouter {
//
//    /**
//     * Creates a new NoiseRouter with all density functions wrapped.
//     */
//    public static NoiseRouter wrap(NoiseRouter original, int worldSizeBlocks) {
//        return new NoiseRouter(
//                wrap(original.barrierNoise(), worldSizeBlocks),
//                wrap(original.fluidLevelFloodedness(), worldSizeBlocks),
//                wrap(original.fluidLevelSpread(), worldSizeBlocks),
//                wrap(original.lavaNoise(), worldSizeBlocks),
//                wrap(original.temperature(), worldSizeBlocks),
//                wrap(original.vegetation(), worldSizeBlocks),
//                wrap(original.continents(), worldSizeBlocks),
//                wrap(original.erosion(), worldSizeBlocks),
//                wrap(original.depth(), worldSizeBlocks),
//                wrap(original.ridges(), worldSizeBlocks),
//                wrap(original.initialDensityWithoutJaggedness(), worldSizeBlocks),
//                wrap(original.finalDensity(), worldSizeBlocks),
//                wrap(original.veinToggle(), worldSizeBlocks),
//                wrap(original.veinRidged(), worldSizeBlocks),
//                wrap(original.veinGap(), worldSizeBlocks)
//        );
//    }
//
//    /**
//     * Wraps a single DensityFunction with coordinate wrapping.
//     */
//    private static DensityFunction wrap(DensityFunction function, int worldSizeBlocks) {
//        return new WrappedDensityFunction(function, worldSizeBlocks);
//    }
//}