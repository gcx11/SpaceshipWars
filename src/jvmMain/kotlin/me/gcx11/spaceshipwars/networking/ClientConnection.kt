package me.gcx11.spaceshipwars.networking

import me.gcx11.spaceshipwars.packets.NoopPacket
import me.gcx11.spaceshipwars.packets.Packet
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicLong

class ClientConnection(
    var id: Long
) {
    private val packetBuffer = ConcurrentLinkedDeque<Packet>()

    companion object {
        @JvmStatic
        private val counter = AtomicLong(1L)
    }

    constructor(): this(counter.getAndIncrement())

    fun sendPacket(packet: Packet) {
        packetBuffer.add(packet)
    }

    fun getNextPacket(): Packet {
        return packetBuffer.poll() ?: NoopPacket(id)
    }
}