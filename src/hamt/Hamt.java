package hamt;

import java.util.Arrays;
import java.util.function.Function;

public class Hamt<Key extends Comparable<Key>, Value> {
    private static final int topLevelBits = 10;
    private static final int nextPlace = 64 - topLevelBits - Utils.maskBits;

    private final Function<Key, Long> hasher;
    private @SuppressWarnings("unchecked") final Node<Key, Value>[] nodes = new Node[1 << topLevelBits];

    public Hamt() {
        this(key -> key == null ? 0L : Long.reverseBytes(key.hashCode()));
    }

    public Hamt(final Function<Key, Long> hashFunction) {
        if (hashFunction == null) {
            throw new NullPointerException("Must provide a hash function.");
        }
        this.hasher = hashFunction;
    }

    private static int getIndex(final long hash) {
        return (int) (hash >>> (64 - topLevelBits));
    }

    public Value get(final Key key) {
        return get(key, null);
    }

    public Value get(final Key key, final Value notPresent) {
        final long hash = hasher.apply(key);
        final int realIndex = getIndex(hash);
        final Node<Key, Value> node = nodes[realIndex];
        if (node == null) {
            return notPresent;
        }
        return node.get(hash, nextPlace, key, notPresent);
    }

    public boolean put(final Key key, final Value value) {
        final long hash = hasher.apply(key);
        final int realIndex = getIndex(hash);
        final Node<Key, Value> node = nodes[realIndex];
        if (node == null) {
            nodes[realIndex] = new Entry<>(hash, key, value);
        } else {
            nodes[realIndex] = node.set(hash, nextPlace, key, value);
        }
        return node != nodes[realIndex];
    }

    public boolean remove(final Key key) {
        final long hash = hasher.apply(key);
        final int realIndex = getIndex(hash);
        final Node<Key, Value> node = nodes[realIndex];
        if (node != null) {
            nodes[realIndex] = node.remove(hash, nextPlace, key);
        }
        return node != nodes[realIndex];
    }

    @Override
    public String toString() {
        return "Hamt{" +
                "  nodes=" + Arrays.toString(nodes) +
                '}';
    }
}
