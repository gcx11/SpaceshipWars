package me.gcx11.spaceshipwars.packets

import me.gcx11.spaceshipwars.collections.SwapQueue

object PacketQueue {
    val incoming = SwapQueue<Packet>()
}