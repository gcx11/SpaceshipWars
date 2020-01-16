package me.gcx11.spaceshipwars.packets

import kotlinx.io.core.IoBuffer
import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes

sealed class Packet(val id: Byte)
class NoopPacket(val clientId: Long): Packet(1)
class ClientJoinPacket(val clientId: Long): Packet(2)
class RespawnRequestPacket(val clientId: Long): Packet(3)
class SpaceshipSpawnPacket(val clientId: Long, val entityId: Long, val x: Float, val y: Float): Packet(4)
class SpaceshipPositionPacket(val entityId: Long, val x: Float, val y: Float): Packet(5)
class MoveRequestPacket(val clientId: Long, val entityId: Long, val direction: Int): Packet(6)
class EntityRemovePacket(val entityId: Long): Packet(7)

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
        }

        is MoveRequestPacket -> withBuffer {
            writeByte(packet.id)
            writeLong(packet.clientId)
            writeLong(packet.entityId)
            writeInt(packet.direction)
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
                SpaceshipPositionPacket(readLong(), readFloat(), readFloat())
            }

            6.toByte() -> {
                MoveRequestPacket(readLong(), readLong(), readInt())
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