package hamt.oo;

import java.util.Arrays;
import java.util.function.Function;

public class Hamt<Key extends Comparable<Key>, Value> {
    private static final int topLevelBits = 4;
    private static final int nextPlace = 64 - topLevelBits - 6;

    private final Function<Key, Long> hasher;
    private @SuppressWarnings("unchecked") final Node<Key, Value>[] nodes = new Node[16];

    public Hamt(Function<Key, Long> hasher) {
        this.hasher = hasher;
    }

    public Value get(Key key) {
        return get(key, null);
    }

    public Value get(Key key, Value notPresent) {
        long hash = hasher.apply(key);
        int realIndex = getIndex(hash);
        Node<Key, Value> node = nodes[realIndex];
        if (node == null) {
            return notPresent;
        }
        return node.get(hash, nextPlace, key, notPresent);
    }

    public boolean put(Key key, Value value) {
        long hash = hasher.apply(key);
        int realIndex = getIndex(hash);
        Node<Key, Value> node = nodes[realIndex];
        if (node == null) {
            nodes[realIndex] = new Entry<>(hash, key, value);
        } else {
            nodes[realIndex] = node.set(hash, nextPlace, key, value);
        }
        return node != nodes[realIndex];
    }

    public boolean remove(Key key) {
        long hash = hasher.apply(key);
        int realIndex = getIndex(hash);
        Node<Key, Value> node = nodes[realIndex];
        if (node != null) {
            nodes[realIndex] = node.remove(hash, nextPlace, key);
        }
        return node != nodes[realIndex];
    }

    private static int getIndex(long hash) {
        return (int) (hash >>> (64 - topLevelBits));
    }

    @Override
    public String toString() {
        return "Hamt{" +
                "  nodes=" + Arrays.toString(nodes) +
                '}';
    }
}
