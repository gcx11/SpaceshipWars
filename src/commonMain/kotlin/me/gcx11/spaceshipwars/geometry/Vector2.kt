package me.gcx11.spaceshipwars.geometry

import kotlin.math.abs
import kotlin.math.hypot

class Vector2(
    val x: Float,
    val y: Float
) {
    val length get() = hypot(x, y)

    operator fun minus(other: Vector2): Vector2 {
        return Vector2(this.x - other.x, this.y - other.y)
    }

    fun times(scalar: Float): Vector2 {
        return Vector2(this.x * scalar, this.y * scalar)
    }

    fun cross(other: Vector2): Float {
        return this.x*other.y - this.y*other.x
    }

    fun epsilonEquals(other: Vector2): Boolean {
        return abs(this.x - other.x) < epsilon && abs(this.y - other.y) < epsilon
    }
}