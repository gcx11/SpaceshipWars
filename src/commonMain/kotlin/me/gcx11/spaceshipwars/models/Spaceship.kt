package me.gcx11.spaceshipwars.models

data class Spaceship(var clientId: Long, var x: Float, var y: Float): Entity(clientId) {
    constructor(clientId: Long, entityId: Long, x: Float, y: Float): this(clientId, x, y) {
        this.id = entityId
    }
}