package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.components.ClientComponent
import me.gcx11.spaceshipwars.components.GeometricComponent
import me.gcx11.spaceshipwars.geometry.Point
import me.gcx11.spaceshipwars.models.Entity

actual object SpaceshipFactory {
    fun create(entityId: Long, x: Float, y: Float, clientId: Long): Entity {
        return Entity(entityId).apply {
            // TODO fix
            addComponent(object : GeometricComponent {
                override var x = x
                override var y = y
                override val parent = this@apply
                override val shape get() = Point(x, y)
            })
            addComponent(ShapeRenderableComponent(this))
            addComponent(
                ClientComponent(
                    this,
                    clientId
                )
            )
        }
    }
}