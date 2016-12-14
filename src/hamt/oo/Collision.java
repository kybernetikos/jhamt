package hamt.oo;

import java.util.Arrays;
import java.util.Comparator;

public class Collision<Key extends Comparable<Key>, Value> implements Node<Key, Value> {

    private static final @SuppressWarnings("unchecked") Comparator<Entry> keyComparator = Comparator.comparing(Entry::getKey, Comparator.nullsLast(Comparator.naturalOrder()));

    private final long hash;
    private final Entry<Key, Value>[] children;

    @SuppressWarnings("unchecked")
    Collision(long hash, Entry<Key, Value> entry1, Entry<Key, Value> entry2) {
        this(hash, new Entry[] {entry1, entry2});
    }

    private Collision(long hash, Entry<Key, Value>[] children) {
        assert children.length >= 2;

        Arrays.sort(children, keyComparator);
        this.hash = hash;
        this.children = children;
    }

    @Override
    public Value get(long hash, int place, Key key, Value notPresent) {
        if (hash != this.hash) {
            return notPresent;
        }
        int index = Arrays.binarySearch(children, new Entry<>(hash, key, notPresent), keyComparator);
        if (index >= 0) {
            return children[index].getValue();
        }
        return notPresent;
    }

    @Override
    public Node<Key, Value> set(long hash, int place, Key key, Value value) {
       Entry<Key, Value> entry = new Entry<>(hash, key, value);
        if (hash == this.hash) {
            int index = Arrays.binarySearch(children, entry, keyComparator);
            if (index >= 0) {
                Entry<Key, Value>[] result = Arrays.copyOf(children, children.length);
                result[index] = entry;
                return new Collision<>(hash, result);
            } else {
                Entry<Key, Value>[] result = Arrays.copyOf(children, children.length + 1);
                result[children.length] = entry;
                Arrays.sort(result, keyComparator);
                return new Collision<>(hash, result);
            }
        } else {
            // already gone into the collision, so place has already been decremented
            return Table.fromSingleNode(this.hash, place, this).set(hash, place, key, value);
        }
    }

    @Override
    public Node<Key, Value> remove(long hash, int place, Key key) {
        if (hash != this.hash) {
            return this;
        }
        Entry<Key, Value> entry = new Entry<>(hash, key, null);
        int index = Arrays.binarySearch(children, entry, keyComparator);
        if (index < 0) {
            return this;
        }
        if (children.length == 2) {
            return children[1 - index];
        }
        Entry<Key, Value>[] newChildren = Arrays.copyOf(children, children.length - 1);
        System.arraycopy(children, index + 1, newChildren, index, children.length - index - 1);
        return new Collision<>(hash, newChildren);
    }

    @Override
    public String toString() {
        return "Collision{" +
                "hash=" + hash +
                ", children=" + Arrays.toString(children) +
                '}';
    }
}
