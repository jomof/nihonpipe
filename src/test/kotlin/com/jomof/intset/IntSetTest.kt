package com.jomof.intset

import com.google.common.truth.Truth.assertThat
import com.jomof.algorithm.combinations
import com.jomof.algorithm.permutations
import org.junit.Test
import java.io.*

class IntSetTest {

    @Test
    fun insertZero() {
        val set = intSetOf()
        set.add(0)
        assertThat(set.size).isEqualTo(1)
    }

    @Test
    fun iterate2() {
        val set = intSetOf(63)
        val list = set.toList()
        assertThat(list).isEqualTo(listOf(63))
    }

    @Test
    fun iterate3() {
        val set = intSetOf(63, 64)
        val list = set.toList()
        assertThat(list).isEqualTo(listOf(63, 64))
    }

    @Test
    fun minus1() {
        val bf1 = intSetOf(0..100)
        val bf2 = intSetOf(50..100)
        val result = bf1 minus bf2
        assertThat(result.contains(49)).isTrue()
        assertThat(result.contains(50)).isFalse()
        assertThat(result.contains(51)).isFalse()
    }

    @Test
    fun minus2() {
        val bf1 = intSetOf(0..100)
        val bf2 = intSetOf()
        val result = bf1 minus bf2
        assertThat(result.contains(49)).isTrue()
        assertThat(result.contains(50)).isTrue()
        assertThat(result.contains(51)).isTrue()
    }

    @Test
    fun maxDepth() {
        val set = intSetOf(0..8192 step 2)
        assertThat(set.maxDepth()).isEqualTo(1)
    }

    @Test
    fun iterate() {
        val set = intSetOf(1, 2, 3)
        val list = set.toList()
        assertThat(list).isEqualTo(listOf(1, 2, 3))
    }

    @Test
    fun maxDepthAllOnes() {
        val set = intSetOf()
        for (i in 0 until (1024 * 1024)) {
            set +=i
            assertThat(set.size).isEqualTo(i + 1)
            assertThat(set.maxDepth()).isLessThan(3)
        }
        assertThat(set.maxDepth()).isEqualTo(1)
    }

    @Test
    fun maxDepthAllOnesReverse() {
        val set = intSetOf()
        val max = (1024 * 1024 - 1)
        var count = 0
        for (i in max downTo 0) {
            ++ count
            set += i
            assertThat(set.size).isEqualTo(count)
            assertThat(set.maxDepth()).isLessThan(3)
        }
        assertThat(set.maxDepth()).isEqualTo(1)
    }

    @Test
    fun reproStackOverflow() {
        createPairNode(
                LongPageNode(0, arrayOf(1L, 2L, 3L)),  // 0..2
                PairNode(AllSetNode(PageRange(3,5)),   // 3..5
                        LongPageNode(6, arrayOf(4L)))) // 6..6
    }

    @Test
    fun allSetCombines() {
        val allSet = intSetOf(0..63)
        allSet += 64
        assertThat(allSet.maxDepth()).isEqualTo(2)
    }

    @Test
    fun maxDepthAllOnesInterleaved() {
        val set = intSetOf()
        val max = (128 * 128 - 1)
        for ((count, i) in (0..max).withIndex()) {
            val value = if (i % 2 == 0) {
                (max - i)
            } else {
                i - 1
            }
            assertThat(set).named("iteration=$i").doesNotContain(value)
            set += value
            assertThat(set.size).isEqualTo(count + 1)
            assertThat(set.maxDepth()).isLessThan(3)
        }
        assertThat(set.maxDepth()).isEqualTo(1)
    }

    @Test
    fun reproSizeWrongAtBoundary() {
        val set = intSetOf(0..126)
        assertThat(set.size).isEqualTo(127)
        set += 127
        assertThat(set.size).isEqualTo(128)
    }

    @Test
    fun unionToFullPage() {
        val set1 = intSetOf(0..62)
        val set2 = intSetOf(63)
        val result = set1 union set2
        assertThat(result.maxDepth()).isEqualTo(1)
    }

