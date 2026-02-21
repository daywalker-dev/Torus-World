//package base.worldsize;
//
//public class TorusConfig {
//
//    // ----- CONFIGURE WORLD SIZE HERE -----
//    public static final int RADIUS = 16 * 30;
//    public static final int WORLD_WIDTH  = RADIUS;
//    public static final int WORLD_LENGTH = RADIUS;
//    // -------------
//    /**
//     * Wraps a value to the given size using modulus, positive-only
//     */
//    public static int wrap(int value, int size) {
//        int result = value % size;
//        return result < 0 ? result + size : result;
//    }
//
//    public static int wrapBlockX(int x) {
//        return wrap(x, WORLD_WIDTH);
//    }
//
//    public static int wrapBlockZ(int z) {
//        return wrap(z, WORLD_LENGTH);
//    }
//}
