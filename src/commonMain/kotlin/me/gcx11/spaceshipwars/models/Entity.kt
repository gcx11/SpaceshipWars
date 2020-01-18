package me.gcx11.spaceshipwars.models

import me.gcx11.spaceshipwars.components.Component

class Entity(
    var id: Long = 0,
    val components: MutableList<Component> = mutableListOf()
) {
    companion object {
        private var currentId = 0L
    }

    init {
        id = ++currentId
    }

    constructor(entityId: Long): this() {
        this.id = entityId
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
        return buildString {
            append("Entity $id with ")
            components.forEach {
                append("\n\t")
                append(it.toString())
            }
        }
    }
}