package kotlinx.coroutines.guide.context06

import kotlinx.coroutines.*

fun main() = runBlocking<Unit> {
    // launch a coroutine to process some kind of incoming request
    val request = launch {
        // it spawns two other jobs, one with GlobalScope
        GlobalScope.launch {
            println("job1: I run in GlobalScope and execute independently!")
            delay(1000)
            println("job1: I am not affected by cancellation of the request")
        }
        // and the other inherits the parent context
        launch {
            delay(100)
            println("job2: I am a child of the request coroutine")
            delay(1000)
            println("job2: I will not execute this line if my parent request is cancelled")
        }
    }
    delay(500)
    request.cancel() // cancel processing of the request
    delay(1000) // delay a second to see what happens
    println("main: Who has survived request cancellation?")
}


//当一个协程被其它协程在 CoroutineScope 中启动的时候， 它将通过 CoroutineScope.coroutineContext 来承袭上下文，
//并且这个新协程的 Job 将会成为父协程作业的 子 作业。
//当一个父协程被取消的时候，所有它的子协程也会被递归的取消。
//
//然而，当使用 GlobalScope 来启动一个协程时，则新协程的作业没有父作业。 因此它与这个启动的作用域无关且独立运作。
//
//
//这段代码的输出如下：
//
//ob1: I run in GlobalScope and execute independently!
//job2: I am a child of the request coroutine
//job1: I am not affected by cancellation of the request
//main: Who has survived request cancellation?




























