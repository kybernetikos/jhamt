package hamt.persistent;

import hamt.Node;
import hamt.Utils;

import java.util.Arrays;

/*
 * A collision stores multiple entries that all share the same hash.
 */
public class Collision<Key extends Comparable<Key>, Value> implements Node<Key, Value> {

    private final long hash;
    private final Entry<Key, Value>[] children;

    /*
     * Children must be currently sorted, and all entries within it must have the same hash as the provided hash.
     * It must always contain at least two items, since otherwise a null (0) or plain Entry (1) should be used.
     * Do not modify the children array after passing it into a Collision.
     */
    private Collision(final long hash, final Entry<Key, Value>[] children) {
        assert children.length >= 2;

        this.hash = hash;
        this.children = children;
    }

    @SuppressWarnings("unchecked")
    static <Key extends Comparable<Key>, Value> Collision<Key, Value> fromEntries(final long hash, final Entry<Key, Value> entry1, final Entry<Key, Value> entry2) {
        assert entry1 != null;
        assert entry2 != null;

        final Entry[] children = new Entry[]{entry1, entry2};
        Arrays.sort(children, Entry.keyComparator);
        return new Collision(hash, children);
    }

    @Override
    public Value get(final long hash, final int place, final Key key, final Value notPresent) {
        if (hash != this.hash) {
            return notPresent;
        }
        final int index = Arrays.binarySearch(children, new Entry<>(hash, key, notPresent), Entry.keyComparator);
        if (index >= 0) {
            return children[index].getValue();
        }
        return notPresent;
    }

    @Override
    public Node<Key, Value> set(final long hash, final int place, final Key key, final Value value) {
        if (hash == this.hash) {
            final Entry<Key, Value> entry = new Entry<>(hash, key, value);
            final int index = Arrays.binarySearch(children, entry, Entry.keyComparator);
            if (index >= 0) {
                if (children[index].getValue() == value) {
                    return this;
                }
                return new Collision<>(hash, Utils.arrayReplace(children, index, entry));
            } else {
                return new Collision<>(hash, Utils.arrayInsert(children, -index - 1, entry));
            }
        } else {
            return Table.fromSingleNode(this.hash, place, this).set(hash, place, key, value);
        }
    }

    @Override
    public Node<Key, Value> remove(final long hash, final int place, final Key key) {
        if (hash != this.hash) {
            return this;
        }
        final Entry<Key, Value> entry = new Entry<>(hash, key, null);
        final int index = Arrays.binarySearch(children, entry, Entry.keyComparator);
        if (index < 0) {
            return this;
        }
        if (children.length == 2) {
            return children[1 - index];
        }
        return new Collision<>(hash, Utils.arrayRemove(children, index));
    }

    @Override
    public String toString() {
        return "Collision{" +
                "hash=" + hash +
                ", children=" + Arrays.toString(children) +
                '}';
    }
}