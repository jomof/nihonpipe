package com.jomof.intset

import java.io.ObjectInput

fun readNode(i: ObjectInput): Node {
    val code = NodeCode.values()[i.readByte().toInt()]
    return when (code) {
        NodeCode.EMPTY -> EmptyNode.instance
        NodeCode.PAIR -> PairNode.of(i)
        NodeCode.LONG_PAGE -> LongPageNode.of(code, i)
        NodeCode.MULTI_LONG_PAGE -> LongPageNode.of(code, i)
        NodeCode.ALL_SET -> AllSetNode.of(code, i)
        NodeCode.MULTI_PAGE_ALL_SET -> AllSetNode.of(code, i)
    }
}