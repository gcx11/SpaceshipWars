package me.gcx11.spaceshipwars.components

interface HealthComponent: Component {
    fun getHealth(): Int

    fun applyDamage(damage: Int)

    fun isAlive(): Boolean
}