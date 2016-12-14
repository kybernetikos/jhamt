package hamt.struct;

import hamt.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HamtUtils32 {

    @SuppressWarnings("unchecked")
    static <Key, Value> Node32<Key, Value>[] delete(Node32<Key, Value>[] array, int index) {
        final List<Node32<Key, Value>> result = new ArrayList<>(Arrays.asList(array));
        result.remove(index);
        return (Node32<Key, Value>[]) result.toArray(new Node32[result.size()]);
    }

    public static <Key, Value> void set(Node32<Key, Value> tree, Key key, Value value) {
        set(tree, key.hashCode(), 5*6, key, value);
    }

    public static <Key, Value> void remove(Node32<Key, Value> node, Key key) {
        remove(node, key.hashCode(), 5*6, key);
    }

    @SuppressWarnings("unchecked")
    static <Key, Value> void remove(Node32<Key, Value> node, int hash, int place, Key key) {
        switch (node.type) {
            case Entry:
                if (node.key.equals(key)) {
                    System.err.println("Removing from entry " + key);
                    node.mutateTree(0, new Node32[0]);
                }
                break;
            case Collision:
                if (node.hash == hash) {
                    final Node32<Key, Value>[] newChildren = (Node32<Key, Value>[]) Arrays.stream(node.children).filter((entry) -> !entry.key.equals(key)).toArray();
                    if (newChildren.length == 1) {
                        node.mutateEntry(hash, newChildren[0].key, newChildren[0].value);
                    } else if (newChildren.length < node.children.length) {
                        node.mutateCollision(hash, newChildren);
                    }
                }
                break;
            case Tree:
                final int newPartHash = (hash & (0b11111 << place)) >>> place;
                final int realIndex = Utils.index32(node.population, newPartHash);
                final int popPos = 1 << newPartHash;
                if (realIndex >= 0) {
                    final Node32<Key, Value> newNode = node.children[realIndex];
                    switch (newNode.type) {
                        case Entry:
                            if (newNode.key.equals(key)) {
                                final Node32<Key, Value>[] newChildren = delete(node.children, realIndex);
                                int newPopulation = (~popPos) & node.population;
                                if (Integer.bitCount(newPopulation) == 1 && newChildren[0].type != Node32.NodeType.Tree) {
                                    node.mutateCopy(newChildren[0]);
                                } else {
                                    node.mutateTree(newPopulation, newChildren);
                                }
                            }
                            break;
                        case Collision:
                        case Tree:
                            remove(newNode, hash, place - 5, key);
                    }
                }
        }
    }

    @SuppressWarnings("unchecked")
    static <Key, Value> void set(Node32<Key, Value> node, int hash, int place, Key key, Value value) {
        switch (node.type) {
            case Entry:
                if (hash == node.hash) {
                    if (key.equals(node.key)) {
                        node.mutateEntry(hash, key, value);
                    } else {
                        node.mutateCollision(hash, new Node32[] {Node32.Entry(hash, node.key, node.value), Node32.Entry(hash, key, value)});
                    }
                    return;
                } else {
                    final int newPartHash = (node.hash & (0b11111 << place)) >>> place;
                    final Node32<Key, Value> result = Node32.Tree(1 << newPartHash, new Node32[] {Node32.Entry(node.hash, node.key, node.value)});
                    set(result, hash, place, key, value);
                    node.mutateTree(result.population, result.children);
                    return;
                }
            case Collision:
                if (node.hash == hash) {
                    for (Node32<Key, Value> n : node.children) {
                        if (n.key.equals(key)) {
                            n.mutateEntry(hash, key, value);
                            return;
                        }
                    }
                    Node32<Key, Value>[] nodes = (Node32<Key, Value>[]) new Node32[node.children.length + 1];
                    System.arraycopy(node.children, 0, nodes, 0, node.children.length);
                    nodes[node.children.length] = Node32.Entry(hash, key, value);
                    node.mutateCollision(hash, nodes);
                    return;
                } else {
                    final int newPartHash = (node.hash & (0b11111 << (place - 5))) >>> (place - 5);
                    final Node32<Key, Value> result = Node32.Tree(1 << newPartHash, new Node32[] {Node32.Collision(node.hash, node.children)});
                    set(result, hash, place - 5, key, value);
                    node.mutateTree(result.population, result.children);
                    return;
                }
            case Tree:
                final int newPartHash = (hash & (0b11111 << place)) >>> place;
                final int realIndex = Utils.index32(node.population, newPartHash);
                if (realIndex < 0) {
                    final int newLocation = -realIndex - 1;
                    final Node32<Key, Value>[] nodes = new Node32[node.children.length + 1];
                    System.arraycopy(node.children, 0, nodes, 0, newLocation);
                    nodes[newLocation] = Node32.Entry(hash, key, value);
                    System.arraycopy(node.children, newLocation, nodes, newLocation + 1, node.children.length - newLocation);
                    node.mutateTree(node.population | (1 << newPartHash), nodes);
                } else {
                    set(node.children[realIndex], hash, place - 5, key, value);
                }
        }
    }

    public static <Key, Value> Value get(Node32<Key, Value> tree, Key key) {
        return get(tree, key.hashCode(), key);
    }

    public static <Key, Value> Value get(Node32<Key, Value> tree, int hash, Key key) {
        return get(tree, hash, 5 * 6, key, null);
    }

    static <Key, Value> Value get(Node32<Key, Value> node, int hash, int place, Key key, Value missingValue) {
        switch (node.type) {
            case Entry:
                if (node.hash == hash && node.key.equals(key)) {
                    return node.value;
                }
                break;
            case Collision:
                if (node.hash == hash) {
                    for (Node32<Key, Value> child : node.children) {
                        if (child.key.equals(key)) {
                            return child.value;
                        }
                    }
                }
                break;
            case Tree:
                final int partHash = (hash & (0b11111 << place)) >>> place;
                final int population = node.population;
                final int realIndex = Utils.index32(population, partHash);
                if (realIndex < 0) {
                    return missingValue;
                }
                final Node32<Key, Value> newNode = node.children[realIndex];
                return get(newNode, hash, place - 5, key, missingValue);
        }
        return missingValue;
    }
}
