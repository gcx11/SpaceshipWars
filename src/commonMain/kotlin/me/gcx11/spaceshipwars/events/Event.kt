package me.gcx11.spaceshipwars.events

abstract class Event

val packetEventHandler = EventHandler<PacketEvent>()
val keyPressEventHandler = EventHandler<KeyPressEvent>()
val clientConnectEventHandler = EventHandler<ClientConnectEvent>()
val clientDisconnectEventHandler = EventHandler<ClientDisconnectEvent>()
val gameTickEventHandler = EventHandler<GameTickEvent>()
val mouseDownEventHandler = EventHandler<MouseDownEvent>()
val spawnEntityEventHandler = EventHandler<SpawnEntityEvent>()
val removeEntityEventHandler = EventHandler<RemoveEntityEvent>()
val collisionEventHandler = EventHandler<CollisionEvent>()