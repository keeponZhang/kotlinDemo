package kotlinx.coroutines.guide.exceptions06

import kotlinx.coroutines.*
import java.io.*

fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("Caught original $exception")
    }
    val job = GlobalScope.launch(handler) {
        val inner = launch {
            launch {
                launch {
                    throw IOException()
                }
            }
        }
        try {
            inner.join()
        } catch (e: CancellationException) {
            println("Rethrowing CancellationException with original cause "+e.message) //StandaloneCoroutine is cancelling
            throw e
        }
    }
    job.join()
}
//这段代码的输出如下：















