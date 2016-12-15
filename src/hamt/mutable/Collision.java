package hamt.mutable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import hamt.Node;

/*
 * A collision stores multiple entries that all share the same hash.
 */
class Collision<Key extends Comparable<Key>, Value> implements Node<Key, Value> {

    private final long hash;
    private final List<Entry<Key, Value>> children;

    /*
     * Children must be currently sorted, and all entries within it must have the same hash as the provided hash.
     * It must always contain at least two items, since otherwise a null (0) or plain Entry (1) should be used.
     * Do not modify the children array after passing it into a Collision.
     */
    @SafeVarargs
    Collision(final long hash, final Entry<Key, Value>... children) {
        assert children.length >= 2;

        this.hash = hash;
        this.children = new ArrayList<>(Arrays.asList(children));
        this.children.sort(Entry.keyComparator);
    }

    @Override
    public Value get(final long hash, final int place, final Key key, final Value notPresent) {
        if (hash != this.hash) {
            return notPresent;
        }
        final int index = Collections.binarySearch(children, new Entry<>(hash, key, null), Entry.keyComparator);

        if (index >= 0) {
            return children.get(index).getValue();
        }
        return notPresent;
    }

    @Override
    public Node<Key, Value> set(final long hash, final int place, final Key key, final Value value) {
        if (hash != this.hash) {
            return Table.fromSingleNode(this.hash, place, this).set(hash, place, key, value);
        }
        final Entry<Key, Value> entry = new Entry<>(hash, key, value);
        final int index = Collections.binarySearch(children, entry, Entry.keyComparator);
        if (index >= 0) {
            final Value oldValue = children.get(index).getValue();
            if (oldValue != value) {
                children.set(index, entry);
            }
        } else {
            children.add(-index-1, entry);
        }
        return this;
    }

    @Override
    public Node<Key, Value> remove(final long hash, final int place, final Key key) {
        if (hash != this.hash) {
            return this;
        }
        final Entry<Key, Value> entry = new Entry<>(hash, key, null);
        final int index = Collections.binarySearch(children, entry, Entry.keyComparator);
        if (index < 0) {
            return this;
        }
        if (children.size() == 2) {
            return children.get(1 - index);
        }
        children.remove(index);
        return this;
    }

    @Override
    public String toString() {
        return "Collision{" +
                "hash=" + hash +
                ", children=" + children.size() + ":" + children +
                '}';
    }
}