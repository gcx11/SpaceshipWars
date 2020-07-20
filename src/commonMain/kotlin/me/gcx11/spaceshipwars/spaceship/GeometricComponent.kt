package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.components.GeometricComponent
import me.gcx11.spaceshipwars.geometry.Circle
import me.gcx11.spaceshipwars.geometry.ComposedFromTwo
import me.gcx11.spaceshipwars.geometry.Point
import me.gcx11.spaceshipwars.geometry.Triangle
import me.gcx11.spaceshipwars.models.Entity
import kotlin.math.cos
import kotlin.math.sin

class GeometricComponent(
    override val parent: Entity,

    override var x: Float,
    override var y: Float,

    private val noseSize: Float,
    private val backSize: Float,
    private val wingSize: Float,

    var directionAngle: Float = 0f
) : GeometricComponent {
    val noseX get() = x + noseSize * cos(directionAngle)
    val noseY get() = y + noseSize * sin(directionAngle)

    val leftWingX get() = x - backSize * cos(directionAngle) - wingSize * sin(directionAngle)
    val leftWingY get() = y + wingSize * cos(directionAngle) - backSize * sin(directionAngle)

    val rightWingX get() = x - backSize * cos(directionAngle) + wingSize * sin(directionAngle)
    val rightWingY get() = y - wingSize * cos(directionAngle) - backSize * sin(directionAngle)

    val nose get() = Point(noseX, noseY)

    val leftWing get() = Point(leftWingX, leftWingY)

    val rightWing get() = Point(rightWingX, rightWingY)

    val center get() = Point(x, y)

    val leftPart get() = Triangle(nose, leftWing, center)

    val rightPart get() = Triangle(nose, rightWing, center)

    override val shape get() = ComposedFromTwo(leftPart, rightPart)

    override fun toString(): String {
        return buildString {
            append(leftPart)
            append(rightPart)
        }
    }
}
