package me.gcx11.spaceshipwars.geometry

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sqrt

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
            is Circle -> this.isInside(shape)
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
            is Line -> when {
                shape.intersectsWith(first) -> true
                shape.intersectsWith(second) -> true
                else -> this.isCrossing(shape)
            }
            is Triangle -> {
                val isTouchingEdges = shape.edges.any { edge ->
                    edge.intersectsWith(this)
                }

                val isInside = shape.intersectsWith(first) || shape.intersectsWith(second)

                isTouchingEdges || isInside
            }
            is Circle -> when {
                shape.intersectsWith(first) -> true
                shape.intersectsWith(second) -> true
                else -> this.isCrossing(shape)
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
                val isTouchingEdges = shape.edges.any { edge ->
                    edges.any { it.intersectsWith(edge) }
                }

                val isInside = shape.vertices.any {
                    it.isPointInsideTriangle(this)
                } || vertices.any {
                    it.isPointInsideTriangle(shape)
                }

                isTouchingEdges || isInside
            }
            is Circle -> shape.intersectsWith(this)
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

data class Circle(
    var center: Point,
    var radius: Float
) : Shape() {
    override fun intersectsWith(shape: Shape): Boolean {
        return when (shape) {
            is Point -> (center - shape).length <= radius
            is Line -> shape.first.isInside(this) || shape.second.isInside(this) || shape.isCrossing(this)
            is Triangle -> {
                val verticleInside = shape.vertices.any {
                    it.isInside(this)
                }

                val touchingByEdge = shape.edges.any {
                    it.intersectsWith(this)
                }

                verticleInside || touchingByEdge
            }
            is Circle -> (shape.center - this.center).length <= shape.radius + this.radius
            is ComposedFromTwo -> shape.intersectsWith(this)
            is Composed -> shape.intersectsWith(this)
        }
    }

    override fun points(): Iterable<Point> {
        return listOf()
    }

    companion object {
        val default get() = Circle(Point.default, 0f)
    }
}

data class ComposedFromTwo(
    var first: Shape,
    var second: Shape
) : Shape() {
    override fun intersectsWith(shape: Shape): Boolean {
        return shape.intersectsWith(first) || shape.intersectsWith(second)
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
        return subShapes.any { it.intersectsWith(shape) }
    }

    override fun points(): Iterable<Point> {
        return subShapes.flatMap { it.points() }
    }

    companion object {
        val default get() = Composed(mutableListOf())
    }
}

fun Point.isPointInsideTriangle(triangle: Triangle): Boolean {
    val a = triangle.first
    val b = triangle.second
    val c = triangle.third

    val t = this.x - a.x
    val u = this.y - a.y

    val v = (b.x - a.x) * u - (b.y - a.y) * t > 0

    if ((c.x - a.x) * u - (c.y - a.y) * t > 0 == v) return false

    return (c.x - b.x) * (this.y - b.y) - (c.y - b.y) * (this.x - b.x) > 0 == v
}

fun Line.isCrossing(line: Line): Boolean {
    val x1 = this.first.x
    val y1 = this.first.y
    val x2 = this.second.x
    val y2 = this.second.y
    val x3 = line.first.x
    val y3 = line.first.y
    val x4 = line.second.x
    val y4 = line.second.y

    val d = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1)
    if (d == 0f) return false

    val t = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / d
    if (t !in 0.0..1.0) return false
    val u = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / d
    if (u !in 0.0..1.0) return false

    return true
}

fun Point.isInside(circle: Circle): Boolean {
    return (this - circle.center).length <= circle.radius
}

fun Line.isCrossing(circle: Circle): Boolean {
    val d = this.second.x - this.first.x
    val e = this.first.x - circle.center.x
    val f = this.second.y - this.first.y
    val g = this.first.y - circle.center.y

    val a = d*d+f*f
    val b = 2*(d*e+f*g)
    val c = e*e+g*g-circle.radius*circle.radius

    val disc = b*b - 4*a*c
    if (disc < 0) return false

    if (disc == 0f) return -b/(2*a) in (0.0..1.0)
    val t = (-b-sqrt(disc))/(2*a)
    if (t in 0.0..1.0) return true
    val u = (-b+sqrt(disc))/(2*a)
    if (u in 0.0..1.0) return true

    return false
}