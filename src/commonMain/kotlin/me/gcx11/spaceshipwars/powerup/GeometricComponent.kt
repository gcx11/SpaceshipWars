package me.gcx11.spaceshipwars.powerup

import me.gcx11.spaceshipwars.components.GeometricComponent
import me.gcx11.spaceshipwars.geometry.Circle
import me.gcx11.spaceshipwars.geometry.Point
import me.gcx11.spaceshipwars.models.Entity
import me.gcx11.spaceshipwars.powerUpRadius

class GeometricComponent(
    override val parent: Entity,
    override var x: Float,
    override var y: Float,

    var radius: Float = powerUpRadius
): GeometricComponent {

    override val shape get() = Circle(Point(x, y), radius)
}