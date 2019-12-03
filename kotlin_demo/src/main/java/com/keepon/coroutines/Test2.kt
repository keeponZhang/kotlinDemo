package com.keepon.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * createBy	 keepon
 */






fun main() {
    //Dispatchers.IO 也是用的DefaultDispatcher（通过createScheduler方法创建的线程池）
//    public fun blocking(parallelism: Int = BLOCKING_DEFAULT_PARALLELISM): CoroutineDispatcher {
//        require(parallelism > 0) { "Expected positive parallelism level, but have $parallelism" }
//        return LimitingDispatcher(this, parallelism, TaskMode.PROBABLY_BLOCKING)
//    }
    GlobalScope.launch(Dispatchers.IO){ // 默认继承 parent coroutine 的 CoroutineDispatcher，运行commonPool
        println("main runBlocking: I'm working in thread ${Thread.currentThread().name}")
        delay(100)
        println("main runBlocking: After delay in thread ${Thread.currentThread().name}")
    }
    GlobalScope.launch(Dispatchers.Unconfined) {
        println("Unconfined      : I'm working in thread ${Thread.currentThread().name}")
        delay(100)
        println("Unconfined      : After delay in thread ${Thread.currentThread().name}")
    }
    Thread.sleep(30000)

}

//Unconfined      : I'm working in thread main
//main runBlocking: I'm working in thread DefaultDispatcher-worker-1
//Unconfined      : After delay in thread kotlinx.coroutines.DefaultExecutor
//main runBlocking: After delay in thread DefaultDispatcher-worker-2

//经过delay挂起函数后，使用Dispatchers.Unconfined的协程挂起恢复后依然在delay函数使用的DefaultExecutor上。