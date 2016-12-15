package hamt.mutable

import java.util.function.Function

class MutableHamtTest extends GroovyTestCase {
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

        doSomeAddAndRemoves(rnd, 800, tree)
    }

    void testNullObj() {
        println "Test with some null values"

        def tree = new Hamt<Double, String>()
        tree.put(null, "null or thereabouts")

        assert tree.get(null) == "null or thereabouts"

        tree.put(null, null)

        assert tree.get(null) == null
        assert tree.get(null, "empty") == null

        tree.remove(null)
        assert tree.get(null, "empty") == "empty"
    }

    void testWithWeirdTopLevelBits() {
        def seed = new Date().getTime()
        def rnd = new Random(seed)
        println "Top level with weird bits, seed $seed"

        for (topLevelBits in 0..10) {
            def tree = new Hamt<Double, String>({ a -> Long.reverseBytes(a.hashCode()) } as Function<Double, Long>, topLevelBits)
            doSomeAddAndRemoves(rnd, 400, tree)
        }
    }

    static void doSomeAddAndRemoves(rnd, num, tree) {
        def data = [:]
        for (number in 0..num) {
            def key = rnd.nextDouble()
            data[key] = "a"
            tree.put(key, "a")
        }

        for (key in data.keySet()) {
            assert tree.get((Double) key) == "a"
            tree.remove((Double) key)
            assert tree.get((Double) key) == null
        }
    }
}