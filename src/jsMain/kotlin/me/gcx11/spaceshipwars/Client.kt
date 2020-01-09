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
import kotlinx.coroutines.launch
import me.gcx11.spaceshipwars.packets.NumberPacket
import me.gcx11.spaceshipwars.packets.deserialize
import me.gcx11.spaceshipwars.packets.serialize
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document
import kotlin.browser.window

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
    // do nothing
}

fun launchGameloop(context: CanvasRenderingContext2D) {
    GlobalScope.launch {
        while (true) {
            window.awaitAnimationFrame()
            clearCanvas(context)
            draw(context)
        }
    }
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
            outgoing.send(Frame.Binary(true, serialize(NumberPacket(1))))

            loop@ for (frame in incoming) {
                when (frame) {
                    is Frame.Binary -> {
                        val incomingPacket = deserialize(frame.readBytes()) as NumberPacket
                        var number = incomingPacket.number

                        println("Received from server: $number")

                        if (number > 1024) {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Bye"))
                            break@loop
                        }

                        outgoing.send(Frame.Binary(true, serialize(NumberPacket(++number))))
                    }
                }
            }
        }
    }
}