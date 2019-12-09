package kotlinx.coroutines.guide.exceptions02

import kotlinx.coroutines.*

fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("Caught $exception")
    }
    val job = GlobalScope.launch(handler) {
        throw AssertionError()
    }
    val deferred = GlobalScope.async(handler) {
        throw ArithmeticException() // Nothing will be printed, relying on user to call deferred.await()
    }
    joinAll(job, deferred)
}


//这段代码的输出如下：
//Caught java.lang.AssertionError
//但是如果不想将所有的异常打印在控制台中呢？ CoroutineExceptionHandler 上下文元素被用来将通用的 catch 代码块用于在协程中自定义日志记录或异常处理。 它和使用 Thread.uncaughtExceptionHandler 很相似。
//
//在 JVM 中可以重定义一个全局的异常处理者来将所有的协程通过 ServiceLoader 注册到 CoroutineExceptionHandler。 全局异常处理者就如同 Thread.defaultUncaughtExceptionHandler 一样，在没有更多的指定的异常处理者被注册的时候被使用。 在 Android 中， uncaughtExceptionPreHandler 被设置在全局协程异常处理者中。

//CoroutineExceptionHandler 仅在预计不会由用户处理的异常上调用， 所以在 async 构建器中注册它没有任何效果。

























