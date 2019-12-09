package kotlinx.coroutines.guide.supervision02

import kotlin.coroutines.*
import kotlinx.coroutines.*

fun main() = runBlocking {
    try {
        supervisorScope {
            val child = launch {
                try {
                    println("Child is sleeping")
                    delay(Long.MAX_VALUE)
                } finally {
                    println("Child is cancelled")
                }
            }
            // Give our child a chance to execute and print using yield
            yield()
            println("Throwing exception from scope")
            throw AssertionError()
        }
    } catch(e: AssertionError) {
        println("Caught assertion error")
    }
}

//这段代码的输出如下：
//Child is sleeping
//Throwing exception from scope
//Child is cancelled
//Caught assertion error
//
//对于作用域的并发，supervisorScope 可以被用来替代 coroutineScope 来实现相同的目的。
//它只会单向的传播并且当作业自身执行失败的时候将所有子作业全部取消。
//作业自身也会在所有的子作业执行结束前等待， 就像 coroutineScope 所做的那样。










