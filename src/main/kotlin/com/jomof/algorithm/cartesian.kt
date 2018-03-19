package com.jomof.algorithm


fun cartesian(a: IntRange, b: IntRange, action: (a: Int, b: Int) -> Unit) {
    for (i in a) {
        for (j in b) {
            action(i, j)
        }
    }
}

fun cartesian(a: IntRange, b: IntRange, c: IntRange, action: (a: Int, b: Int, c: Int) -> Unit) {
    for (i in a) {
        for (j in b) {
            for (k in c) {
                action(i, j, k)
            }
        }
    }
}

fun cartesian(a: Int, b: Int, action: (a: Int, b: Int) -> Unit) {
    cartesian(0 until a, 0 until b, action)
}

fun cartesian(a: Int, b: Int, c: Int, action: (a: Int, b: Int, c: Int) -> Unit) {
    cartesian(0 until a, 0 until b, 0 until c, action)
}

fun <A, B, C> cartesian(a: List<A>, b: List<B>, c: List<C>, action: (a: A, b: B, c: C) -> Unit) {
    cartesian(a.size, b.size, c.size) { i, j, k ->
        action(a[i], b[j], c[k])
    }
}

