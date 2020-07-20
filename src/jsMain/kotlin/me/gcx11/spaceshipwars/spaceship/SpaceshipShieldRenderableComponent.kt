package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.Camera
import me.gcx11.spaceshipwars.components.CanvasContextRenderableComponent
import me.gcx11.spaceshipwars.models.Entity
import me.gcx11.spaceshipwars.shieldRadius
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.math.PI

class SpaceshipShieldRenderableComponent(
    override val parent: Entity
) : CanvasContextRenderableComponent {
    override var context: CanvasRenderingContext2D? = null

    override fun draw() {
        val ctx = context ?: return

        val geometricComponent = parent.getRequiredComponent<GeometricComponent>()
        val shieldComponent = parent.getRequiredComponent<ShieldComponent>()

        if (shieldComponent.hasShield) {
            val (x, y) = Camera.project(geometricComponent.center.x, geometricComponent.center.y)

            ctx.beginPath()
            ctx.moveTo(x.toDouble() + shieldRadius.toDouble(), y.toDouble())
            ctx.arc(
                x.toDouble(),
                y.toDouble(),
                shieldRadius.toDouble(),
                0.0,
                2 * PI
            )
            ctx.closePath()

            ctx.strokeStyle = "cyan"
            ctx.lineWidth = 2.0
            ctx.stroke()
        }
    }
}