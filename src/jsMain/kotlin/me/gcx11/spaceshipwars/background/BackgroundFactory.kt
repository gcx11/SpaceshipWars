package me.gcx11.spaceshipwars.background

import me.gcx11.spaceshipwars.models.Entity

object BackgroundFactory {
    fun create(): Entity {
        return Entity().apply {
            addComponent(BackgroundRenderableComponent(this))
        }
    }
}