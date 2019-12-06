package kotlinx.coroutines.guide.context05

import kotlinx.coroutines.*

fun main() = runBlocking<Unit> {
    println("My job is ${coroutineContext[Job]}")
}

//在调试模式下，它将输出如下这些信息：
//
//My job is "coroutine#1":BlockingCoroutine{Active}@6d311334
//请注意，CoroutineScope 中的 isActive 只是 coroutineContext[Job]?.isActive == true 的一种方便的快捷方式。






