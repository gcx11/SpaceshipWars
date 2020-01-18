package me.gcx11.spaceshipwars.components

import me.gcx11.spaceshipwars.events.EventHandler
import me.gcx11.spaceshipwars.events.MoveEvent

interface MoveComponent : BehaviourComponent, DisposableComponent {
    var speed: Float
    val eventHandler: EventHandler<MoveEvent>

    override fun dispose() {
        eventHandler.removeAll()
    }
}