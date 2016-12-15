package hamt.persistent;

/*
 * An Entry stores the relationship between a key and value.
 */
public class Entry<Key extends Comparable<Key>, Value> implements Node<Key, Value> {
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
        if (hash != this.hash || !keyMatches(key)) {
            return notPresent;
        }
        return value;
    }

    @Override
    public Node<Key, Value> set(final long hash, final int place, final Key key, final Value value) {
        if (hash == this.hash) {
            if (keyMatches(key)) {
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
        if (hash == this.hash && keyMatches(key)) {
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

    private boolean keyMatches(final Key key) {
        return Utils.nullFriendlyComparator.compare(this.key, key) == 0;
    }
}
