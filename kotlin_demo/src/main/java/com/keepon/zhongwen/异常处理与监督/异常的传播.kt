package kotlinx.coroutines.guide.exceptions01

import kotlinx.coroutines.*


//程构建器有两种风格：自动的传播异常（launch 以及 actor） 或者将它们暴露给用户（async 以及 produce）。
//前者对待异常是不处理的，类似于 Java 的 Thread.uncaughtExceptionHandler，
//而后者依赖用户来最终消耗异常，比如说，通过 await 或 receive （produce 以及 receive 在通道中介绍过）。
fun main() = runBlocking {
    val job = GlobalScope.launch {
        println("Throwing exception from launch")
        throw IndexOutOfBoundsException() // Will be printed to the console by Thread.defaultUncaughtExceptionHandler
    }
    job.join()
    println("Joined failed job")
    val deferred = GlobalScope.async {
        println("Throwing exception from async")
        throw ArithmeticException() // Nothing is printed, relying on user to call await
    }
//    try {
//        deferred.await()
//        println("Unreached")
//    } catch (e: ArithmeticException) {
//        println("Caught ArithmeticException")
//    }
}
