package me.gcx11.spaceshipwars.models

import me.gcx11.spaceshipwars.components.Component

class Entity(
    var internalId: Long = 0,
    var externalId: Long = 0,
    val components: MutableList<Component> = mutableListOf()
) {
    var tag = ""

    companion object {
        private var currentId = 0L
    }

    init {
        internalId = ++currentId
    }

    constructor(externalId: Long): this() {
        this.externalId = externalId
    }

    fun addComponent(component: Component) {
        components.add(component)
    }

    inline fun <reified T : Component> hasComponent(): Boolean {
        return components.any { it is T }
    }

    inline fun <reified T : Component> getRequiredComponent(): T {
        return getOptionalComponent()
            ?: throw Exception("Component of type ${T::class.simpleName} not found")
    }

    inline fun <reified T : Component> getOptionalComponent(): T? {
        return components.firstOrNull { it is T } as T?
    }

    inline fun <reified T : Component> getAllComponents(): List<T> {
        return components.mapNotNull { it as? T }
    }

    inline fun <reified T : Component> removeAllComponents() {
        components.removeAll { it is T }
    }

    override fun toString(): String {
        return "Entity $externalId, tag: $tag"
    }
}