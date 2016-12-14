package hamt.struct;

import java.util.Arrays;

public class Node32<Key, Value> {
    NodeType type;
    int hash;
    Key key;
    Value value;
    Node32<Key, Value>[] children;
    int population;

    private Node32(NodeType type, int hash, Key key, Value value, Node32<Key, Value>[] children, int population) {
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

    void mutateEntry(int hash, Key key, Value value) {
        this.type = NodeType.Entry;
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.children = null;
        this.population = 0;
    }

    void mutateCollision(int hash, Node32<Key, Value>[] entries) {
        this.type = NodeType.Collision;
        this.hash = hash;
        this.key = null;
        this.value = null;
        this.children = entries;
        this.population = 0;
    }

    void mutateTree(int population, Node32<Key, Value>[] nodes) {
        this.type = NodeType.Tree;
        this.hash = 0;
        this.key = null;
        this.value = null;
        this.children = nodes;
        this.population = population;
    }

    void mutateCopy(Node32<Key, Value> other) {
        this.type = other.type;
        hash = other.hash;
        key = other.key;
        value = other.value;
        children = other.children;
        population = other.population;
    }

    public static <Key, Value> Node32<Key, Value> Entry(int hash, Key key, Value value) {
        if (key == null) {
            throw new NullPointerException("Key may not be null.");
        }
        return new Node32(NodeType.Entry, hash, key, value, null, 0);
    }

    static <Key, Value> Node32<Key, Value> Collision(int hash, Node32<Key, Value>[] entries) {
        for (Node32<Key, Value> entry : entries) {
            if (entry.type != NodeType.Entry) {
                throw new RuntimeException("Members of a collision must be entries, node " + entry + " may not be a member of a collision.");
            }
            if (entry.hash != hash) {
                throw new RuntimeException("Members of a collision must share a hash, node " + entry + " had hash " + entry.hash + " which is different from " + hash);
            }
        }
        return new Node32(NodeType.Collision, hash, null, null, entries, 0);
    }

    public static <Key, Value> Node32<Key, Value> Tree(int population, Node32<Key, Value>[] nodes) {
        if (Integer.bitCount(population) != nodes.length) {
            throw new RuntimeException("Tree had " + nodes.length + " children, but population " + Integer.toBinaryString(population) + " only shows " + Integer.bitCount(population) + ".");
        }
        return new Node32(NodeType.Tree, 0, null, null, nodes, population);
    }

    public static <Key, Value> Node32<Key, Value> Tree() {
        return Tree(0, new Node32[0]);
    }

    @Override
    public String toString() {
        switch (type) {
            case Entry:
                return "Entry(" + key  + " (#" + hash + ") " + value + ")";
            case Collision:
                return "Entries(hash=" + hash + ", length=" + children.length + ") " + Arrays.asList(children);
            case Tree:
                return "Tree(population=" + Integer.toBinaryString(population) + ") " + Arrays.asList(children);
        }
        throw new RuntimeException("Invalid node type: " + type);
    }

    enum NodeType { Entry, Collision, Tree }
}
