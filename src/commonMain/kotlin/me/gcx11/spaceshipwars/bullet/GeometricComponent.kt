package me.gcx11.spaceshipwars.bullet

import me.gcx11.spaceshipwars.components.GeometricComponent
import me.gcx11.spaceshipwars.geometry.Line
import me.gcx11.spaceshipwars.geometry.Point
import me.gcx11.spaceshipwars.models.Entity
import kotlin.math.cos
import kotlin.math.sin

class GeometricComponent(
    override val parent: Entity,

    override var x: Float,
    override var y: Float,
    var directionAngle: Float = 0f,

    val size: Float = 5f
) : GeometricComponent {
    val endX get() = x + size * cos(directionAngle)
    val endY get() = y + size * sin(directionAngle)

    val first get() = Point(this.x,  this.y)
    val second get() = Point(endX, endY)

    override val shape get() = Line(this.first, this.second)
}