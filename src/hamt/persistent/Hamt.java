package hamt.persistent;

import hamt.Node;
import hamt.Utils;

import java.util.Arrays;
import java.util.function.Function;

/*
 * Paper at http://lampwww.epfl.ch/papers/idealhashtrees.pdf
 * TODO: automatic root table resizing
 * TODO: pool unneeded entries and nodes
 * TODO: rehash instead of collision bucket
 */
public final class Hamt<Key extends Comparable<Key>, Value> {
    private static final int defaultTopLevelBits = 10;

    private final int topLevelBits;
    private final int nextPlace;

    private final Function<Key, Long> hashFunction;
    private final Node<Key, Value>[] nodes;

    @SuppressWarnings("unchecked")
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
        this(hashFunction, topLevelBits, new Node[1 << topLevelBits]);
    }

    private Hamt(final Function<Key, Long> hashFunction, final int topLevelBits, final Node<Key, Value>[] nodes) {
        assert topLevelBits >= 0;
        assert topLevelBits <= 64;
        assert nodes.length == 1 << topLevelBits;
        if (hashFunction == null) {
            throw new NullPointerException("If you wish to use a default hash function, call the 0-arg constructor.");
        }

        this.hashFunction = hashFunction;
        this.topLevelBits = topLevelBits;
        this.nextPlace = 64 - topLevelBits - Utils.maskBits64;
        this.nodes = nodes;
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * @see #get(Key, Value)
     *
     * @param key the key to search for.
     * @return the value associated with the provided key or {@code null}
     *          if no value is associated with the key.
     */
    public Value get(final Key key) {
        return get(key, null);
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or the {@code notPresent} value if this map contains no mapping
     * for the key.
     *
     * @see #put(Key, Value)
     *
     * @param key the key to search for.
     * @param notPresent the value to return if the key is not mapped in
     *                  this hamt.
     */
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

    /**
     * Associates a value with a key into a new hamt. If the map previously
     * contained a mapping for the key, the old value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return a new Hamt containing the association.
     */
    public Hamt<Key, Value> put(final Key key, final Value value) {
        final long hash = hashFunction.apply(key);
        final int realIndex = getIndex(hash);
        final Node<Key, Value> node = nodes[realIndex];
        if (node == null) {
            return withNodes(Utils.arrayReplace(nodes, realIndex, new Entry<>(hash, key, value)));
        } else {
            return withNodes(Utils.arrayReplace(nodes, realIndex, node.set(hash, nextPlace, key, value)));
        }
    }

    public Hamt<Key, Value> remove(final Key key) {
        final long hash = hashFunction.apply(key);
        final int realIndex = getIndex(hash);
        final Node<Key, Value> node = nodes[realIndex];
        if (node != null) {
            return withNodes(Utils.arrayReplace(nodes, realIndex, node.remove(hash, nextPlace, key)));
        }
        return this;
    }

    @Override
    public String toString() {
        return "Hamt{" +
                "nodes=" + nodes.length + ":" + Arrays.toString(nodes) +
                '}';
    }

    private int getIndex(final long hash) {
        if (topLevelBits == 0) {
            return 0;
        }
        return (int) (hash >>> (64 - topLevelBits));
    }

    private Hamt<Key, Value> withNodes(Node<Key, Value>[] newNodes) {
        return new Hamt<>(hashFunction, topLevelBits, newNodes);
    }
}