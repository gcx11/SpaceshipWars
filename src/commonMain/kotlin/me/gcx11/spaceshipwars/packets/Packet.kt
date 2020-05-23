package me.gcx11.spaceshipwars.packets

import kotlinx.io.core.*
import me.gcx11.spaceshipwars.UUID

sealed class Packet(val id: Byte)
data class NoopPacket(val clientId: UUID): Packet(1)
data class ClientJoinPacket(val clientId: UUID): Packet(2)
data class RespawnRequestPacket(val clientId: UUID, val nickName: String): Packet(3)
data class SpaceshipSpawnPacket(val clientId: UUID, val entityId: Long, val x: Float, val y: Float, val nickName: String): Packet(4)
data class EntityPositionPacket(val positions: List<EntityPosition>): Packet(5)
data class MoveRequestPacket(val clientId: UUID, val entityId: Long, val speed: Float, val direction: Float): Packet(6)
data class EntityRemovePacket(val entityId: Long): Packet(7)
data class FirePacket(val clientId: UUID, val entityId: Long): Packet(8)
data class BulletSpawnPacket(val entityId: Long, val x: Float, val y: Float): Packet(9)

data class EntityPosition(val entityId: Long, val x: Float, val y: Float, val direction: Float)

private val pool = IoBuffer.Pool

fun serialize(packet: Packet): ByteArray {
    return when (packet) {
        is NoopPacket -> withBuffer {
            writeByte(packet.id)
            writeUUID(packet.clientId)
        }

        is ClientJoinPacket -> withBuffer {
            writeByte(packet.id)
            writeUUID(packet.clientId)
        }

        is RespawnRequestPacket -> withBuffer {
            writeByte(packet.id)
            writeUUID(packet.clientId)
            writeText(packet.nickName)
        }

        is SpaceshipSpawnPacket -> withBuffer {
            writeByte(packet.id)
            writeUUID(packet.clientId)
            writeLong(packet.entityId)
            writeFloat(packet.x)
            writeFloat(packet.y)
            writeText(packet.nickName)
        }

        is EntityPositionPacket -> withBuffer {
            writeByte(packet.id)
            writeInt(packet.positions.size)
            for (position in packet.positions) {
                writeLong(position.entityId)
                writeFloat(position.x)
                writeFloat(position.y)
                writeFloat(position.direction)
            }
        }

        is MoveRequestPacket -> withBuffer {
            writeByte(packet.id)
            writeUUID(packet.clientId)
            writeLong(packet.entityId)
            writeFloat(packet.speed)
            writeFloat(packet.direction)
        }

        is EntityRemovePacket -> withBuffer {
            writeByte(packet.id)
            writeLong(packet.entityId)
        }

        is FirePacket -> withBuffer {
            writeByte(packet.id)
            writeUUID(packet.clientId)
            writeLong(packet.entityId)
        }

        is BulletSpawnPacket -> withBuffer {
            writeByte(packet.id)
            writeLong(packet.entityId)
            writeFloat(packet.x)
            writeFloat(packet.y)
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
                NoopPacket(readUUID())
            }

            2.toByte() -> {
                ClientJoinPacket(readUUID())
            }

            3.toByte() -> {
                RespawnRequestPacket(readUUID(), readText())
            }

            4.toByte() -> {
                SpaceshipSpawnPacket(readUUID(), readLong(), readFloat(), readFloat(), readText())
            }

            5.toByte() -> {
                val size = readInt()
                val positions = (0 until size).map {
                    EntityPosition(readLong(), readFloat(), readFloat(), readFloat())
                }

                EntityPositionPacket(positions)
            }

            6.toByte() -> {
                MoveRequestPacket(readUUID(), readLong(), readFloat(), readFloat())
            }

            7.toByte() -> {
                EntityRemovePacket(readLong())
            }

            8.toByte() -> {
                FirePacket(readUUID(), readLong())
            }

            9.toByte() -> {
                BulletSpawnPacket(readLong(), readFloat(), readFloat())
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

private fun IoBuffer.writeUUID(uuid: UUID) {
    this.writeLong(uuid.getHigh())
    this.writeLong(uuid.getLow())
}

private fun IoBuffer.readUUID(): UUID {
    return UUID.from(readLong(), readLong())
}

private fun IoBuffer.writeText(text: String) {
    val bytes = text.toByteArray()

    this.writeInt(bytes.size)
    this.writeFully(bytes, 0, bytes.size)
}

private fun IoBuffer.readText(): String {
    val bytesSize = readInt()
    val byteArray = ByteArray(bytesSize)
    readFully(byteArray, 0, bytesSize)

    return String(byteArray)
}