    @Test
    fun unionToFullPage2() {
        val set1 = intSetOf(0..126)
        val set2 = intSetOf(127)
        val result = set1 union set2
        assertThat(result.maxDepth()).isEqualTo(1)
    }

    @Test
    fun unionToFullPage3() {
        val set1 = intSetOf(1..127)
        val set2 = intSetOf(0)
        val result = set1 union set2
        assertThat(result.maxDepth()).isEqualTo(1)
    }

    @Test
    fun intersect1() {
        val set1 = intSetOf(0..127)
        val set2 = intSetOf(0)
        val result = set1 intersect set2
        assertThat(result.maxDepth()).isEqualTo(1)
    }

    @Test
    fun intersect2() {
        val set1 = intSetOf(0..127)
        val set2 = intSetOf()
        val result = set1 intersect set2
        assertThat(result.maxDepth()).isEqualTo(1)
    }

    @Test
    fun intersect3() {
        val set1 = intSetOf(0..127)
        val set2 = intSetOf(70)
        val result = set1 intersect set2
        assertThat(result.maxDepth()).isEqualTo(1)
    }

    @Test
    fun testAnd() {
        val set1 = intSetOf(1, 2, 3)
        val set2 = intSetOf(3, 4, 5)
        val result = set1 intersect set2
        assertThat(result.contains(1)).isFalse()
        assertThat(result.contains(2)).isFalse()
        assertThat(result.contains(3)).isTrue()
        assertThat(result.contains(4)).isFalse()
        assertThat(result.contains(5)).isFalse()
    }

    @Test
    fun testEmpty() {
        val set = intSetOf()
        assertThat(set).hasSize(0)
        soak(set)
    }

    @Test
    fun setValue() {
        val set = intSetOf()
        assertThat(set.add(0)).isFalse()
        assertThat(set.contains(0)).isTrue()
        assertThat(set.contains(1)).isFalse()
        soak(set)
    }

    @Test
    fun setValueAdjacent() {
        val set = intSetOf()
        assertThat(set.add(0)).isFalse()
        assertThat(set.add(1)).isFalse()
        assertThat(set.add(1)).isTrue()
        assertThat(set.contains(0)).isTrue()
        assertThat(set.contains(1)).isTrue()
        assertThat(set.contains(2)).isFalse()
        soak(set)
    }

    @Test
    fun setHighValue() {
        val set = intSetOf()
        set.add(1234567)
        assertThat(set.contains(1234566)).isFalse()
        assertThat(set.contains(1234567)).isTrue()
        assertThat(set.contains(1234568)).isFalse()
        soak(set)
    }

    @Test
    fun setHighValueThenLow() {
        val set = intSetOf()
        set.add(1234567)
        assertThat(set.contains(1234566)).isFalse()
        assertThat(set.contains(1234567)).isTrue()
        assertThat(set.contains(1234568)).isFalse()
        set.add(3)
        assertThat(set.contains(2)).isFalse()
        assertThat(set.contains(3)).isTrue()
        assertThat(set.contains(4)).isFalse()
        soak(set)
    }

    @Test
    fun setPageAlmostAdjacent() {
        val set = intSetOf()
        set.add(0)
        set.add(63)
        assertThat(set.contains(0)).isTrue()
        assertThat(set.contains(1)).isFalse()
        assertThat(set.contains(62)).isFalse()
        assertThat(set.contains(63)).isTrue()
        assertThat(set.contains(64)).isFalse()
        soak(set)
    }

    @Test
    fun setPageAdjacent() {
        val set = intSetOf()
        set.add(0)
        assertThat(set.contains(0)).isTrue()
        assertThat(set.contains(1)).isFalse()
        assertThat(set.contains(63)).isFalse()
        assertThat(set.contains(64)).isFalse()
        assertThat(set.contains(65)).isFalse()
        set.add(64)
        assertThat(set.contains(0)).isTrue()
        assertThat(set.contains(1)).isFalse()
        assertThat(set.contains(63)).isFalse()
        assertThat(set.contains(64)).isTrue()
        assertThat(set.contains(65)).isFalse()
        soak(set)
    }


