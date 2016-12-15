package hamt.mutable;

import hamt.Utils;
import hamt.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Table<Key extends Comparable<Key>, Value> implements Node<Key, Value> {
    private long population;
    private final List<Node<Key, Value>> children;

    /*
     * Do not modify the children array once it has been passed in.
     * Children must not be empty.
     * The population must be correctly set to describe the position of the children.
     */
    private Table(final long population, Node<Key, Value>... children) {
        assert Long.bitCount(population) == children.length;
        assert children.length > 0;

        this.population = population;
        this.children = new ArrayList<>(Arrays.asList(children));
    }

    @SuppressWarnings("unchecked")
    static <Key extends Comparable<Key>, Value> Table<Key, Value> fromSingleNode(final long hash, final int place, final Node<Key, Value> node) {
        assert node != null;
        final int newPartHash = Utils.extractHashPart64(hash, place);
        return new Table(1L << newPartHash, node);
    }

    @Override
    public Value get(final long hash, final int place, final Key key, final Value notPresent) {
        final int partHash = Utils.extractHashPart64(hash, place);
        final int realIndex = Utils.index64(population, partHash);
        if (realIndex < 0) {
            return notPresent;
        }
        final Node<Key, Value> newNode = children.get(realIndex);
        return newNode.get(hash, place - Utils.maskBits, key, notPresent);
    }

    @Override
    public Table<Key, Value> set(final long hash, final int place, final Key key, final Value value) {
        final int newPartHash = Utils.extractHashPart64(hash, place);
        final int realIndex = Utils.index64(population, newPartHash);
        if (realIndex < 0) {
            final int newLocation = -realIndex - 1;
            population |= 1L << newPartHash;
            children.add(newLocation, new Entry<>(hash, key, value));
        } else {
            final Node<Key, Value> newNode = children.get(realIndex).set(hash, place - Utils.maskBits, key, value);
            children.set(realIndex, newNode);
        }
        return this;
    }

    @Override
    public Node<Key, Value> remove(final long hash, final int place, final Key key) {
        final int newPartHash = Utils.extractHashPart64(hash, place);
        final int realIndex = Utils.index64(population, newPartHash);
        final long popPos = 1L << newPartHash;
        if (realIndex >= 0) {
            final Node<Key, Value> newNode = children.get(realIndex).remove(hash, place - Utils.maskBits, key);
            if (newNode != null) {
                children.set(realIndex, newNode);
                return this;
            } else {
                if (children.size() == 1) {
                    return null;
                }
                children.remove(realIndex);
                if (children.size() == 1 && !(children.get(0) instanceof Table)) {
                    return children.get(0);
                }
                population &= ~popPos;
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return "Table{" +
                "population=" + Long.toBinaryString(population) +
                ", children=" + children.size() + ":" + children +
                '}';
    }
}
