package hamt

import java.util.function.Function

class HamtUtilsTest extends GroovyTestCase {
    void testIndex32() {
        println "index32 Test"
        assert Utils.index32(0b0000_0000_0000_0000_0000_0000_0000_0000i, 0) == -1
        assert Utils.index32(0b0000_0000_0000_0000_0000_0000_0000_0001i, 0) == 0
        assert Utils.index32(0b0000_0000_0000_0000_0000_0000_0000_0010i, 0) == -1
        assert Utils.index32(0b0000_0000_0000_0000_0000_0000_0000_0010i, 1) == 0
        assert Utils.index32(0b0000_0000_0000_0000_0000_0000_0000_0011i, 1) == 1
        assert Utils.index32(0b0000_0000_0000_0000_0000_0000_0000_0100i, 0) == -1
        assert Utils.index32(0b0000_0000_0000_0000_0000_0000_0000_0100i, 1) == -1
        assert Utils.index32(0b0000_0000_0000_0000_0000_0000_0000_0100i, 2) == 0
        assert Utils.index32(0b0000_0000_0000_0000_0000_0000_0000_0101i, 0) == 0
        assert Utils.index32(0b0000_0000_0000_0000_0000_0000_0000_0101i, 1) == -2
        assert Utils.index32(0b0000_0000_0000_0000_0000_0000_0000_0101i, 2) == 1
        assert Utils.index32(0b0000_0000_0000_0000_0000_0000_0000_1100i, 0) == -1
        assert Utils.index32(0b0000_0000_0000_0000_0000_0000_0000_1100i, 1) == -1
        assert Utils.index32(0b0000_0000_0000_0000_0000_0000_0000_1100i, 2) == 0
        assert Utils.index32(0b1000_0000_0000_0000_0000_0000_0000_1100i, 31) == 2
    }

    void testIndex64() {
        println "index64 Test"
        assert Utils.index64(0b0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000l, 0) == -1
        assert Utils.index64(0b0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000l, 63) == -1
        assert Utils.index64(0b1000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000l, 63) == 0
        assert Utils.index64(0b1000_0000_0100_0000_0000_0100_0000_0000_0000_0000_0000_0000_0000_0100_0000_0100l, 63) == 4
        assert Utils.index64(0b0000_0000_0100_0000_0000_0100_0000_0000_0000_0000_0000_0000_0000_0100_0000_0100l, 63) == -5
    }

    void testObjRem() {
        def seed = new Date().getTime()
        def rnd = new Random(seed)
        println "Random obj Test with seed $seed"
        def tree = new Hamt<Double, String>()

        def data = [:]
        for (number in 0..1000) {
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

    void testBadHasherObjRem() {
        def seed = new Date().getTime()
        def rnd = new Random(seed)
        println "Bad hash (exercise collisions) obj Test with seed $seed"
        def tree = new Hamt<Double, String>({ d -> Double.doubleToLongBits(d) >>> 45 } as Function<Double, Long>)

        def data = [:]
        for (number in 0..10000) {
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

    void testNullObj() {
        def tree = new Hamt<Double, String>()

        tree.put(null, "null or thereabouts")

        assert tree.get(null) == "null or thereabouts"

        tree.remove(null)

        assert tree.get(null) == null
    }
}
