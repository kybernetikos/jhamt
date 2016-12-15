package hamt;

import java.util.Arrays;
import java.util.Comparator;

public class Utils {
    // the number of bits each level of the tree uses
    static final int maskBits = 6;
    // a mask with maskBits number of 1s.  It will be shifted according to depth in the tree in order to extract
    // some of the hash bits.
    static final long mask = (1L << maskBits) - 1;

    @SuppressWarnings("unchecked")
    private static final Comparator<Comparable> natural = Comparator.naturalOrder();
    final static Comparator<Comparable> nullFriendlyComparator = Comparator.nullsLast(natural);
    static final Comparator<Entry> keyComparator = Comparator.comparing(Entry::getKey, nullFriendlyComparator);

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

    static int extractHashPart(final long hash, final int place) {
        assert place >= 0;

        return (int) ((hash & (mask << place)) >>> place);
    }

    static <T> T[] arrayInsert(final T[] array, final int position, final T value) {
        final T[] nodes = Arrays.copyOf(array, array.length + 1);
        System.arraycopy(nodes, position, nodes, position + 1, array.length - position);
        nodes[position] = value;
        return nodes;
    }

    static <T> T[] arrayRemove(final T[] array, final int position) {
        final T[] nodes = Arrays.copyOf(array, array.length - 1);
        System.arraycopy(array, position + 1, nodes, position, array.length - position - 1);
        return nodes;
    }
}