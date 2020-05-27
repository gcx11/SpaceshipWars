package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.Camera
import me.gcx11.spaceshipwars.components.CanvasContextRenderableComponent
import me.gcx11.spaceshipwars.components.GeometricComponent
import me.gcx11.spaceshipwars.models.Entity
import org.w3c.dom.CENTER
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextAlign

class SpaceshipNickNameComponent(
    override val parent: Entity,
    val nickName: String
): CanvasContextRenderableComponent {
    override var context: CanvasRenderingContext2D? = null

    override fun draw() {
        val ctx = context ?: return
        val geometricComponent = parent.getOptionalComponent<GeometricComponent>() ?: return

        val (x, y) = Camera.project(geometricComponent.x, geometricComponent.y)
        ctx.font = "12px Arial"
        ctx.fillStyle = "white"
        ctx.textAlign = CanvasTextAlign.CENTER
        ctx.fillText(nickName, x.toDouble(), y.toDouble())
    }
}