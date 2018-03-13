package com.jomof.intset

infix fun Iterable<Page>.coiterate(other: Iterable<Page>): Iterable<Copage> = CopageIterable(this, other)