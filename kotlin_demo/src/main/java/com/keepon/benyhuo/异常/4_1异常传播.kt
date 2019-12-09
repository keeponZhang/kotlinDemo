package com.keepon.benyhuo.异常4异常传播.kt

import com.keepon.coroutines.utils.log
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * createBy	keepon
 */
suspend fun main() {
    log(1)
    try {
        coroutineScope {
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

//11:37:36:208 [main] 1
//11:37:36:255 [main] 2
//11:37:36:325 [DefaultDispatcher-worker-1] 3
//11:37:36:325 [DefaultDispatcher-worker-1] 5
//11:37:36:326 [DefaultDispatcher-worker-3] 4
//11:37:36:331 [main] 6
//11:37:36:336 [DefaultDispatcher-worker-1] 7
//11:37:36:336 [main] 8
//11:37:36:441 [DefaultDispatcher-worker-1] 10. kotlinx.coroutines.JobCancellationException: ScopeCoroutine is cancelling; job=ScopeCoroutine{Cancelling}@2bc92d2f
//11:37:36:445 [DefaultDispatcher-worker-1] 12. java.lang.ArithmeticException: Hey!!
//11:37:36:445 [DefaultDispatcher-worker-1] 13
//
//作者：bennyhuo
//链接：http://www.imooc.com/article/286070
//来源：慕课网
//本文原创发布于慕课网 ，转载请注明出处，谢谢合作

//注意两个位置，一个是 10，我们调用 join，收到了一个取消异常，在协程当中支持取消的操作的suspend方法在取消时会抛出一个
//CancellationException，这类似于线程中对 InterruptException 的响应，遇到这种情况表示 join 调用所在的协程已经被取消了，
//那么这个取消究竟是怎么回事呢？
//
//原来协程 ③ 抛出了未捕获的异常，进入了异常完成的状态，它与父协程 ② 之间遵循默认的作用域规则
//，因此 ③ 会通知它的父协程也就是 ② 取消，② 根据作用域规则通知父协程 ① 也就是整个作用域取消，这是一个自下而上的一次传播，
//这样身处 ① 当中的 job.join 调用就会抛异常，也就是 10 处的结果了。如果不是很理解这个操作，想一下我们说到的，
//coroutineScope 内部启动的协程就是“一损俱损”。实际上由于父协程 ① 被取消，协程④ 也不能幸免，如果大家有兴趣的话，
//也可以对 ④ 当中的 delay进行捕获，一样会收获一枚取消异常。
//
//还有一个位置就是 12，这个是我们对 coroutineScope 整体的一个捕获，如果 coroutineScope 内部以为异常而结束，
//那么我们是可以对它直接 try ... catch ... 来捕获这个异常的，这再一次表明协程把异步的异常处理到同步代码逻辑当中。




