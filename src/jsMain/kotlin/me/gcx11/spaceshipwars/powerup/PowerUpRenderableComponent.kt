package me.gcx11.spaceshipwars.powerup

import me.gcx11.spaceshipwars.Camera
import me.gcx11.spaceshipwars.components.CanvasContextRenderableComponent
import me.gcx11.spaceshipwars.components.GeometricComponent
import me.gcx11.spaceshipwars.geometry.Circle
import me.gcx11.spaceshipwars.models.Entity
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.math.PI

class PowerUpRenderableComponent(
    override val parent: Entity
): CanvasContextRenderableComponent {
    override var context: CanvasRenderingContext2D? = null

    override fun draw() {
        val ctx = context ?: return
        val shape = parent.getRequiredComponent<GeometricComponent>().shape as? Circle ?: return
        val powerUpTypeComponent = parent.getRequiredComponent<PowerUpTypeComponent>()

        when (powerUpTypeComponent.type) {
            PowerUpType.REGEN -> {
                renderRegen(ctx, shape)
            }

            PowerUpType.BOOST -> {
                renderBoost(ctx, shape)
            }

            PowerUpType.SHIELD -> {
                renderShield(ctx, shape)
            }

            else -> {}
        }
    }

    private fun renderRegen(ctx: CanvasRenderingContext2D, shape: Circle) {
        ctx.beginPath()

        val (x, y) = Camera.project(shape.center.x, shape.center.y)
        val lineDistanceToCenter = 20.0

        ctx.moveTo(x.toDouble() - lineDistanceToCenter, y.toDouble())
        ctx.lineTo(x.toDouble() + lineDistanceToCenter, y.toDouble())

        ctx.moveTo(x.toDouble(), y.toDouble() - lineDistanceToCenter)
        ctx.lineTo(x.toDouble(), y.toDouble() + lineDistanceToCenter)

        ctx.closePath()

        ctx.strokeStyle = "#ff0066"
        ctx.lineWidth = 15.0
        ctx.stroke()
    }

    private fun renderBoost(ctx: CanvasRenderingContext2D, shape: Circle) {
        ctx.beginPath()

        val (x, y) = Camera.project(shape.center.x, shape.center.y)
        val a = 30.0
        val b = 30.0
        val lineDistanceToCenter = 20.0

        ctx.moveTo(x.toDouble() + a, y.toDouble() - b)
        ctx.lineTo(x.toDouble() - lineDistanceToCenter, y.toDouble())
        ctx.lineTo(x.toDouble() + lineDistanceToCenter, y.toDouble())
        ctx.lineTo(x.toDouble() - a, y.toDouble() + b)

        ctx.closePath()

        ctx.fillStyle = "#dddd66"
        ctx.lineWidth = 5.0
        ctx.fill()
    }

    private fun renderShield(ctx: CanvasRenderingContext2D, shape: Circle) {
        ctx.beginPath()

        val (x, y) = Camera.project(shape.center.x, shape.center.y)
        val distance = 15.0

        ctx.moveTo(x.toDouble() - distance, y.toDouble() - distance)
        ctx.lineTo(x.toDouble() + distance, y.toDouble() - distance)
        ctx.lineTo(x.toDouble() + distance, y.toDouble() + distance)
        ctx.lineTo(x.toDouble() - distance, y.toDouble() + distance)
        ctx.arc(x.toDouble(), y.toDouble() + distance, distance, 0.0, PI)

        ctx.closePath()

        ctx.fillStyle = "#66dddd"
        ctx.lineWidth = 5.0
        ctx.fill()
    }
}