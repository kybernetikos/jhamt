package hamt;

import java.util.Arrays;
import java.util.Comparator;

public class Utils {
    // the number of bits each level of the tree uses
    public static final int maskBits = 6;
    // a mask with maskBits number of 1s.  It will be shifted according to depth in the tree in order to extract
    // some of the hash bits.
    private static final long mask = (1L << maskBits) - 1;
    @SuppressWarnings("unchecked")
    private static final Comparator<Comparable> natural = Comparator.naturalOrder();
    public final static Comparator<Comparable> nullFriendlyComparator = Comparator.nullsLast(natural);

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

    public static int extractHashPart64(final long hash, final int place) {
        assert place >= 0;

        return (int) ((hash & (mask << place)) >>> place);
    }

    public static <T> T[] arrayInsert(final T[] array, final int position, final T value) {
        assert array != null;
        assert position <= array.length;
        assert position >= 0;

        final T[] nodes = Arrays.copyOf(array, array.length + 1);
        System.arraycopy(nodes, position, nodes, position + 1, array.length - position);
        nodes[position] = value;
        return nodes;
    }

    public static <T> T[] arrayRemove(final T[] array, final int position) {
        assert array != null;
        assert position < array.length;
        assert position >= 0;

        final T[] nodes = Arrays.copyOf(array, array.length - 1);
        System.arraycopy(array, position + 1, nodes, position, array.length - position - 1);
        return nodes;
    }

    public static <T> T[] arrayReplace(final T[] array, final int position, final T value) {
        assert array != null;
        assert position < array.length;
        assert position >= 0;

        if (array[position] == value) {
            return array;
        }
        final T[] nodes = Arrays.copyOf(array, array.length);
        nodes[position] = value;
        return nodes;
    }
}