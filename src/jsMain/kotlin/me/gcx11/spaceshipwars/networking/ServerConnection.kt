package me.gcx11.spaceshipwars.networking

import me.gcx11.spaceshipwars.ClientState
import me.gcx11.spaceshipwars.UUID
import me.gcx11.spaceshipwars.packets.MoveRequestPacket
import me.gcx11.spaceshipwars.packets.NoopPacket
import me.gcx11.spaceshipwars.packets.Packet

class ServerConnection {
    private val packetBuffer = mutableListOf<Packet>()
    var id: UUID = UUID.new() // TODO zero UUID?
    var clientState = ClientState.CONNECTING

    fun sendPacket(packet: Packet) {
        if (packet is MoveRequestPacket) {
            for ((index, value) in packetBuffer.withIndex()) {
                if (value is MoveRequestPacket && value.entityId == packet.entityId) {
                    packetBuffer[index] = packet
                    return
                }
            }
        }

        packetBuffer.add(packet)
    }

    fun getNextPacket(): Packet {
        // TODO use better data structure
        if (packetBuffer.isNotEmpty()) return packetBuffer.removeAt(0)

        return NoopPacket(id)
    }
}