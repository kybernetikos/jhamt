package hamt.mutable

import java.util.function.Function

class MutableHamtTest extends GroovyTestCase {
    void testObjRem() {
        def seed = new Date().getTime()
        def rnd = new Random(seed)
        println "Mutable Random obj Test with seed $seed"
        def start = System.nanoTime()

        def tree = new Hamt<Double, String>()

        doSomeAddAndRemoves(rnd, 1000, tree)
        println ".. finished ${System.nanoTime() - start}"
    }

    void testBadHasherObjRem() {
        def seed = new Date().getTime()
        def rnd = new Random(seed)
        println "Mutable Bad hash (exercise collisions) obj Test with seed $seed"
        def start = System.nanoTime();

        def tree = new Hamt<Double, String>({ d -> Double.doubleToLongBits(d) >>> 45 } as Function<Double, Long>)

        doSomeAddAndRemoves(rnd, 800, tree)
        println ".. finished ${System.nanoTime() - start}"
    }

    void testNullObj() {
        println "Mutable Test with some null values"
        def start = System.nanoTime()

        def tree = new Hamt<Double, String>()
        tree.put(null, "null or thereabouts")

        assert tree.get(null) == "null or thereabouts"

        tree.put(null, null)

        assert tree.get(null) == null
        assert tree.get(null, "empty") == null

        tree.remove(null)
        assert tree.get(null, "empty") == "empty"

        println ".. finished ${System.nanoTime() - start}"
    }

    void testWithWeirdTopLevelBits() {
        def seed = new Date().getTime()
        def rnd = new Random(seed)
        println "Mutable Top level with weird bits, seed $seed"
        def start = System.nanoTime();

        for (topLevelBits in 0..10) {
            def tree = new Hamt<Double, String>({ a -> Long.reverseBytes(a.hashCode()) } as Function<Double, Long>, topLevelBits)
            doSomeAddAndRemoves(rnd, 400, tree)
        }

        println ".. finished ${System.nanoTime() - start}"
    }

    void doSomeAddAndRemoves(rnd, num, tree) {
        def randomStrings = ["hello", "world", "marvellous", "things"]
        def data = [:]
        for (number in 0..num) {
            def key = rnd.nextDouble()
            def value = randomStrings.getAt(rnd.nextInt(randomStrings.size()))
            data[key] = value
            tree.put(key, value)
        }

        for (key in data.keySet()) {
            assert tree.get((Double) key) == data[key]
            tree.remove((Double) key)
            assert tree.get((Double) key) == null
        }
    }
}