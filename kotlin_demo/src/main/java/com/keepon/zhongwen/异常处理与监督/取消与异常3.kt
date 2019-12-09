package kotlinx.coroutines.guide.exceptions042

import kotlinx.coroutines.*
import java.lang.Exception

val handler = CoroutineExceptionHandler { _, exception ->
    println("Caught $exception")
}

fun main() = runBlocking {
    handler

    val job = GlobalScope.launch() {
        launch {
            // the first child
            try {
                delay(Long.MAX_VALUE)
            } catch (e: Exception) {
                println("e Exception " + e.message) // Parent job is Cancelling
            } finally {
                withContext(NonCancellable) {
                    println(
                        "Children are cancelled, but exception is not handled until all children terminate")
                    delay(100)
                    println("The first child finished its non cancellable block")
                }
            }
        }
        launch {
            // the second child
            delay(10)
            println("Second child throws an exception")
            try {
                throw ArithmeticException()
            } catch (e: Exception) {
//                e.printStackTrace()
            }
        }
    }
    job.join()
}
//这也是为什么，在这个例子中，CoroutineExceptionHandler 总是被设置在由 GlobalScope 启动的协程中。
//将异常处理者设置在 runBlocking 主作用域内启动的协程中是没有意义的，
//尽管子协程已经设置了异常处理者， 但是主协程也总是会被取消的。
//这段代码的输出如下：
//Second child throws an exception
//Children are cancelled, but exception is not handled until all children terminate
//The first child finished its non cancellable block
//Exception in thread "DefaultDispatcher-worker-3" java.lang.ArithmeticException
//at kotlinx.coroutines.guide.exceptions042.取消与异常3Kt$main$1$job$1$2.invokeSuspend(取消与异常3.kt:30)
























