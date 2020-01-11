package me.gcx11.spaceshipwars.events

class EventHandler<T> {
    private val listeners = mutableSetOf<(T) -> Unit>()
    private val toAdd = mutableSetOf<(T) -> Unit>()
    private val toRemove = mutableSetOf<(T) -> Unit>()

    operator fun plusAssign(func: (T) -> Unit) {
        toAdd.add(func)
    }

    operator fun minusAssign(func: (T) -> Unit) {
        toRemove.add(func)
    }

    fun removeAll() {
        toAdd.clear()
        toRemove.clear()
        listeners.clear()
    }

    operator fun invoke(value: T) {
        listeners.removeAll(toRemove)
        toRemove.clear()
        listeners.addAll(toAdd)
        toAdd.clear()

        listeners.forEach { it(value) }
    }
}