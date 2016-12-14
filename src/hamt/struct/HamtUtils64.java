package hamt.struct;

import hamt.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HamtUtils64 {

    @SuppressWarnings("unchecked")
    static <Key, Value> Node64<Key, Value>[] delete(Node64<Key, Value>[] array, int index) {
        final List<Node64<Key, Value>> result = new ArrayList<>(Arrays.asList(array));
        result.remove(index);
        return (Node64<Key, Value>[]) result.toArray(new Node64[result.size()]);
    }

    public static <Key, Value> void set(Node64<Key, Value> tree, Key key, Value value) {
        set(tree, key.hashCode(), 6*10, key, value);
    }

    public static <Key, Value> void remove(Node64<Key, Value> node, Key key) {
        remove(node, key.hashCode(), 6*10, key);
    }

    @SuppressWarnings("unchecked")
    static <Key, Value> void remove(Node64<Key, Value> node, long hash, int place, Key key) {
        switch (node.type) {
            case Entry:
                if (node.key.equals(key)) {
                    System.err.println("Removing from entry " + key);
                    node.mutateTree(0, new Node64[0]);
                }
                break;
            case Collision:
                if (node.hash == hash) {
                    final Node64<Key, Value>[] newChildren = (Node64<Key, Value>[]) Arrays.stream(node.children).filter((entry) -> !entry.key.equals(key)).toArray();
                    if (newChildren.length == 1) {
                        node.mutateEntry(hash, newChildren[0].key, newChildren[0].value);
                    } else if (newChildren.length < node.children.length) {
                        node.mutateCollision(hash, newChildren);
                    }
                }
                break;
            case Tree:
                final int newPartHash = (int) ((hash & (0b111111L << place)) >>> place);
                final int realIndex = Utils.index64(node.population, newPartHash);
                final long popPos = 1L << newPartHash;
                if (realIndex >= 0) {
                    final Node64<Key, Value> newNode = node.children[realIndex];
                    switch (newNode.type) {
                        case Entry:
                            if (newNode.key.equals(key)) {
                                final Node64<Key, Value>[] newChildren = delete(node.children, realIndex);
                                long newPopulation = (~popPos) & node.population;
                                if (Long.bitCount(newPopulation) == 1 && newChildren[0].type != Node64.NodeType.Tree) {
                                    node.mutateCopy(newChildren[0]);
                                } else {
                                    node.mutateTree(newPopulation, newChildren);
                                }
                            }
                            break;
                        case Collision:
                        case Tree:
                            remove(newNode, hash, place - 6, key);
                    }
                }
        }
    }

    @SuppressWarnings("unchecked")
    static <Key, Value> void set(Node64<Key, Value> node, int hash, int place, Key key, Value value) {
        switch (node.type) {
            case Entry:
                if (hash == node.hash) {
                    if (key.equals(node.key)) {
                        node.mutateEntry(hash, key, value);
                    } else {
                        node.mutateCollision(hash, new Node64[] {Node64.Entry(hash, node.key, node.value), Node64.Entry(hash, key, value)});
                    }
                    return;
                } else {
                    final int newPartHash = (int) ((node.hash & (0b111111L << place)) >>> place);
                    final Node64<Key, Value> result = Node64.Tree(1L << newPartHash, new Node64[] {Node64.Entry(node.hash, node.key, node.value)});
                    set(result, hash, place, key, value);
                    node.mutateTree(result.population, result.children);
                    return;
                }
            case Collision:
                if (node.hash == hash) {
                    for (Node64<Key, Value> n : node.children) {
                        if (n.key.equals(key)) {
                            n.mutateEntry(hash, key, value);
                            return;
                        }
                    }
                    Node64<Key, Value>[] nodes = (Node64<Key, Value>[]) new Node64[node.children.length + 1];
                    System.arraycopy(node.children, 0, nodes, 0, node.children.length);
                    nodes[node.children.length] = Node64.Entry(hash, key, value);
                    node.mutateCollision(hash, nodes);
                    return;
                } else {
                    final long newPartHash = (node.hash & (0b111111L << (place - 6))) >>> (place - 6);
                    final Node64<Key, Value> result = Node64.Tree(1L << newPartHash, new Node64[] {Node64.Collision(node.hash, node.children)});
                    set(result, hash, place - 6, key, value);
                    node.mutateTree(result.population, result.children);
                    return;
                }
            case Tree:
                final int newPartHash = (int) ((hash & (0b111111L << place)) >>> place);
                final int realIndex = Utils.index64(node.population, newPartHash);
                if (realIndex < 0) {
                    final int newLocation = -realIndex - 1;
                    final Node64<Key, Value>[] nodes = new Node64[node.children.length + 1];
                    System.arraycopy(node.children, 0, nodes, 0, newLocation);
                    nodes[newLocation] = Node64.Entry(hash, key, value);
                    System.arraycopy(node.children, newLocation, nodes, newLocation + 1, node.children.length - newLocation);
                    node.mutateTree(node.population | (1L << newPartHash), nodes);
                } else {
                    set(node.children[realIndex], hash, place - 6, key, value);
                }
        }
    }

    public static <Key, Value> Value get(Node64<Key, Value> tree, Key key) {
        return get(tree, key.hashCode(), key);
    }

    static <Key, Value> Value get(Node64<Key, Value> tree, int hash, Key key) {
        return get(tree, hash, 6 * 10, key, null);
    }

    static <Key, Value> Value get(Node64<Key, Value> node, int hash, int place, Key key, Value missingValue) {
        switch (node.type) {
            case Entry:
                if (node.hash == hash && node.key.equals(key)) {
                    return node.value;
                }
                break;
            case Collision:
                if (node.hash == hash) {
                    for (Node64<Key, Value> child : node.children) {
                        if (child.key.equals(key)) {
                            return child.value;
                        }
                    }
                }
                break;
            case Tree:
                final int partHash = (int)((hash & (0b111111L << place)) >>> place);
                final long population = node.population;
                final int realIndex = Utils.index64(population, partHash);
                if (realIndex < 0) {
                    return missingValue;
                }
                final Node64<Key, Value> newNode = node.children[realIndex];
                return get(newNode, hash, place - 6, key, missingValue);
        }
        return missingValue;
    }

}
