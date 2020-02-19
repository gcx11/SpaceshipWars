package me.gcx11.spaceshipwars.components

import me.gcx11.spaceshipwars.Camera
import me.gcx11.spaceshipwars.models.Entity
import org.w3c.dom.CanvasRenderingContext2D

open class ShapeRenderableComponent(
    override val parent: Entity,
    val fillStyle: FillStyle,
    var context: CanvasRenderingContext2D? = null
): RenderableComponent {
    override fun draw() {
        val ctx = context ?: return
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

        when (fillStyle) {
            FillStyle.OUTLINE -> {
                ctx.strokeStyle = color()
                ctx.stroke()
            }

            FillStyle.FULL -> {
                ctx.fillStyle = color()
                ctx.fill()
            }
        }
    }

    open fun color(): String = "red"
}

enum class FillStyle {
    OUTLINE, FULL
}