package hamt;

import java.util.Comparator;

public class Entry<Key extends Comparable<Key>, Value> implements Node<Key, Value> {
    private final Comparator<Key> comparator = Comparator.nullsLast(Comparator.naturalOrder());
    private final long hash;
    private final Key key;
    private final Value value;

    Entry(final long hash, final Key key, final Value value) {
        this.hash = hash;
        this.key = key;
        this.value = value;
    }

    @Override
    public Value get(long hash, int place, Key key, Value notPresent) {
        if (hash != this.hash || comparator.compare(this.key, key) != 0) {
            return notPresent;
        }
        return value;
    }

    @Override
    public Node<Key, Value> set(final long hash, final int place, final Key key, final Value value) {
        if (hash == this.hash) {
            if (comparator.compare(this.key, key) == 0) {
                if (this.value == value) {
                    return this;
                }
                return new Entry<>(hash, key, value);
            } else {
                return Collision.fromEntries(hash, this, new Entry<>(hash, key, value));
            }
        }
        return Table.fromSingleNode(this.hash, place, this).set(hash, place, key, value);
    }

    @Override
    public Node<Key, Value> remove(final long hash, final int place, final Key key) {
        if (hash == this.hash && comparator.compare(this.key, key) == 0) {
            return null;
        }
        return this;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "hash=" + hash +
                ", key=" + key +
                ", value=" + value +
                '}';
    }

    Key getKey() {
        return key;
    }

    Value getValue() {
        return value;
    }
}
