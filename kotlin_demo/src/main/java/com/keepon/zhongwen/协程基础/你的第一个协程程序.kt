package kotlinx.coroutines.guide.basic01

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun main() {
    GlobalScope.launch {
        // launch a new coroutine in background and continue
//        delay(1000L) // non-blocking delay for 1 second (default time unit is ms)
        println("World!") // print after delay
    }
    println("Hello,") // main thread continues while coroutine is delayed
    Thread.sleep(2000L) // block main thread for 2 seconds to keep JVM alive
}

//本质上，协程是轻量级的线程。 它们在某些 CoroutineScope 上下文中与 launch 协程构建器 一起启动。
// 这里我们在 GlobalScope 中启动了一个新的协程，这意味着新协程的生命周期只受整个应用程序的生命周期限制。
//
//可以将 GlobalScope.launch { …… } 替换为 thread { …… }，将 delay(……) 替换为 Thread.sleep(……) 达到同样目的。 尝试一下。
//
//如果你首先将 GlobalScope.launch 替换为 thread，编译器会报以下错误：
//
//Error: Kotlin: Suspend functions are only allowed to be called from a coroutine or another suspend function
//这是因为 delay 是一个特殊的 挂起函数 ，它不会造成线程阻塞，但是会 挂起 协程，并且只能在协程中使用