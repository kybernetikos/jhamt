package hamt.mutable;

import hamt.Utils;
import hamt.Node;
import java.util.Arrays;
import java.util.function.Function;

public class Hamt<Key extends Comparable<Key>, Value> {
    private static final int defaultTopLevelBits = 10;

    private final int topLevelBits;
    private final int nextPlace;

    private final Function<Key, Long> hashFunction;
    private final Node<Key, Value>[] nodes;

    public Hamt() {
        // I reverse the bytes because otherwise an int32 hashcode would leave all the most significant bits
        // of the hash empty, resulting in an unnecessarily nested structure.
        this(key -> key == null ? 0L : Long.reverseBytes(key.hashCode()));
    }

    public Hamt(final Function<Key, Long> hashFunction) {
        this(hashFunction, defaultTopLevelBits);
    }

    @SuppressWarnings("unchecked")
    public Hamt(final Function<Key, Long> hashFunction, final int topLevelBits) {
        assert topLevelBits >= 0;
        assert topLevelBits <= 64;
        if (hashFunction == null) {
            throw new NullPointerException("If you wish to use a default hash function, call the 0-arg constructor.");
        }

        this.hashFunction = hashFunction;
        this.topLevelBits = topLevelBits;
        this.nextPlace = 64 - topLevelBits - Utils.maskBits;
        this.nodes = new Node[1 << topLevelBits];
    }

    public Value get(final Key key) {
        return get(key, null);
    }

    @SuppressWarnings("Duplicates")
    public Value get(final Key key, final Value notPresent) {
        final long hash = hashFunction.apply(key);
        final int realIndex = getIndex(hash);
        final Node<Key, Value> node = nodes[realIndex];
        if (node == null) {
            return notPresent;
        }
        return node.get(hash, nextPlace, key, notPresent);
    }

    public void put(final Key key, final Value value) {
        final long hash = hashFunction.apply(key);
        final int realIndex = getIndex(hash);
        final Node<Key, Value> node = nodes[realIndex];
        if (node == null) {
            nodes[realIndex] = new Entry<>(hash, key, value);
        } else {
            nodes[realIndex] = node.set(hash, nextPlace, key, value);
        }
    }

    public void remove(final Key key) {
        final long hash = hashFunction.apply(key);
        final int realIndex = getIndex(hash);
        final Node<Key, Value> node = nodes[realIndex];
        if (node != null) {
            nodes[realIndex] = node.remove(hash, nextPlace, key);
        }
    }

    @Override
    public String toString() {
        return "Hamt{" +
                "  nodes=" + Arrays.toString(nodes) +
                '}';
    }

    private int getIndex(final long hash) {
        if (topLevelBits == 0) {
            return 0;
        }
        return (int) (hash >>> (64 - topLevelBits));
    }
}