import hamt.Utils
import hamt.oo.Hamt
import hamt.struct.HamtUtils32
import hamt.struct.HamtUtils64
import hamt.struct.Node32
import hamt.struct.Node64

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

    void testGet32() {
        println "Get32 Test"
        final item = Node32.Entry(587, "a", "hello")
        final tree = Node32.Tree(0b0000_0000_0000_0000_0000_0000_0000_0001i, (Node32<String, String>[]) [item].toArray())
        final result = HamtUtils32.get(tree, 587, "a")
        assert result == "hello"
    }

    void testGetSet32() {
        println "Get/Set32 Test"
        final tree = Node32.Tree()
        HamtUtils32.set(tree, "hello", "world")
        HamtUtils32.set(tree, "goodbye", "everyone")

        assert HamtUtils32.get(tree, "hello") == "world"
        assert HamtUtils32.get(tree, "goodbye") == "everyone"

        HamtUtils32.set(tree, "goodbye", "boom")

        assert HamtUtils32.get(tree, "goodbye") == "boom"
    }

    void testGetSet64() {
        println "Get/Set64 Test"
        final tree = Node64.Tree(0L, new Node64<String, String>[0])
        HamtUtils64.set(tree, "hello", "world")
        HamtUtils64.set(tree, "goodbye", "everyone")

        assert HamtUtils64.get(tree, "hello") == "world"
        assert HamtUtils64.get(tree, "goodbye") == "everyone"

        HamtUtils64.set(tree, "goodbye", "boom")

        assert HamtUtils64.get(tree, "goodbye") == "boom"
    }

    void testRandom32() {
        def seed = new Date().getTime()
        println "Random 32 Test with seed $seed"
        def rnd = new Random(seed)

        def data = [:]
        final tree = Node32.Tree()
        for (number in 0..800) {
            def key = rnd.nextDouble()
            def value = rnd.nextDouble()
            data[key] = value
            HamtUtils32.set(tree, key, value)
        }

        for (key in data.keySet()) {
            assert HamtUtils32.get(tree, key) == data[key]
        }
    }

    void testRemove32() {
        def seed = new Date().getTime()
        def rnd = new Random(seed)
        println "Random32 Removal Test with seed $seed"
        final tree = Node32.Tree()

        def data = [:]
        for (number in 0..800) {
            def key = rnd.nextDouble()
            data[key] = "a"
            HamtUtils32.set(tree, key, "a")
        }

        for (key in data.keySet()) {
            assert HamtUtils32.get(tree, key) == "a"
            HamtUtils32.remove(tree, key)
            assert HamtUtils32.get(tree, key) == null
        }
    }

    void testRemove64() {
        def seed = new Date().getTime()
        def rnd = new Random(seed)
        println "Random Removal64 Test with seed $seed"
        final tree = (Node64<Double, String>) Node64.Tree()

        def data = [:]
        for (number in 0..800) {
            def key = rnd.nextDouble()
            data[key] = "a"
            HamtUtils64.set(tree, key, "a")
        }

        for (key in data.keySet()) {
            assert HamtUtils64.get(tree, key) == "a"
            HamtUtils64.remove(tree, key)
            assert HamtUtils64.get(tree, key) == null
        }
    }

    void testObjRem() {
        def seed =  new Date().getTime()
        def rnd = new Random(seed)
        println "Random obj Test with seed $seed"
        def tree = new Hamt<Double, String>({ d -> Double.doubleToLongBits(d) } as Function<Double, Long>)

        def data = [:]
        for (number in 0..800) {
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
        def tree = new Hamt<Double, String>({ d -> Double.doubleToLongBits(d) >>> 55 } as Function<Double, Long>)

        def data = [:]
        for (number in 0..800) {
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
        def tree = new Hamt<Double, String>({ d -> 23L } as Function<Double, Long>)

        tree.put(0.2, "hello there")
        tree.put(null, "null or thereabouts")

        assert tree.get(null) == "null or thereabouts"

        tree.remove(null)

        assert tree.get(null) == null
    }
}
