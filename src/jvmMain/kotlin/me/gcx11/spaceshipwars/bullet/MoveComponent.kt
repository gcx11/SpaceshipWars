package me.gcx11.spaceshipwars.bullet

import me.gcx11.spaceshipwars.clients
import me.gcx11.spaceshipwars.components.DefaultMoveComponent
import me.gcx11.spaceshipwars.components.getRequiredSibling
import me.gcx11.spaceshipwars.models.Entity
import me.gcx11.spaceshipwars.models.World
import me.gcx11.spaceshipwars.packets.EntityRemovePacket

class MoveComponent(
    override val parent: Entity,

    override var speed: Float = 5f
) : DefaultMoveComponent(parent, speed) {
    private var timer = 2f

    override fun update(delta: Float) {
        if (timer < 0) {
            World.deleteLater(parent)

            // TODO use events
            clients.forEach {
                it.sendPacket(EntityRemovePacket(parent.externalId))
            }
        }

        timer -= delta
        super.update(delta)
    }

    override fun direction(): Float {
        return getRequiredSibling<GeometricComponent>().directionAngle
    }
}