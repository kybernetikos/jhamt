package hamt;

public class Utils {

    public static int index32(int population, int index) {
        assert index >= 0;
        assert index < 32;

        final int position = 1 << index;
        final int lowerCount = Integer.bitCount((position - 1) & population);
        if ((population & position) != 0) {
           return lowerCount;
        }
        return -lowerCount - 1;
    }

    public static int index64(long population, int index) {
        assert index >= 0;
        assert index < 64;

        final long position = 1L << index;
        final int lowerCount = Long.bitCount((position - 1) & population);
        if ((population & position) != 0) {
            return lowerCount;
        }
        return -lowerCount - 1;
    }
}
