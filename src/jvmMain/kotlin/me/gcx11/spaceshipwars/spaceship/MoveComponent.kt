package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.components.DefaultMoveComponent
import me.gcx11.spaceshipwars.components.getRequiredSibling
import me.gcx11.spaceshipwars.models.Entity

class MoveComponent(
    override val parent: Entity,

    val defaultSpeed: Float = 2f
): DefaultMoveComponent(parent, defaultSpeed) {
    override fun direction(): Float {
        return getRequiredSibling<GeometricComponent>().directionAngle
    }
}