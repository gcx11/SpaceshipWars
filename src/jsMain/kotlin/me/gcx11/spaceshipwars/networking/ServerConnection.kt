package me.gcx11.spaceshipwars.networking

import me.gcx11.spaceshipwars.packets.NoopPacket
import me.gcx11.spaceshipwars.packets.Packet

class ServerConnection() {
    var id: Long = 0L

    val isReady get() = id != 0L

    fun getNextPacket(): Packet {
        return NoopPacket(id)
    }
}