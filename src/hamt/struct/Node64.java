package hamt.struct;

import java.util.Arrays;

public class Node64<Key, Value> {
    NodeType type;
    long hash;
    Key key;
    Value value;
    Node64<Key, Value>[] children;
    long population;

    private Node64(NodeType type, long hash, Key key, Value value, Node64<Key, Value>[] children, long population) {
        if (type == null) {
            throw new NullPointerException("hamt.Node type may not be null");
        }
        this.type = type;
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.children = children;
        this.population = population;
    }

    void mutateEntry(long hash, Key key, Value value) {
        this.type = NodeType.Entry;
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.children = null;
        this.population = 0;
    }

    void mutateCollision(long hash, Node64<Key, Value>[] entries) {
        this.type = NodeType.Collision;
        this.hash = hash;
        this.key = null;
        this.value = null;
        this.children = entries;
        this.population = 0;
    }

    void mutateTree(long population, Node64<Key, Value>[] nodes) {
        this.type = NodeType.Tree;
        this.hash = 0;
        this.key = null;
        this.value = null;
        this.children = nodes;
        this.population = population;
    }

    void mutateCopy(Node64<Key, Value> other) {
        this.type = other.type;
        hash = other.hash;
        key = other.key;
        value = other.value;
        children = other.children;
        population = other.population;
    }

    static <Key, Value> Node64<Key, Value> Entry(long hash, Key key, Value value) {
        if (key == null) {
            throw new NullPointerException("Key may not be null.");
        }
        return new Node64(NodeType.Entry, hash, key, value, null, 0);
    }

    static <Key, Value> Node64<Key, Value> Collision(long hash, Node64<Key, Value>[] entries) {
        for (Node64<Key, Value> entry : entries) {
            if (entry.type != NodeType.Entry) {
                throw new RuntimeException("Members of a collision must be entries, node " + entry + " may not be a member of a collision.");
            }
            if (entry.hash != hash) {
                throw new RuntimeException("Members of a collision must share a hash, node " + entry + " had hash " + entry.hash + " which is different from " + hash);
            }
        }
        return new Node64(NodeType.Collision, hash, null, null, entries, 0);
    }

    public static <Key, Value> Node64<Key, Value> Tree(long population, Node64<Key, Value>[] nodes) {
        if (Long.bitCount(population) != nodes.length) {
            throw new RuntimeException("Tree had " + nodes.length + " children, but population " + Long.toBinaryString(population) + " only shows " + Long.bitCount(population) + ".");
        }
        return new Node64(NodeType.Tree, 0, null, null, nodes, population);
    }

    static <Key, Value> Node64<Key, Value> Tree() {
        return Tree(0, new Node64[0]);
    }

    @Override
    public String toString() {
        switch (type) {
            case Entry:
                return "Entry(" + key  + " (#" + hash + ") " + value + ")";
            case Collision:
                return "Entries(hash=" + hash + ", length=" + children.length + ") " + Arrays.asList(children);
            case Tree:
                return "Tree(population=" + Long.toBinaryString(population) + ") " + Arrays.asList(children);
        }
        throw new RuntimeException("Invalid node type: " + type);
    }

    enum NodeType { Entry, Collision, Tree }
}
