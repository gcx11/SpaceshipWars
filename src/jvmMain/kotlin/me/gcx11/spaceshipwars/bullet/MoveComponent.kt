package me.gcx11.spaceshipwars.bullet

import me.gcx11.spaceshipwars.components.DefaultMoveComponent
import me.gcx11.spaceshipwars.components.getRequiredSibling
import me.gcx11.spaceshipwars.models.Entity
import me.gcx11.spaceshipwars.models.World

class MoveComponent(
    override val parent: Entity,

    override var speed: Float = 100f
) : DefaultMoveComponent(parent, speed) {
    private var timer = 2f

    override fun update(delta: Float) {
        if (timer < 0) {
            World.deleteLater(parent)
        }

        timer -= delta
        super.update(delta)
    }

    override fun direction(): Float {
        return getRequiredSibling<GeometricComponent>().directionAngle
    }
}