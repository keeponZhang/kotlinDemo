package com.keepon.coroutines

import kotlinx.coroutines.*

/**
 * createBy	 keepon
 */
// suspendCancellableCoroutine和 suspendCoroutine 的区别就在这里，如果协程已经被取消或者已完成，就会抛出 CancellationException 异常





//suspend fun <T> Call<T>.await(): T = suspendCoroutine { cont ->
//    enqueue(object : Callback<T> {
//        override fun onResponse(call: Call<T>, response: Response<T>) {
//            if (response.isSuccessful) {
//                cont.resume(response.body()!!)
//            } else {
//                cont.resumeWithException(ErrorResponse(response))
//            }
//        }
//        override fun onFailure(call: Call<T>, t: Throwable) {
//            cont.resumeWithException(t)
//        }
//    })
//}

//suspend  fun main() {
////    delay()
//    yield2()
//
//}
fun main(args: Array<String>) = runBlocking<Unit> {
    launch {
        repeat(3) {
            println("job1 repeat $it times")
            yield()
        }
    }
    launch {
        repeat(3) {
            println("job2 repeat $it times")
            yield()
        }
    }
}
suspend  fun yield2() {
    GlobalScope.launch (){
        repeat(3) {
            println("job1 repeat $it times")
            yield()
        }
        repeat(3) {
            println("job2 repeat $it times")
            yield()
        }
    }

    Thread.sleep(4000)
}
//通过yield()实现 job1 和 job2 两个协程交替运行，输出如下：
//job1 repeat 0 times
//job2 repeat 0 times
//job1 repeat 1 times
//job2 repeat 1 times
//job1 repeat 2 times
//job2 repeat 2 times


//launch(Dispatchers.Unconfined)两个协程输出结果
//job1 repeat 0 times
//job1 repeat 1 times
//job1 repeat 2 times
//job2 repeat 0 times
//job2 repeat 1 times
//job2 repeat 2 times

//现在来看其实现：
//public suspend fun yield(): Unit = suspendCoroutineUninterceptedOrReturn sc@ { uCont ->
//    val context = uCont.context
//    // 检测协程是否已经取消或者完成，如果是的话抛出 CancellationException
//    context.checkCompletion()
//    // 如果协程没有线程调度器，或者像 Dispatchers.Unconfined 一样没有进行调度，则直接返回
//    val cont = uCont.intercepted() as? DispatchedContinuation<Unit> ?: return@sc Unit
//    if (!cont.dispatcher.isDispatchNeeded(context)) return@sc Unit

//    // dispatchYield(Unit) 最终会调用到 dispatcher.dispatch(context, block) 将协程分发到调度器队列中，这样线程可以执行其他协程
//    cont.dispatchYield(Unit)
//    COROUTINE_SUSPENDED
//}
//所以注意到，yield()需要依赖协程的线程调度器，而调度器再次执行该协程时，在第二篇中有讲过会调用resume来恢复协程运行。













private fun delay() {
    GlobalScope.launch(Dispatchers.Unconfined) {
        println("Unconfined      : I'm working in thread ${Thread.currentThread().name}")
        delay(100)
        println("Unconfined      : After delay in thread ${Thread.currentThread().name}")
    }
}


//1.1 delay 的实现
//public suspend fun delay(timeMillis: Long) {
//    if (timeMillis <= 0) return // don't delay
//    return suspendCancellableCoroutine sc@ { cont: CancellableContinuation<Unit> ->
//        cont.context.delay.scheduleResumeAfterDelay(timeMillis, cont)
//    }
//}
//
///** Returns [Delay] implementation of the given context */
//internal val CoroutineContext.delay: Delay get() = get(ContinuationInterceptor) as? Delay ?: DefaultDelay
//
//internal actual val DefaultDelay: Delay = DefaultExecutor

//delay 使用suspendCancellableCoroutine挂起协程，而协程恢复的一般情况下是关键在DefaultExecutor.
//scheduleResumeAfterDelay()，其中实现是schedule(DelayedResumeTask(timeMillis, continuation))，
//其中的关键逻辑是将 DelayedResumeTask 放到 DefaultExecutor 的队列最后，在延迟的时间到达就会执行 DelayedResumeTask，那么该 task 里面的实现是什么：
//override fun run() {
//    // 直接在调用者线程恢复协程
//    with(cont) { resumeUndispatched(Unit) }
//}

//HandlerContext的实现方法实际就是用handler延迟
//override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
//    val block = Runnable {
//        with(continuation) { resumeUndispatched(Unit) }
//    }
//    handler.postDelayed(block, timeMillis.coerceAtMost(MAX_DELAY))
//    continuation.invokeOnCancellation { handler.removeCallbacks(block) }
//}




//1.2 yield 的实现
//yield()的作用是挂起当前协程，然后将协程分发到 Dispatcher 的队列，这样可以让该协程所在线程或线程池可以运行其他协程逻辑
//，然后在 Dispatcher 空闲的时候继续执行原来协程。简单的来说就是让出自己的执行权，给其他协程使用，
//当其他协程执行完成或也让出执行权时，一开始的协程可以恢复继续运行。










































































