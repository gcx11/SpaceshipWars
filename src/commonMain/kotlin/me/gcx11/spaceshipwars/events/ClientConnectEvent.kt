package me.gcx11.spaceshipwars.events

import me.gcx11.spaceshipwars.UUID

class ClientConnectEvent(
    val clientId: UUID
): Event()