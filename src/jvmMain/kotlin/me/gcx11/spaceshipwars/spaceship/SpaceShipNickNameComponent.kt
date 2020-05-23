package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.components.Component
import me.gcx11.spaceshipwars.models.Entity

class SpaceShipNickNameComponent(
    override val parent: Entity,
    val nickName: String
): Component