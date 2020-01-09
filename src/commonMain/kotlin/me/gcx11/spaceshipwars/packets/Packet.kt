package me.gcx11.spaceshipwars.packets

import kotlinx.io.core.IoBuffer
import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes

sealed class Packet(val id: Byte)
class NumberPacket(val number: Long): Packet(1)
class SpawnRequestPacket: Packet(2)
class SpawnResponsePacket(val entityId: Int, val x: Float, val y: Float): Packet(3)

private val pool = IoBuffer.Pool

fun serialize(packet: Packet): ByteArray {
    return when (packet) {
        is NumberPacket -> withBuffer {
            writeByte(packet.id)
            writeLong(packet.number)
        }

        else -> ByteArray(0)
    }
}

fun deserialize(byteArray: ByteArray): Packet? {
    if (byteArray.isEmpty()) return null

    var packet: Packet? = null

    withBuffer {
        writeFully(byteArray, 0, byteArray.size)

        val id = readByte()
        packet = when (id) {
            1.toByte() -> {
                NumberPacket(readLong())
            }

            else -> null
        }
    }

    return packet
}

private fun withBuffer(body: IoBuffer.() -> Unit): ByteArray {
    val buffer = pool.borrow()
    buffer.body()

    val result = buffer.readBytes()
    buffer.release(pool)
    return result
}