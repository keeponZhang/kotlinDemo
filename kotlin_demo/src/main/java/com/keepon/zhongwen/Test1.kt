package com.keepon.zhongwen

import com.keepon.coroutines.utils.log
import kotlinx.coroutines.*

/**
 * createBy	 keepon
 */
fun main() = runBlocking {
//   test1()


//    test2()
//    test3()
    test4()
}

private suspend fun test4() {
    withTimeout(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
//            "Done"  只加Done也会抛异常
        }
    }
}

private suspend fun test3() {
    //这个不会抛异常
    val result = withTimeoutOrNull(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
        log("Done")
        "Done" // 在它运行得到结果之前取消它
    }
    println("Result is $result")
}

private suspend fun CoroutineScope.test2() {
    val job = launch {
        try {
            repeat(1000) { i ->
                println("job: I'm sleeping $i ...")
                delay(500L)//
            }
        } catch (e: Exception) {
            log("e："+e)
        } finally {
            delay(1)
            println("job: I'm running finally")
        }
    }
    delay(1300L) // 延迟一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消该作业并且等待它结束
    println("main: Now I can quit.")
}

private suspend fun CoroutineScope.test1() {
    val startTime = System.currentTimeMillis()
    val job = launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (i < 5) { // 一个执行计算的循环，只是为了占用 CPU
            // 每秒打印消息两次
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: I'm sleeping ${i++} ...")

                delay(400)
                nextPrintTime += 500L
//                try {
//                    delay(10)
//                }catch (e:Exception){
//                    log("exception "+e)
//                }finally {
//                    println("job: I'm running finally")
//                }
            }

        }
    }
    delay(1300L) // 等待一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消一个作业并且等待它结束
    println("main: Now I can quit.")
}