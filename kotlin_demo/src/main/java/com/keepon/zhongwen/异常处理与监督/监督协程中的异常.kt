package kotlinx.coroutines.guide.supervision03

import kotlin.coroutines.*
import kotlinx.coroutines.*
//常规的作业和监督作业之间的另一个重要区别是异常处理。 监督协程中的每一个子作业应该通过异常处理机制处理自身的异常。 这种差异来自于子作业的执行失败不会传播给它的父作业的事实。
fun main() = runBlocking {
    val handler = CoroutineExceptionHandler { _, exception ->
        println("Caught $exception")
    }
    supervisorScope {
        val child = launch(handler) {
            println("Child throws an exception")
            throw AssertionError()
        }
        println("Scope is completing")
    }
    println("Scope is completed")
}

//这段代码的输出如下：
//Scope is completing
//Child throws an exception
//Caught java.lang.AssertionError
//Scope is completed