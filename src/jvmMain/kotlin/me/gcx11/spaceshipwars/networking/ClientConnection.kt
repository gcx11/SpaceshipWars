package me.gcx11.spaceshipwars.networking

import me.gcx11.spaceshipwars.packets.NoopPacket
import me.gcx11.spaceshipwars.packets.Packet
import java.util.concurrent.atomic.AtomicLong

class ClientConnection(
    var id: Long
) {
    companion object {
        @JvmStatic
        private val counter = AtomicLong(1L)
    }

    constructor(): this(counter.getAndIncrement())

    fun getNextPacket(): Packet {
        return NoopPacket(id)
    }
}