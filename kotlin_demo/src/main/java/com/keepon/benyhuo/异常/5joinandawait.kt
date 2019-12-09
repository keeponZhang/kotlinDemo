package com.keepon.benyhuo.异常5joinandawait.kt

import com.keepon.coroutines.utils.log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

/**
 * createBy	keepon
 */
//前面我们举例子一直用的是 launch，启动协程其实常用的还有 async、actor 和 produce，其中 actor 和 launch 的行为类似，
//在未捕获的异常出现以后，会被当做为处理的异常抛出，就像前面的例子那样。而 async 和 produce 则主要是用来输出结果的，
//他们内部的异常只在外部消费他们的结果时抛出。这两组协程的启动器，你也可以认为分别是“消费者”和“生产者”，
//消费者异常立即抛出，生产者只有结果消费时抛出异常。
suspend fun main() {
    val deferred = GlobalScope.async<Int> {
        throw ArithmeticException()
    }
    try {
//        val value = deferred.await()
        val value = deferred.join()
        log("1. $value")
    } catch (e: Exception) {
        log("2. $e")
    }
}

//13:25:14:693 [main] 2. java.lang.ArithmeticException
//

//用join，我们就会发现，异常被吞掉了！它只关心有没有完成，至于怎么完成的它不关心
//[DefaultDispatcher-worker-1 @coroutine#1] 1. kotlin.Unit
