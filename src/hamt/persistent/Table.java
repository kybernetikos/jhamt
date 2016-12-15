package hamt.persistent;

import java.util.Arrays;

public class Table<Key extends Comparable<Key>, Value> implements Node<Key, Value> {
    private final long population;
    private final Node<Key, Value>[] children;

    /*
     * Do not modify the children array once it has been passed in.
     * Children must not be empty.
     * The population must be correctly set to describe the position of the children.
     */
    private Table(final long population, final Node<Key, Value>[] children) {
        assert Long.bitCount(population) == children.length;
        assert children.length > 0;

        this.population = population;
        this.children = children;
    }

    @SuppressWarnings("unchecked")
    static <Key extends Comparable<Key>, Value> Table<Key, Value> fromSingleNode(final long hash, final int place, final Node<Key, Value> node) {
        assert node != null;

        final int newPartHash = Utils.extractHashPart(hash, place);
        return new Table(1L << newPartHash, new Node[]{node});
    }

    @Override
    public Value get(final long hash, final int place, final Key key, final Value notPresent) {
        final int partHash = Utils.extractHashPart(hash, place);
        final int realIndex = Utils.index64(population, partHash);
        if (realIndex < 0) {
            return notPresent;
        }
        final Node<Key, Value> newNode = children[realIndex];
        return newNode.get(hash, place - Utils.maskBits, key, notPresent);
    }

    @Override
    public Table<Key, Value> set(final long hash, final int place, final Key key, final Value value) {
        final int newPartHash = Utils.extractHashPart(hash, place);
        final int realIndex = Utils.index64(population, newPartHash);
        if (realIndex < 0) {
            final int newLocation = -realIndex - 1;
            final long newPopulation = population | (1L << newPartHash);
            return new Table<>(newPopulation, Utils.arrayInsert(children, newLocation, new Entry<>(hash, key, value)));
        } else {
            final Node<Key, Value> newNode = children[realIndex].set(hash, place - Utils.maskBits, key, value);
            if (children[realIndex] == newNode) {
                return this;
            }
            return new Table<>(population, Utils.arrayReplace(children, realIndex, newNode));
        }
    }

    @Override
    public Node<Key, Value> remove(final long hash, final int place, final Key key) {
        final int newPartHash = Utils.extractHashPart(hash, place);
        final int realIndex = Utils.index64(population, newPartHash);
        final long popPos = 1L << newPartHash;
        if (realIndex >= 0) {
            final Node<Key, Value> newNode = children[realIndex].remove(hash, place - Utils.maskBits, key);
            if (newNode != null) {
                return new Table<>(population, Utils.arrayReplace(children, realIndex, newNode));
            } else {
                final Node<Key, Value>[] newChildren = Utils.arrayRemove(children, realIndex);
                if (newChildren.length == 0) {
                    return null;
                }
                if (newChildren.length == 1 && !(newChildren[0] instanceof Table)) {
                    return newChildren[0];
                }
                final long newPopulation = (~popPos) & population;
                return new Table<>(newPopulation, newChildren);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return "Table{" +
                "population=" + Long.toBinaryString(population) +
                ", children=" + Arrays.toString(children) +
                '}';
    }
}
