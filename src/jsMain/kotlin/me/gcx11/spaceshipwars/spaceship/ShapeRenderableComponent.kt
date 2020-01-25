package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.components.ClientComponent
import me.gcx11.spaceshipwars.components.GeometricComponent
import me.gcx11.spaceshipwars.components.RenderableComponent
import me.gcx11.spaceshipwars.models.Entity
import me.gcx11.spaceshipwars.serverConnection
import org.w3c.dom.CanvasRenderingContext2D

class ShapeRenderableComponent(
    override val parent: Entity,
    var context: CanvasRenderingContext2D? = null
): RenderableComponent {
    override fun draw() {
        val ctx = context ?: return
        val clientComponent = parent.getOptionalComponent<ClientComponent>() ?: return
        val geometricComponent = parent.getOptionalComponent<GeometricComponent>() ?: return

        ctx.fillStyle = if (serverConnection.id == clientComponent.clientId) "green" else "red"

        ctx.fillRect(geometricComponent.x.toDouble(), geometricComponent.y.toDouble(), 50.0,50.0)
    }
}