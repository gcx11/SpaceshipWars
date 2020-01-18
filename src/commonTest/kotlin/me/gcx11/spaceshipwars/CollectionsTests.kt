package me.gcx11.spaceshipwars

import me.gcx11.spaceshipwars.collections.retrieveAll
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionsTests {

    @Test
    fun `test retrieve all`() {
        val numbers = mutableListOf(1, 2, 3, 4, 5, 6)
        val oddNumbers = numbers.retrieveAll { it % 2 != 0 }

        assertEquals(mutableListOf(2, 4, 6), numbers)
        assertEquals(listOf(1, 3, 5), oddNumbers)
    }
}