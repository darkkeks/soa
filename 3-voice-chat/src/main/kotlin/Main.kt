import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.ByteBuffer
import kotlin.time.Duration.Companion.seconds

suspend fun loopbackAudio() = coroutineScope {
    val channel = Channel<ByteBuffer>(capacity = Channel.UNLIMITED)

    launch { AudioUtils.startCapture(channel) }
    launch { AudioUtils.startPlayback(channel) }

    delay(10.seconds)
    channel.close()
}

fun main(): Unit = runBlocking {
    launch { ChatServer().start() }
    launch { ChatClient("kekos").start() }
    delay(1000)
    launch { ChatClient("posos").start() }
}

