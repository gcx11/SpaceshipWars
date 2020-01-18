package me.gcx11.spaceshipwars.components

import me.gcx11.spaceshipwars.models.Entity

interface Component {
    val parent: Entity
}

inline fun <reified T : Component> Component.getRequiredSibling(): T {
    return parent.getRequiredComponent()
}

inline fun <reified T : Component> Component.getOptionalSibling(): T? {
    return parent.getOptionalComponent()
}