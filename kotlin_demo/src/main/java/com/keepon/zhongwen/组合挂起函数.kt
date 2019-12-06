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
fun main() = runBlocking {
    //     默认顺序调用()
    //  使用async并发()

//    惰性启动的async()

//    async风格的函数()
    async风格的函数抛异常()
//    async的结构化并发并且抛异常()
}

suspend fun async的结构化并发并且抛异常() {
    val time = measureTimeMillis {
        println("The answer is ${concurrentSum()}")
    }
    println("Completed in $time ms")
}

//如果其中一个子协程（即 two）失败，第一个 async 以及等待中的父协程都会被取消：
suspend fun concurrentSum(): Int = coroutineScope {
    val one = async {
        try {
            doSomethingUsefulOne结构化并发()
        } finally {
            println("First child was cancelled")
            42
            "Done"
        }

    }
    val two = async { doSomethingUsefulTwo结构化并发并且抛异常() }
    one.await() + two.await()
}

suspend fun doSomethingUsefulOne结构化并发(): Int {
    log("doSomethingUsefulOne结构化并发  before")
    delay(1000L) // 假设我们在这里做了一些有用的事

    log("doSomethingUsefulOne结构化并发  after")
    return 13
}

suspend fun doSomethingUsefulTwo结构化并发并且抛异常(): Int {
    log("doSomethingUsefulTwo结构化并发并且抛异常  before")
    throw RuntimeException("keepon")
    log("doSomethingUsefulTwo结构化并发并且抛异常  after")
    return 29
}

private fun async风格的函数抛异常() {  //程序会终止,第二个
    val time = measureTimeMillis {
        // 我们可以在协程外面启动异步执行
        val one = somethingUsefulOneAsync抛异常()
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

fun somethingUsefulOneAsync抛异常() = GlobalScope.async {
    //    throw RuntimeException("keepon")
    doSomethingUsefulOne抛异常()
}

private fun async风格的函数() {
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
    doSomethingUsefulOne默认顺序调用()
}

// somethingUsefulTwoAsync 函数的返回值类型是 Deferred<Int>
fun somethingUsefulTwoAsync() = GlobalScope.async {
    doSomethingUsefulTwo默认顺序调用()
}

private suspend fun CoroutineScope.惰性启动的async() {
    val time = measureTimeMillis {
        val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne惰性启动的async() }
        val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne惰性启动的async() }
        // 执行一些计算
        delay(10)
        println("这里先执行")
        delay(1000)
//        one.start() // 启动第一个
//        two.start() // 启动第二个
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}

suspend fun doSomethingUsefulOne惰性启动的async(): Int {
    log("doSomethingUsefulOne惰性启动的async")
    delay(1000L) // 假设我们在这里做了一些有用的事
    return 13
}

suspend fun doSomethingUsefulTwo惰性启动的async(): Int {
    log("doSomethingUsefulTwo惰性启动的async")
    delay(1200L) // 假设我们在这里也做了一些有用的事
    return 29
}

private suspend fun CoroutineScope.使用async并发() {
    val time = measureTimeMillis {
        val one = async { doSomethingUsefulOne使用async并发() }
        val two = async { doSomethingUsefulTwo使用async并发() }
        delay(10)
        println("这里不一定先执行")
        delay(1000)
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}

suspend fun doSomethingUsefulOne使用async并发(): Int {
    log("doSomethingUsefulOne使用async并发")
    delay(1000L) // 假设我们在这里做了一些有用的事
    return 13
}

suspend fun doSomethingUsefulTwo使用async并发(): Int {
    log("doSomethingUsefulTwo使用async并发")
    delay(1200L) // 假设我们在这里也做了一些有用的事
    return 29
}

private suspend fun 默认顺序调用() {
    val time = measureTimeMillis {
        val one = doSomethingUsefulOne默认顺序调用()
        val two = doSomethingUsefulTwo默认顺序调用()
        println("The answer is ${one + two}")
    }
    println("Completed in $time ms")
}

suspend fun doSomethingUsefulOne抛异常(): Int {
    delay(500L) // 假设我们在这里做了一些有用的事
    log("doSomethingUsefulOne抛异常")
    throw RuntimeException("keepon")
    return 13
}

suspend fun doSomethingUsefulOne默认顺序调用(): Int {
    log("doSomethingUsefulOne默认顺序调用")
    delay(1000L) // 假设我们在这里做了一些有用的事
    return 13
}

suspend fun doSomethingUsefulTwo默认顺序调用(): Int {
    log("doSomethingUsefulTwo before 默认顺序调用")
    delay(1000L) // 假设我们在这里也做了一些有用的事
    log("doSomethingUsefulTwo after 默认顺序调用")
    return 29
}






















































