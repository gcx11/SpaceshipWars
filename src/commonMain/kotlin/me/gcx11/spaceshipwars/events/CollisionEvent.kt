package me.gcx11.spaceshipwars.events

import me.gcx11.spaceshipwars.components.CollidableComponent

class CollisionEvent(
    val first: CollidableComponent,
    val second: CollidableComponent
): Event()