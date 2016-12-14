package hamt.oo;

import hamt.Utils;

import java.util.Arrays;

public class Table<Key extends Comparable<Key>, Value> implements Node<Key, Value> {
    private static final int maskBits = 6;
    private static final long mask = (1L << maskBits) - 1;

    private final long population;
    private final Node<Key, Value>[] children;

    private Table(long population, Node<Key, Value>[] children) {
        assert children.length > 0;

        this.population = population;
        this.children = children;
    }

    private static int extractHashPart(long hash, int place) {
        return (int) ((hash & (mask << place)) >>> place);
    }

    @SuppressWarnings("unchecked")
    static <Key extends Comparable<Key>, Value> Table<Key, Value> fromSingleNode(long hash, int place, Node<Key, Value> node) {
        final int newPartHash = extractHashPart(hash, place);
        return new Table(1L << newPartHash, new Node[]{node});
    }

    @Override
    public Value get(long hash, int place, Key key, Value notPresent) {
        final int partHash = extractHashPart(hash, place);
        final int realIndex = Utils.index64(population, partHash);
        if (realIndex < 0) {
            return notPresent;
        }
        final Node<Key, Value> newNode = children[realIndex];
        return newNode.get(hash, place - maskBits, key, notPresent);
    }

    @Override
    public Table<Key, Value> set(long hash, int place, Key key, Value value) {
        final int newPartHash = extractHashPart(hash, place);
        final int realIndex = Utils.index64(population, newPartHash);
        if (realIndex < 0) {
            final int newLocation = -realIndex - 1;
            final Node<Key, Value>[] nodes = Arrays.copyOf(children, children.length + 1);
            System.arraycopy(nodes, newLocation, nodes, newLocation + 1, children.length - newLocation);
            nodes[newLocation] = new Entry<>(hash, key, value);
            return new Table<>(population | (1L << newPartHash), nodes);
        } else {
            final Node<Key, Value>[] nodes = Arrays.copyOf(children, children.length);
            nodes[realIndex] = nodes[realIndex].set(hash, place - maskBits, key, value);
            return new Table<>(population, nodes);
        }
    }

    @Override
    public Node<Key, Value> remove(long hash, int place, Key key) {
        final int newPartHash = extractHashPart(hash, place);
        final int realIndex = Utils.index64(population, newPartHash);
        final long popPos = 1L << newPartHash;
        if (realIndex >= 0) {
            final Node<Key, Value> newNode = children[realIndex].remove(hash, place - maskBits, key);
            if (newNode != null) {
                final Node<Key, Value>[] newChildren = Arrays.copyOf(children, children.length);
                newChildren[realIndex] = newNode;
                return new Table<>(population, newChildren);
            } else {
                final Node<Key, Value>[] newChildren = Arrays.copyOf(children, children.length - 1);
                System.arraycopy(children, realIndex + 1, newChildren, realIndex, children.length - realIndex - 1);
                if (newChildren.length == 0) {
                    return null;
                }
                if (newChildren.length == 1 && !(newChildren[0] instanceof Table)) {
                    return newChildren[0];
                }
                return new Table<>((~popPos) & population, newChildren);
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
