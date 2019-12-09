package com.keepon.benyhuo.异常4_2异常传播.kt

import com.keepon.coroutines.utils.log
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * createBy	keepon
 */
suspend fun main() {
    log(1)
    try {
        supervisorScope() {
            //①
            log(2)
            launch {
                // ②
                log(3)
                launch {
                    // ③
                    log(4)
                    delay(100)
                    throw ArithmeticException("Hey!!")
                }
                log(5)
            }
            log(6)
            val job = launch {
                // ④
                log(7)
                try {
                    delay(1000)
                } catch (e: java.lang.Exception) {
                    log("④ Exception " + e.message)
                }

            }
            try {
                log(8)
                log("job is Active " + job.isActive)
                job.join()
                log("9")
            } catch (e: Exception) {
                log("10. $e")
            }
        }
        log(11)
    } catch (e: Exception) {
        log("12. $e")
    }
    log(13)
}

//如果我们把 coroutineScope 换成 supervisorScope，其他不变，运行结果会是怎样呢？
//11:52:48:632 [main] 1
//11:52:48:694 [main] 2
//11:52:48:875 [main] 6
//11:52:48:892 [DefaultDispatcher-worker-1 @coroutine#1] 3
//11:52:48:895 [DefaultDispatcher-worker-1 @coroutine#1] 5
//11:52:48:900 [DefaultDispatcher-worker-3 @coroutine#3] 4
//11:52:48:905 [DefaultDispatcher-worker-2 @coroutine#2] 7
//11:52:48:907 [main] 8
//Exception in thread "DefaultDispatcher-worker-3 @coroutine#3" java.lang.ArithmeticException: Hey!!
//at com.bennyhuo.coroutines.sample2.exceptions.ScopesKt$main$2$1$1.invokeSuspend(Scopes.kt:17)
//at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
//at kotlinx.coroutines.DispatchedTask.run(Dispatched.kt:238)
//at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:594)
//at kotlinx.coroutines.scheduling.CoroutineScheduler.access$runSafely(CoroutineScheduler.kt:60)
//at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:742)
//11:52:49:915 [DefaultDispatcher-worker-3 @coroutine#2] 9
//11:52:49:915 [DefaultDispatcher-worker-3 @coroutine#2] 11
//11:52:49:915 [DefaultDispatcher-worker-3 @coroutine#2] 13

//我们可以看到，1-8 的输出其实没有本质区别，顺序上的差异是线程调度的前后造成的，并不会影响协程的语义。
//差别主要在于 9 与 10、11与12的区别，如果把 scope 换成 supervisorScope，
//我们发现 ③ 的异常并没有影响作用域以及作用域内的其他子协程的执行，也就是我们所说的“自作自受”。
