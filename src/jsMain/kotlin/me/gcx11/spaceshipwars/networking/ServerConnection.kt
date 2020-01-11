package me.gcx11.spaceshipwars.networking

import me.gcx11.spaceshipwars.packets.NoopPacket
import me.gcx11.spaceshipwars.packets.Packet

class ServerConnection {
    private val packetBuffer = mutableListOf<Packet>()
    var id: Long = 0L

    fun sendPacket(packet: Packet) {
        packetBuffer.add(packet)
    }

    fun getNextPacket(): Packet {
        // TODO use better data structure
        if (packetBuffer.isNotEmpty()) return packetBuffer.removeAt(0)

        return NoopPacket(id)
    }
}