package kotlinx.coroutines.guide.context03

import kotlinx.coroutines.*

fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

fun main() = runBlocking<Unit> {
    val a = async {
        log("I'm computing a piece of the answer")
        6
    }
    val b = async {
        log("I'm computing another piece of the answer")
        7
    }
    log("The answer is ${a.await() * b.await()}")
}

//[main @coroutine#2] I'm computing a piece of the answer
//[main @coroutine#3] I'm computing another piece of the answer
//[main @coroutine#1] The answer is 42

//这里有三个协程，包括 runBlocking 内的主协程 (#1) ， 以及计算延期的值的另外两个协程 a (#2) 和 b (#3)。
//它们都在 runBlocking 上下文中执行并且被限制在了主线程内



















