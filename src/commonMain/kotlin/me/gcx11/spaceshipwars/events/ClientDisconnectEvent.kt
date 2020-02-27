package me.gcx11.spaceshipwars.events

import me.gcx11.spaceshipwars.UUID

class ClientDisconnectEvent(
    val clientId: UUID
): Event()