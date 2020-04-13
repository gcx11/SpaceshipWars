package me.gcx11.spaceshipwars

import kotlin.random.Random

@OptIn(ExperimentalUnsignedTypes::class)
class UUID private constructor(
    private val high: Long,
    private val low: Long
) {
    companion object {
        private val rnd = Random.Default

        fun new(): UUID {
            // TODO UUIDv4
            val high = rnd.nextLong()
            val low = rnd.nextLong()

            return UUID(high, low)
        }

        fun from(high: Long, low: Long): UUID {
            return UUID(high, low)
        }
    }

    fun getHigh(): Long {
        return this.high
    }

    fun getLow(): Long {
        return this.low
    }

    override fun equals(other: Any?): Boolean {
        if (other !is UUID) return false

        return this.high == other.high && this.low == other.low
    }

    override fun hashCode(): Int {
        var result = high.hashCode()
        result = 31 * result + low.hashCode()
        return result
    }

    override fun toString(): String {
        val highHex = high.toULong().toString(16).let { "0".repeat(16 - it.length) + it }
        val lowHex = low.toULong().toString(16).let { "0".repeat(16 - it.length) + it }

        return buildString {
            append(highHex.slice(0..7))
            append("-")
            append(highHex.slice(8..11))
            append("-")
            append(highHex.slice(12..15))
            append("-")
            append(lowHex.slice(0..7))
            append("-")
            append(lowHex.slice(8..15))
        }
    }
}