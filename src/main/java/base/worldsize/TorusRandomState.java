//package base.worldsize;
//
//import net.minecraft.world.level.levelgen.NoiseRouter;
//import net.minecraft.world.level.levelgen.RandomState;
//
///**
// * RandomState wrapper that provides a wrapped NoiseRouter.
// * This ensures all noise sampling goes through our coordinate wrapping.
// */
//public class TorusRandomState extends RandomState {
//
//    private final RandomState delegate;
//    private final NoiseRouter wrappedRouter;
//
//    public TorusRandomState(RandomState delegate, int worldSizeBlocks) {
//        // Call parent constructor with delegate's values
//        super(delegate.legacyLevelSeed());
//
//        this.delegate = delegate;
//        this.wrappedRouter = WrappedNoiseRouter.wrap(delegate.router(), worldSizeBlocks);
//    }
//
//    /**
//     * Return the wrapped router instead of the original.
//     * This is the key method - all noise sampling goes through router().
//     */
//    @Override
//    public NoiseRouter router() {
//        return wrappedRouter;
//    }
//
//    @Override
//    public long legacyLevelSeed() {
//        return delegate.legacyLevelSeed();
//    }
//}