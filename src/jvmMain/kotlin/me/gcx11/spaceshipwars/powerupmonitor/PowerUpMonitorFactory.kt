package me.gcx11.spaceshipwars.powerupmonitor

import me.gcx11.spaceshipwars.models.Entity

object PowerUpMonitorFactory {
    fun create(): Entity {
        return Entity().apply {
            addComponent(CheckWorldComponent(this))
        }.apply {
            this.externalId = this.internalId
            this.tag = "PowerUpMonitor"
        }
    }
}