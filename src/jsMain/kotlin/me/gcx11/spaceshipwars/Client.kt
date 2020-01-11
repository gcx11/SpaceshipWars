package me.gcx11.spaceshipwars

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.awaitAnimationFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.gcx11.spaceshipwars.events.PacketEvent
import me.gcx11.spaceshipwars.events.packetEventHandler
import me.gcx11.spaceshipwars.networking.ServerConnection
import me.gcx11.spaceshipwars.packets.*
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document
import kotlin.browser.window

val serverConnection = ServerConnection()

fun main() {
    window.onload = {
        val context = createCanvas()
        launchGameloop(context)
        launchNetworking()
    }
}

fun createCanvas(): CanvasRenderingContext2D {
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    val context = canvas.getContext("2d") as CanvasRenderingContext2D
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
    document.body!!.appendChild(canvas)

    return context
}

fun clearCanvas(context: CanvasRenderingContext2D) {
    context.fillStyle = "#000"

    context.fillRect(
        0.0,
        0.0,
        context.canvas.width.toDouble(),
        context.canvas.height.toDouble()
    )
}

fun draw(context: CanvasRenderingContext2D) {
    context.font = "30px Arial"
    context.fillStyle = "white"
    context.fillText("Client id: ${serverConnection.id}", 100.0, 100.0)
}

fun launchGameloop(context: CanvasRenderingContext2D) {
    packetEventHandler += { event ->
        if (event.packet is NoopPacket) {
            // do nothing
        }
    }

    packetEventHandler += { event ->
        val packet = event.packet
        if (packet is ClientJoinPacket) {
            serverConnection.id = packet.clientId
        }
    }

    GlobalScope.launch {
        while (true) {
            window.awaitAnimationFrame()
            val packets = PacketQueue.incoming.freeze()

            for (packet in packets) {
                processPacket(packet)
            }

            PacketQueue.incoming.unfreeze()
            clearCanvas(context)
            draw(context)
            delay(10L)
        }
    }
}

fun processPacket(packet: Packet) {
    packetEventHandler(PacketEvent(packet))
}

@UseExperimental(KtorExperimentalAPI::class)
fun launchNetworking() {
    GlobalScope.launch {
        val client = HttpClient(Js) {
            install(WebSockets)
        }

        client.ws(
            method = HttpMethod.Get,
            host = "127.0.0.1",
            port = 8080, path = "/ws"
        ) {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Binary -> {
                        val incomingPacket = deserialize(frame.readBytes())!!
                        PacketQueue.incoming.push(incomingPacket)
                    }
                }

                val packet = serverConnection.getNextPacket()
                send(Frame.Binary(true, serialize(packet)))
            }
        }
    }
}