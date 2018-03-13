package com.jomof.intset

internal fun pageOf(element: Int) = element / 64
internal fun offsetOf(element: Int) = element % 64
internal fun bitOf(offset: Int): Long = 1L shl offset