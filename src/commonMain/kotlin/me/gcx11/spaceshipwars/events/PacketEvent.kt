package me.gcx11.spaceshipwars.events

import me.gcx11.spaceshipwars.packets.Packet

class PacketEvent(
    val packet: Packet
): Event()