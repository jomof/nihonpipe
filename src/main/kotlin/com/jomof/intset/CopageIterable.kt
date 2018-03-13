package com.jomof.intset

class CopageIterable(
        private val left: Iterable<Page>,
        private val right: Iterable<Page>) : Iterable<Copage> {

    override fun iterator() = IntSetCoiterator(
            left.iterator(),
            right.iterator())

    class IntSetCoiterator(
            private val left: Iterator<Page>,
            private val right: Iterator<Page>) : Iterator<Copage> {
        private fun nextOrNull(it: Iterator<Page>): Page? {
            return if (it.hasNext()) {
                it.next()
            } else {
                null
            }
        }

        private var leftPage = nextOrNull(left)
        private var rightPage = nextOrNull(right)
        override fun hasNext() = leftPage != null || rightPage != null
        override fun next(): Copage {
            if (leftPage == null && rightPage == null) {
                throw RuntimeException()
            } else if (leftPage == null && rightPage != null) {
                val result = Copage(rightPage!!.number, 0, rightPage!!.elements)
                rightPage = nextOrNull(right)
                return result
            } else if (rightPage == null && leftPage != null) {
                val result = Copage(leftPage!!.number, leftPage!!.elements, 0)
                leftPage = nextOrNull(left)
                return result
            } else if (leftPage != null && rightPage != null) {
                val (leftNumber, leftElements) = leftPage!!
                val (rightNumber, rightElements) = rightPage!!
                when {
                    leftNumber < rightNumber -> {
                        val result = Copage(leftNumber, leftElements, 0)
                        leftPage = nextOrNull(left)
                        return result
                    }
                    leftNumber > rightNumber -> {
                        val result = Copage(rightNumber, 0, rightElements)
                        rightPage = nextOrNull(right)
                        return result
                    }
                    else -> {
                        val result = Copage(rightNumber, leftElements, rightElements)
                        leftPage = nextOrNull(left)
                        rightPage = nextOrNull(right)
                        return result
                    }
                }
            }
            throw RuntimeException()
        }
    }
}
