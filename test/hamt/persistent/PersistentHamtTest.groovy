package hamt.persistent

import java.util.function.Function

class PersistentHamtTest extends GroovyTestCase {
    void testObjRem() {
        def seed = new Date().getTime()
        def rnd = new Random(seed)
        println "Random obj Test with seed $seed"
        def tree = new Hamt<Double, String>()
        doSomeAddAndRemoves(rnd, 1000, tree)
    }

    void testBadHasherObjRem() {
        def seed = new Date().getTime()
        def rnd = new Random(seed)
        println "Bad hash (exercise collisions) obj Test with seed $seed"
        def tree = new Hamt<Double, String>({ d -> Double.doubleToLongBits(d) >>> 45 } as Function<Double, Long>)
        doSomeAddAndRemoves(rnd, 10000, tree)
    }

    void testNullObj() {
        println "Test with some null values"

        def tree = new Hamt<Double, String>()
        tree = tree.put(null, "null or thereabouts")

        assert tree.get(null) == "null or thereabouts"

        tree = tree.put(null, null)

        assert tree.get(null) == null
        assert tree.get(null, "empty") == null

        tree = tree.remove(null)
        assert tree.get(null, "empty") == "empty"
    }

    void testWithWeirdTopLevelBits() {
        def seed = new Date().getTime()
        def rnd = new Random(seed)
        println "Top level with weird bits, seed $seed"

        for (topLevelBits in 0..10) {
            def tree = new Hamt<Double, String>({ a -> Long.reverseBytes(a.hashCode()) } as Function<Double, Long>, topLevelBits)
            doSomeAddAndRemoves(rnd, 500, tree)
        }
    }

    static void doSomeAddAndRemoves(rnd, num, tree) {
        def randomStrings = ["hello", "world", "marvellous", "things"]
        def data = [:]
        for (number in 0..num) {
            def key = rnd.nextDouble()
            def value = randomStrings.getAt(rnd.nextInt(randomStrings.size()))
            data[key] = value
            tree = tree.put(key, value)
        }

        for (key in data.keySet()) {
            assert tree.get((Double) key) == data[key]
            tree = tree.remove((Double) key)
            assert tree.get((Double) key) == null
        }
    }
}
