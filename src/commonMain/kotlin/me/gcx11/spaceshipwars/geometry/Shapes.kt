package me.gcx11.spaceshipwars.geometry

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

const val epsilon = 1e-13f

sealed class Shape {
    abstract fun intersectsWith(shape: Shape): Boolean
    abstract fun points(): Iterable<Point>
}

data class Point(
    var x: Float,
    var y: Float
) : Shape() {
    val vector get() = Vector2(x, y)

    override fun intersectsWith(shape: Shape): Boolean {
        return when (shape) {
            is Point -> vector.epsilonEquals(shape.vector)
            is Line -> {
                val vectorAC = vector - shape.first.vector
                val vectorCB = shape.second.vector - vector
                val vectorAB = shape.second.vector - shape.first.vector

                vectorAB.length == vectorAC.length + vectorCB.length
            }
            is Triangle -> this.isPointInsideTriangle(shape)
            is ComposedFromTwo -> shape.intersectsWith(this)
            is Composed -> shape.intersectsWith(this)
        }
    }

    override fun points(): Iterable<Point> {
        return listOf(this)
    }

    override fun toString() = "Point ($x, $y)"

    fun angleWith(other: Point): Float {
        return atan2(other.y - y, other.x - x)
    }

    fun distanceTo(other: Point): Float {
        return hypot(abs(other.y - y), abs(other.x - x))
    }

    operator fun minus(other: Point): Vector2 {
        return this.vector - other.vector
    }

    companion object {
        val default get() = Point(0f, 0f)
    }
}

data class Line(
    var first: Point,
    var second: Point
) : Shape() {
    override fun intersectsWith(shape: Shape): Boolean {
        return when (shape) {
            is Point -> shape.intersectsWith(this)
            is Line -> {
                if (shape.intersectsWith(first) || first.intersectsWith(shape)) return true
                else if (shape.intersectsWith(second) || second.intersectsWith(shape)) return true

                this.isCrossing(shape)
            }
            is Triangle -> {
                shape.intersectsWith(first) || shape.intersectsWith(second)
            }
            is ComposedFromTwo -> shape.intersectsWith(this)
            is Composed -> shape.intersectsWith(this)
        }
    }

    override fun points(): Iterable<Point> {
        return listOf(first, second)
    }

    override fun toString() = "Line [(${first.x}, ${first.y}), (${second.x}, ${second.y})]"

    companion object {
        val default get() = Line(Point.default, Point.default)
    }
}

data class Triangle(
    var first: Point,
    var second: Point,
    var third: Point
) : Shape() {
    val firstLine get() = Line(this.first, this.second)
    val secondLine get() = Line(this.second, this.third)
    val thirdLine get() = Line(this.third, this.first)

    override fun intersectsWith(shape: Shape): Boolean {
        return when (shape) {
            is Point -> shape.intersectsWith(this)
            is Line -> shape.intersectsWith(this)
            is Triangle -> {
                shape.first.isPointInsideTriangle(this) ||
                        shape.second.isPointInsideTriangle(this) ||
                        shape.third.isPointInsideTriangle(this) ||
                        first.isPointInsideTriangle(shape) ||
                        second.isPointInsideTriangle(shape) ||
                        first.isPointInsideTriangle(shape)
            }
            is ComposedFromTwo -> shape.intersectsWith(this)
            is Composed -> shape.intersectsWith(this)
        }
    }

    override fun points(): Iterable<Point> {
        return listOf(first, second, third)
    }

    val vertices get() = arrayOf(first, second, third)

    val edges get() = arrayOf(firstLine, secondLine, thirdLine)

    override fun toString(): String {
        return "Triangle [(${first.x}, ${first.y}), (${second.x}, ${second.y}), (${third.x}, ${third.y})]"
    }

    companion object {
        val default get() = Triangle(Point.default, Point.default, Point.default)
    }
}

data class ComposedFromTwo(
    var first: Shape,
    var second: Shape
) : Shape() {
    override fun intersectsWith(shape: Shape): Boolean {
        return when (shape) {
            is Point -> shape.intersectsWith(first) || shape.intersectsWith(second)
            is Line -> shape.intersectsWith(first) || shape.intersectsWith(second)
            is Triangle -> shape.intersectsWith(first) || shape.intersectsWith(second)
            is ComposedFromTwo -> shape.intersectsWith(first) || shape.intersectsWith(second)
            is Composed -> shape.intersectsWith(first) || shape.intersectsWith(second)
        }
    }

    override fun points(): Iterable<Point> {
        return first.points() + second.points()
    }

    companion object {
        val default get() = ComposedFromTwo(Point.default, Point.default)
    }
}

data class Composed(
    val subShapes: MutableList<Shape>
) : Shape() {
    override fun intersectsWith(shape: Shape): Boolean {
        return when (shape) {
            is Point -> subShapes.any { it.intersectsWith(shape) }
            is Line -> subShapes.any { it.intersectsWith(shape) }
            is Triangle -> subShapes.any { it.intersectsWith(shape) }
            is ComposedFromTwo -> subShapes.any { it.intersectsWith(shape) }
            is Composed -> subShapes.any { it.intersectsWith(shape) }
        }
    }

    override fun points(): Iterable<Point> {
        return subShapes.flatMap { it.points() }
    }

    companion object {
        val default get() = Composed(mutableListOf())
    }
}


fun Point.isPointInsideTriangle(triangle: Triangle): Boolean {
    // check edges first
    if (triangle.firstLine.intersectsWith(this)) return true
    else if (triangle.secondLine.intersectsWith(this)) return true
    else if (triangle.thirdLine.intersectsWith(this)) return true

    val ab = triangle.second - triangle.first
    val bc = triangle.third - triangle.second
    val ca = triangle.first - triangle.third

    val ap = this - triangle.first
    val bp = this - triangle.second
    val cp = this - triangle.third

    if (ab.cross(ap) > epsilon && bc.cross(bp) > epsilon && ca.cross(cp) > epsilon) return true
    if (ab.cross(ap) < -epsilon && bc.cross(bp) < -epsilon && ca.cross(cp) < -epsilon) return true
    if ((-epsilon <= ab.cross(ap) || ab.cross(ap) <= epsilon) &&
        (-epsilon <= bc.cross(bp) || bc.cross(bp) <= epsilon) &&
        (-epsilon <= ca.cross(cp) || ca.cross(cp) <= epsilon)) {
        return true
    }

    return false
}

fun Line.isCrossing(line: Line): Boolean {
    // TODO
    return false
}