    private fun serialize(set: IntSet): ByteArray {
        val bos = ByteArrayOutputStream()
        val out: ObjectOutput?
        val bytes: ByteArray
        try {
            out = ObjectOutputStream(bos)
            out.writeObject(set)
            out.flush()
            bytes = bos.toByteArray()
        } finally {
            try {
                bos.close()
            } catch (ex: IOException) {
                // ignore close exception
            }
        }
        return bytes
    }

    private fun deserialize(bytes: ByteArray): IntSet {
        val bis = ByteArrayInputStream(bytes)
        var i: ObjectInput? = null
        try {
            i = ObjectInputStream(bis)
            return i.readObject() as IntSet
        } finally {
            try {
                if (i != null) {
                    i.close()
                }
            } catch (ex: IOException) {
            }
        }
    }

    @Test
    fun serializeDeserialize() {
        val set = intSetOf()
        soak(set)
        set += 132
        soak(set)
        assertThat(set.contains(131)).isFalse()
        assertThat(set.contains(132)).isTrue()
        assertThat(set.contains(133)).isFalse()
        val bytes = serialize(set)
        assertThat(bytes.size).isEqualTo(60)
        val set2 = deserialize(bytes)
        soak(set2)
        assertThat(set2.contains(131)).isFalse()
        assertThat(set2.contains(132)).isTrue()
        assertThat(set2.contains(133)).isFalse()
    }

    @Test
    fun addMany() {
        val set = intSetOf()
        set += 0..63
        soak(set)
        val bytes = serialize(set)
        assertThat(bytes.size).isEqualTo(52)
        val set2 = deserialize(bytes)
        assertThat(set2.contains(63)).isTrue()
        assertThat(set2.contains(64)).isFalse()
        soak(set2)
    }

    @Test
    fun clear() {
        val set = intSetOf()
        soak(set)
        set += 0..63
        assertThat(set.pages().count()).isEqualTo(1)
        assertThat(set).hasSize(64)
        soak(set)
        set.clear()
        assertThat(set.pages().count()).isEqualTo(0)
        soak(set)
        val bytes = serialize(set)
        assertThat(bytes.size).isEqualTo(48)
        val set2 = deserialize(bytes)
        soak(set2)
        assertThat(set2.contains(63)).isFalse()
        soak(set2)
        assertThat(set2.contains(64)).isFalse()
    }

    @Test
    fun manyPages() {
        val set = intSetOf()
        soak(set)
        set += 0..63
        soak(set)
        assertThat(set.size).isEqualTo(64)
        assertThat(set.contains(0)).isTrue()
        assertThat(set.contains(1024)).isFalse()
        set += 64
        soak(set)
        assertThat(set.size).isEqualTo(65)
        set += 0..1024
        soak(set)
        assertThat(set.pages().count()).isEqualTo(17)
    }

    @Test
    fun triNodeCoalescePermutations() {
        val values = listOf(1,63,64,127,128,255)
        for (permutation in values.permutations()) {
            val set = intSetOf()
            val other = mutableSetOf<Int>()
            for (value in permutation) {
                set += value
                other += value
                assertThat(set)
                        .named("$permutation at $value")
                        .containsExactlyElementsIn(other)
            }
            assertThat(set.maxDepth()).isEqualTo(1)
            assertThat(set.serializationSize()).isEqualTo(41)
        }
    }

    @Test
    fun iteratorRepro() {
        val set = intSetOf()
        set += 1
        set += 63
        set += 64
        set += 127
        assertThat(set.pages()).hasSize(2)
        val it = set.iterator()
        assertThat(it.hasNext()).isTrue()
        assertThat(it.next()).isEqualTo(1)
        assertThat(it.hasNext()).isTrue()
        assertThat(it.next()).isEqualTo(63)
        assertThat(it.hasNext()).isTrue()
        assertThat(it.next()).isEqualTo(64)
        assertThat(it.hasNext()).isTrue()
        assertThat(it.next()).isEqualTo(127)
        assertThat(it.hasNext()).isFalse()
        println("$set")
    }

