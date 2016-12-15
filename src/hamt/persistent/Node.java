package hamt.persistent;

/*
 * A Node can be either a single or multiple Key/Value Entries.
 * There are two kinds of multiple Entries: Tables, which stores other Tables or Nodes containing different hashes and
 * Collisions which stores Entries with the same hash.
 */
interface Node<Key extends Comparable<Key>, Value> {
    Value get(long hash, int place, Key key, Value notPresent);

    Node<Key, Value> set(long hash, int place, Key key, Value value);

    Node<Key, Value> remove(long hash, int place, Key key);
}
