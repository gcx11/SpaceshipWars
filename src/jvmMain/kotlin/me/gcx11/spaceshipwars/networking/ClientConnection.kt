package me.gcx11.spaceshipwars.networking

import kotlinx.atomicfu.locks.withLock
import me.gcx11.spaceshipwars.UUID
import me.gcx11.spaceshipwars.packets.NoopPacket
import me.gcx11.spaceshipwars.packets.Packet
import me.gcx11.spaceshipwars.packets.EntityPositionPacket
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock

class ClientConnection(
    var id: UUID
) {
    private val packetBuffer = ConcurrentLinkedDeque<Packet>()
    private val positionPackets = mutableListOf<Packet>()
    private val lock = ReentrantLock()

    constructor(): this(UUID.new())

    fun sendPacket(packet: Packet) {
        if (packet is EntityPositionPacket) {
            lock.withLock {
                for ((index, value) in positionPackets.withIndex()) {
                    if (value is EntityPositionPacket) {
                        positionPackets[index] = value
                        return
                    }
                }

                positionPackets.add(packet)
            }
        } else {
            packetBuffer.add(packet)
        }
    }

    fun getNextPacket(): Packet {
        val packet = packetBuffer.poll()
        if (packet != null) return packet

        return lock.withLock {
            if (positionPackets.isNotEmpty()) positionPackets.removeAt(0) else NoopPacket(id)
        }
    }
}