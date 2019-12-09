package kotlinx.coroutines.guide.exceptions04

import kotlinx.coroutines.*
import java.lang.Exception

fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("Caught $exception")
    }
    val job = GlobalScope.launch(handler) {
        launch {
            // the first child
            try {
                delay(Long.MAX_VALUE)
            } catch (e: Exception) {
                println("e Exception " + e.message) // Parent job is Cancelling
            } finally {
                withContext(NonCancellable) {
                    println(
                        "Children are cancelled, but exception is not handled until all children " +
                            "terminate coroutineContext.isActive " + coroutineContext.isActive +
                            " child1 " + isActive)
                    delay(100)
                    println("The first child finished its non cancellable block " + isActive)
                }
            }
        }
        launch {
            // the second child
            delay(10)

            println("Second child throws an exception")
            throw ArithmeticException()  //会导致父协程取消
            //下面这个不会执行到
            println("Second child after throws an exception")
        }
    }
    job.join()
}

//这段代码的输出如下：
//Second child throws an exception
//Children are cancelled, but exception is not handled until all children terminate
//The first child finished its non cancellable block
//Caught java.lang.ArithmeticException

//这也是为什么，在这个例子中，CoroutineExceptionHandler
//总是被设置在由 GlobalScope 启动的协程中
//。将异常处理者设置在 runBlocking 主作用域内启动的协程中是没有意义的，
//尽管子协程已经设置了异常处理者， 但是主协程也总是会被取消的。
























