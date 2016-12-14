package hamt.oo;

interface Node<Key extends Comparable<Key>, Value> {
    Value get(long hash, int place, Key key, Value notPresent);

    Node<Key, Value> set(long hash, int place, Key key, Value value);

    Node<Key, Value> remove(long hash, int place, Key key);
}
