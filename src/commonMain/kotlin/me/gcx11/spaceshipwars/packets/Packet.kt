package me.gcx11.spaceshipwars.packets

import kotlinx.io.core.IoBuffer
import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes

sealed class Packet(val id: Byte)
data class NoopPacket(val clientId: Long): Packet(1)
data class ClientJoinPacket(val clientId: Long): Packet(2)
data class RespawnRequestPacket(val clientId: Long): Packet(3)
data class SpaceshipSpawnPacket(val clientId: Long, val entityId: Long, val x: Float, val y: Float): Packet(4)
data class SpaceshipPositionPacket(val entityId: Long, val x: Float, val y: Float, val direction: Float): Packet(5)
data class MoveRequestPacket(val clientId: Long, val entityId: Long, val speed: Float, val direction: Float): Packet(6)
data class EntityRemovePacket(val entityId: Long): Packet(7)

private val pool = IoBuffer.Pool

fun serialize(packet: Packet): ByteArray {
    return when (packet) {
        is NoopPacket -> withBuffer {
            writeByte(packet.id)
            writeLong(packet.clientId)
        }

        is ClientJoinPacket -> withBuffer {
            writeByte(packet.id)
            writeLong(packet.clientId)
        }

        is RespawnRequestPacket -> withBuffer {
            writeByte(packet.id)
            writeLong(packet.clientId)
        }

        is SpaceshipSpawnPacket -> withBuffer {
            writeByte(packet.id)
            writeLong(packet.clientId)
            writeLong(packet.entityId)
            writeFloat(packet.x)
            writeFloat(packet.y)
        }

        is SpaceshipPositionPacket -> withBuffer {
            writeByte(packet.id)
            writeLong(packet.entityId)
            writeFloat(packet.x)
            writeFloat(packet.y)
            writeFloat(packet.direction)
        }

        is MoveRequestPacket -> withBuffer {
            writeByte(packet.id)
            writeLong(packet.clientId)
            writeLong(packet.entityId)
            writeFloat(packet.speed)
            writeFloat(packet.direction)
        }

        is EntityRemovePacket -> withBuffer {
            writeByte(packet.id)
            writeLong(packet.entityId)
        }
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
                NoopPacket(readLong())
            }

            2.toByte() -> {
                ClientJoinPacket(readLong())
            }

            3.toByte() -> {
                RespawnRequestPacket(readLong())
            }

            4.toByte() -> {
                SpaceshipSpawnPacket(readLong(), readLong(), readFloat(), readFloat())
            }

            5.toByte() -> {
                SpaceshipPositionPacket(readLong(), readFloat(), readFloat(), readFloat())
            }

            6.toByte() -> {
                MoveRequestPacket(readLong(), readLong(), readFloat(), readFloat())
            }

            7.toByte() -> {
                EntityRemovePacket(readLong())
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