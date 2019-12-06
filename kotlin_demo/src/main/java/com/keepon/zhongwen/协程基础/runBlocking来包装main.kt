package kotlinx.coroutines.guide.basic02b

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    // start main coroutine
    GlobalScope.launch {
        // launch a new coroutine in background and continue
        delay(1000L)
        println("World!")
    }
    println("Hello,") // main coroutine continues here immediately
    delay(2000L)      // delaying for 2 seconds to keep JVM alive
}
//这里的 runBlocking<Unit> { …… } 作为用来启动顶层主协程的适配器。 我们显式指定了其返回类型 Unit，因为在 Kotlin 中 main 函数必须返回 Unit 类型。