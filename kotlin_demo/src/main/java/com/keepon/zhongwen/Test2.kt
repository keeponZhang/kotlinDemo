package com.keepon.zhongwen.test2

import com.keepon.coroutines.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

/**
 * createBy	 keepon
 */

//https://www.kotlincn.net/docs/reference/coroutines/composing-suspending-functions.html
suspend fun main() {
    //    test1()
//    test2()

//    test3()

//    test4()
    concurrentSum()
}

suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { doSomethingUsefulOne2() }
    val two = async { doSomethingUsefulTwo2() }
    one.await() + two.await()
}

suspend fun doSomethingUsefulOne2(): Int {
    delay(1000L) // 假设我们在这里做了一些有用的事
    log("doSomethingUsefulOne")
    throw RuntimeException("keepon")
    return 13
}

suspend fun doSomethingUsefulTwo2(): Int {
    delay(700L) // 假设我们在这里也做了一些有用的事
    log("doSomethingUsefulTwo")
    return 29
}

private fun test4() {
    val time = measureTimeMillis {
        // 我们可以在协程外面启动异步执行
        val one = somethingUsefulOneAsync()
        val two = somethingUsefulTwoAsync()
        // 但是等待结果必须调用其它的挂起或者阻塞
        // 当我们等待结果的时候，这里我们使用 `runBlocking { …… }` 来阻塞主线程
        runBlocking {
            println("The answer is ${one.await() + two.await()}")
        }
    }
    println("Completed in $time ms")

//    这种带有异步函数的编程风格仅供参考，因为这在其它编程语言中是一种受欢迎的风格。在 Kotlin 的协程中使用这种风格是强烈不推荐的， 原因如下所述。
//    考虑一下如果 val one = somethingUsefulOneAsync() 这一行和 one.await() 表达式这里在代码中有逻辑错误，
//    并且程序抛出了异常以及程序在操作的过程中中止，将会发生什么。 通常情况下，一个全局的异常处理者会捕获这个异常，
//    将异常打印成日记并报告给开发者，但是反之该程序将会继续执行其它操作。但是这里我们的 somethingUsefulOneAsync 仍然在后台执行，
//    尽管如此，启动它的那次操作也会被终止。这个程序将不会进行结构化并发，如下一小节所示。
}

// somethingUsefulOneAsync 函数的返回值类型是 Deferred<Int>
fun somethingUsefulOneAsync() = GlobalScope.async {
    //    throw RuntimeException("keepon")
    doSomethingUsefulOne()
}

// somethingUsefulTwoAsync 函数的返回值类型是 Deferred<Int>
fun somethingUsefulTwoAsync() = GlobalScope.async {
    doSomethingUsefulTwo()
}

private suspend fun CoroutineScope.test3() {
    val time = measureTimeMillis {
        val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
        val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }
        // 执行一些计算
        one.start() // 启动第一个
        two.start() // 启动第二个
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}

private suspend fun CoroutineScope.test2() {
    val time = measureTimeMillis {
        val one = async { doSomethingUsefulOne() }
        val two = async { doSomethingUsefulTwo() }
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}

private suspend fun test1() {
    val time = measureTimeMillis {
        val one = doSomethingUsefulOne()
        val two = doSomethingUsefulTwo()
        println("The answer is ${one + two}")
    }
    println("Completed in $time ms")
}

suspend fun doSomethingUsefulOne(): Int {
    delay(1000L) // 假设我们在这里做了一些有用的事
    log("doSomethingUsefulOne")
    throw RuntimeException("keepon")
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(700L) // 假设我们在这里也做了一些有用的事
    log("doSomethingUsefulTwo")
    return 29
}






















































