//package base.worldsize;
//
//import net.minecraft.world.level.levelgen.DensityFunction;
//
///**
// * DensityFunction wrapper that applies modulo wrapping to X and Z coordinates.
// * This is the CRITICAL piece - it ensures all noise sampling uses wrapped coordinates.
// */
//public class WrappedDensityFunction implements DensityFunction {
//
//    private final DensityFunction delegate;
//    private final int worldSizeBlocks;
//
//    public WrappedDensityFunction(DensityFunction delegate, int worldSizeBlocks) {
//        this.delegate = delegate;
//        this.worldSizeBlocks = worldSizeBlocks;
//    }
//
//    /**
//     * This is called for every block during terrain generation.
//     * Wrapping here ensures that noise(1024, y, z) returns the same value as noise(0, y, z).
//     */
//    @Override
//    public double compute(FunctionContext context) {
//        int x = context.blockX();
//        int y = context.blockY();
//        int z = context.blockZ();
//
//        // Wrap X and Z coordinates
//        int wrappedX = Math.floorMod(x, worldSizeBlocks);
//        int wrappedZ = Math.floorMod(z, worldSizeBlocks);
//
//        // Create wrapped context
//        return delegate.compute(new WrappedFunctionContext(context, wrappedX, y, wrappedZ));
//    }
//
//    @Override
//    public void fillArray(double[] array, ContextProvider contextProvider) {
//        // For batch operations, we need to wrap each position
//        contextProvider.fillAllDirectly(array, this);
//    }
//
//    @Override
//    public DensityFunction mapAll(Visitor visitor) {
//        // When the system traverses the density function tree, preserve wrapping
//        return visitor.apply(new WrappedDensityFunction(delegate.mapAll(visitor), worldSizeBlocks));
//    }
//
//    @Override
//    public double minValue() {
//        return delegate.minValue();
//    }
//
//    @Override
//    public double maxValue() {
//        return delegate.maxValue();
//    }
//
//    /**
//     * FunctionContext wrapper that reports wrapped coordinates.
//     */
//    private static class WrappedFunctionContext implements FunctionContext {
//        private final FunctionContext delegate;
//        private final int x, y, z;
//
//        public WrappedFunctionContext(FunctionContext delegate, int x, int y, int z) {
//            this.delegate = delegate;
//            this.x = x;
//            this.y = y;
//            this.z = z;
//        }
//
//        @Override
//        public int blockX() {
//            return x;
//        }
//
//        @Override
//        public int blockY() {
//            return y;
//        }
//
//        @Override
//        public int blockZ() {
//            return z;
//        }
//    }
//}