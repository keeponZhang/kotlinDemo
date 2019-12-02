package com.keepon.coroutines

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * createBy	 keepon
 */
fun main(args: Array<String>) {
    GlobalScope.launch { // 默认继承 parent coroutine 的 CoroutineDispatcher，运行commonPool
        println("main runBlocking: I'm working in thread ${Thread.currentThread().name}")
        delay(100)
        println("main runBlocking: After delay in thread ${Thread.currentThread().name}")
    }
    Thread.sleep(30000)

}

