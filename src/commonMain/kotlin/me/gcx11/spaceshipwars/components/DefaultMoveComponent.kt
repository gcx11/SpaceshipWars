package me.gcx11.spaceshipwars.components

import me.gcx11.spaceshipwars.events.EventHandler
import me.gcx11.spaceshipwars.events.MoveEvent
import me.gcx11.spaceshipwars.models.Entity
import kotlin.math.cos
import kotlin.math.sin

abstract class DefaultMoveComponent(
    override val parent: Entity,
    override var speed: Float,

    override val eventHandler: EventHandler<MoveEvent> = EventHandler()
) : MoveComponent {
    override fun update(delta: Float) {
        getRequiredSibling<GeometricComponent>().let {
            it.x += speed * cos(direction()) * delta
            it.y += speed * sin(direction()) * delta

            eventHandler(MoveEvent(parent))
        }
    }

    abstract fun direction(): Float
}