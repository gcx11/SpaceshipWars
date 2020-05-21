package me.gcx11.spaceshipwars

import me.gcx11.spaceshipwars.geometry.Line
import me.gcx11.spaceshipwars.geometry.Point
import me.gcx11.spaceshipwars.geometry.Triangle
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class GeometryTests {

    @Test
    fun twoPointsIntersection() {
        val p1 = Point(1f, 2f)
        val p2 = Point(3f, 4f)

        assertFalse(p1.intersectsWith(p2))
        assertFalse(p2.intersectsWith(p1))
    }

    @Test
    fun pointAndLineIntersection() {
        val p1 = Point(2f, 3f)
        val p2 = Point(2f, 4f)
        val l1 = Line(Point(1f, 2f), Point(3f, 4f))

        assertTrue(p1.intersectsWith(l1))
        assertTrue(l1.intersectsWith(p1))

        assertFalse(p2.intersectsWith(l1))
        assertFalse(l1.intersectsWith(p2))
    }

    @Test
    fun pointAndTriangleIntersection() {
        val p1 = Point(1f, 2f)
        val p2 = Point(3f, 4f)
        val t1 = Triangle(Point(5f, 6f), Point(6f, 7f), Point(7f, 8f))

        assertFalse(p1.intersectsWith(t1))
        assertFalse(t1.intersectsWith(p1))

        assertFalse(p2.intersectsWith(t1))
        assertFalse(t1.intersectsWith(p2))
    }

    @Test
    fun lineAndLineIntersection() {
        val l1 = Line(Point(1f, 2f), Point(3f, 4f))
        val l2 = Line(Point(3f, 4f), Point(5f, 6f))
        val l3 = Line(Point(3f, 2f), Point(1f, 4f))

        assertTrue(l1.intersectsWith(l2))
        assertTrue(l1.intersectsWith(l3))
        assertFalse(l2.intersectsWith(l3))
    }

    @Test
    fun lineAndTriangleIntersection() {
        val l1 = Line(Point(1f, 2f), Point(3f, 4f))
        val t1 = Triangle(Point(5f, 6f), Point(6f, 7f), Point(7f, 8f))

        assertFalse(t1.firstLine.intersectsWith(l1))
        assertFalse(t1.secondLine.intersectsWith(l1))
        assertFalse(t1.thirdLine.intersectsWith(l1))

        assertFalse(t1.intersectsWith(l1))
        assertFalse(l1.intersectsWith(t1))
    }
}