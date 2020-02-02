package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.Camera
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

        val shape = geometricComponent.shape
        val first = shape.points().firstOrNull()

        ctx.beginPath()

        if (first != null) {
            val (x, y) = Camera.project(first.x, first.y)
            ctx.moveTo(x.toDouble(), y.toDouble())
        }

        shape.points().drop(1).forEach {
            val (x, y) = Camera.project(it.x, it.y)
            ctx.lineTo(x.toDouble(), y.toDouble())
        }

        ctx.closePath()

        ctx.fillStyle = if (serverConnection.id == clientComponent.clientId) "green" else "red"
        ctx.fill()
    }
}