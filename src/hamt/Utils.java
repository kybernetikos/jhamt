package hamt;

import java.util.Comparator;

public class Utils {

    static final @SuppressWarnings("unchecked") Comparator<Entry> keyComparator = Comparator.comparing(Entry::getKey, Comparator.nullsLast(Comparator.naturalOrder()));
    static final int maskBits = 6;
    static final long mask = (1L << maskBits) - 1;

    public static int index32(final int population, final int index) {
        assert index >= 0;
        assert index < 32;

        final int position = 1 << index;
        final int lowerCount = Integer.bitCount((position - 1) & population);
        if ((population & position) != 0) {
           return lowerCount;
        }
        return -lowerCount - 1;
    }

    public static int index64(final long population, final int index) {
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