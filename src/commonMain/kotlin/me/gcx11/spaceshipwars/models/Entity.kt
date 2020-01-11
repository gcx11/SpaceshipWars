package me.gcx11.spaceshipwars.models

open class Entity(
    clientId: Long
) {
    var id: Long = 0

    companion object {
        private var currentId = 0L
    }

    init {
        id = ++currentId
    }

    constructor(clientId: Long, entityId: Long): this(clientId) {
        this.id = entityId
    }
}