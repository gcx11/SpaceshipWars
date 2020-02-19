package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.components.ClientComponent
import me.gcx11.spaceshipwars.components.FillStyle
import me.gcx11.spaceshipwars.components.ShapeRenderableComponent
import me.gcx11.spaceshipwars.models.Entity
import me.gcx11.spaceshipwars.serverConnection

class SpaceshipRenderableComponent(
    parent: Entity,
    fillStyle: FillStyle = FillStyle.FULL
): ShapeRenderableComponent(parent, fillStyle) {
    override fun color(): String {
        val clientComponent = parent.getOptionalComponent<ClientComponent>()
        return if (serverConnection.id == clientComponent?.clientId) "green" else "red"
    }
}