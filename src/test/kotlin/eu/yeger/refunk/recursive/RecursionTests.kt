package eu.yeger.refunk.recursive

import eu.yeger.refunk.base.*
import org.junit.Assert.assertEquals
import org.junit.Test

class RecursionTests {

    @Test
    fun testRecursion() {
        assertEquals(10, Recursion(c(10), c(42)).apply(0))
        assertEquals(42, Recursion(c(10), c(42)).apply(1))
    }

    @Test
    fun testRecursions() {
        assertEquals(6, (
                recursive {Successor() andThen Successor() }
                        withBaseCase { one() andThen Successor() }
                ).apply(2))
    }
}