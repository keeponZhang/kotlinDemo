package kotlinx.coroutines.guide.exceptions05



import kotlinx.coroutines.*
import java.io.*

fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("Caught $exception with suppressed ${exception.suppressed.contentToString()}")
    }
    val job = GlobalScope.launch(handler) {
        launch {
            try {
                delay(Long.MAX_VALUE)
            } finally {
                throw ArithmeticException()
            }
        }
        launch {
            delay(100)
            throw IOException()
        }
        delay(Long.MAX_VALUE)
    }
    job.join()
}

//这段代码的输出如下：
//Caught java.io.IOException with suppressed [java.lang.ArithmeticException]

//如果一个协程的多个子协程抛出异常将会发生什么？ 通常的规则是“第一个异常赢得了胜利”，所以第一个被抛出的异常将会暴露给处理者。
//但也许这会是异常丢失的原因，比如说一个协程在 finally 块中抛出了一个异常。 这时，多余的异常将会被压制。