    @Test
    fun repro2() {
        val left = PairNode(LongPageNode(7, arrayOf(1L, 2L, 3L)), AllSetNode(PageRange(11,11)))
        val right = LongPageNode(12, arrayOf(4L))
        val pair = createPairNode(left, right)
        assertThat(pair.maxDepth()).isEqualTo(2)
    }

    @Test
    fun triNodeCoalesceCombinations() {
        val values = arrayOf(1,63,64,127,128,255)
        for (permutation in values.combinations()) {
            val set = intSetOf()
            for (value in permutation) {
                set += value
                soak(set)
            }
        }
    }

    @Test
    fun triNodeCoalesce1() {
        val set = intSetOf()
        set += 1
        set += 63
        set += 255
        set += 64
    }

    @Test
    fun adjacentNodeCopy() {
        val set = intSetOf()
        set += 0..63
        set += 64
        val copy = set.copy()
        (set.pages() coiterate copy.pages())
                .onEach { assertThat(it.left).isEqualTo(it.right) }
                .count()
    }

    @Test
    fun coiterate() {
        val set1 = intSetOf(52)
        assertThat(set1.contains(52)).isTrue()
        val set2 = intSetOf(Int.MAX_VALUE - 52)
        val combined = (set1.pages() coiterate set2.pages())
                .toList()
                .toTypedArray()
        assertThat(combined).hasLength(2)
        assertThat(combined[0]).isEqualTo(Copage(0, 1L shl 52, 0))
        assertThat(combined[1]).isEqualTo(Copage(33554431, 0, 2048))
    }

    @Test
    fun coiterate2() {
        val set = intSetOf()
        set += 0..63
        val combined = (set.pages() coiterate set.pages())
                .toList()
                .toTypedArray()
        assertThat(combined).hasLength(1)
        assertThat(combined[0]).isEqualTo(Copage(0, -1, -1))
    }

    @Test
    fun setPage() {
        val set = intSetOf()
        set.setPage(0, 3)
        assertThat(set.contains(0)).isTrue()
        assertThat(set.contains(1)).isTrue()
        assertThat(set.contains(2)).isFalse()
        set.setPage(0, 0)
        assertThat(set.contains(0)).isFalse()
        assertThat(set.contains(1)).isFalse()
        assertThat(set.contains(2)).isFalse()
        assertThat(set.pages()).hasSize(1)
    }

    @Test
    fun and() {
        val bf1 = intSetOf(0..5)
        val bf2 = intSetOf(3..4)
        val bf3 = bf1 intersect bf2
        assertThat(bf3.contains(0)).isFalse()
        assertThat(bf3.contains(1)).isFalse()
        assertThat(bf3.contains(2)).isFalse()
        assertThat(bf3.contains(3)).isTrue()
        assertThat(bf3.contains(4)).isTrue()
        assertThat(bf3.contains(5)).isFalse()
    }
    
    @Test
    fun reproCoalesceBug() {
        val filter = PairNode(AllSetNode(PageRange(0,0)), LongPageNode(1, arrayOf(1)))
        var other = filter
        assertThat(IntSet(filter) == IntSet(other)).isTrue()
    }

    private fun soak(set: IntSet) {
        val bytes1 = serialize(set)
        val set2 = deserialize(bytes1)
        val bytes2 = serialize(set2)
        assertThat(bytes1).isEqualTo(bytes2)
        assertThat(set == set2).isTrue()
        set.size
        set.contains(0)
        set.contains(Int.MAX_VALUE)
        set.pages().count()
        (set.pages() coiterate set.pages())
                .onEach { assertThat(it.left).isEqualTo(it.right) }
                .count()

        val copy = set.copy()
        (set.pages() coiterate copy.pages())
                .onEach { assertThat(it.left).isEqualTo(it.right) }
                .count()
        assertThat(set == set).isTrue()

        val forEached = intSetOf()
        set.forEach { forEached += it }
        assertThat(set == forEached).isTrue()
    }
}