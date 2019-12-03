package com.keepon.coroutines.test5

import com.keepon.coroutines.bean.User
import com.keepon.coroutines.interfaces.Callback
import com.keepon.coroutines.utils.log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * createBy	 keepon
 */


//fun main() = runBlocking {
//    val job1 = launch { // ①
//        log(1)
//        delay(1000) // ②
//        log(2)
//    }
//    delay(100)
//    log(3)
//    job1.cancel() // ③
//    log(4)
//}

//fun main() = runBlocking {
//    val job1 = launch { // ①
//        log(1)
//        try {
//            delay(1000) // ②
//        }catch (e:Exception){
//            log("cancelled. $e")
//        }
//
//        log(2)
//    }
//    delay(100)
//    log(3)
//    job1.cancel() // ③
//    log(4)
//}

//这次我们用了一个不一样的写法，我们没有用 suspend main，而是直接用 runBlocking 启动协程，
//这个方法在 Native 上也存在，都是基于当前线程启动一个类似于 Android 的 Looper 的死循环，或者叫消息队列，
//可以不断的发送消息给它进行处理。runBlocking 会启动一个 Job，因此这里也存在默认的作用域，不过这对于我们今天的讨论暂时没有太大影响。
//
//这段代码 ① 处启动了一个子协程，它内部先输出 1，
//接着开始 delay， delay 与线程的 sleep 不同，它不会阻塞线程，你可以认为它实际上就是触发了一个延时任务，
//告诉协程调度系统 1000ms 之后再来执行后面的这段代码（也就是 log(2)）；而在这期间，
//我们在 ③ 处对刚才启动的协程触发了取消，因此在 ② 处的 delay 还没有回调的时候协程就被取消了，因为 delay 可以响应取消，
//因此 delay 后面的代码就不会再次调度了，不调度的原因也很简单，② 处的 delay 会抛一个 CancellationException：

//06:54:56:361 [main] 1
//06:54:56:408 [main] 3
//06:54:56:411 [main] 4
//06:54:56:413 [main] cancelled. kotlinx.coroutines.JobCancellationException: Job was cancelled; job=StandaloneCoroutine{Cancelling}@e73f9ac
//06:54:56:413 [main] 2




suspend fun getUserCoroutine() = suspendCoroutine<User> { continuation ->
    getUser(object : Callback<User> {
        override fun onSuccess(value: User) {
            continuation.resume(value)
        }

        override fun onError(t: Throwable) {
            continuation.resumeWithException(t)
        }
    })
}



fun getUser(callback: Callback<User>) {
    val call = OkHttpClient().newCall(
            Request.Builder()
                    .get().url("https://api.github.com/users/bennyhuo")
                    .build())

    call.enqueue(object : okhttp3.Callback {

        override fun onFailure(call: Call, e: IOException) {
            callback.onError(e)
        }

        override fun onResponse(call: Call, response:Response) {
            response.body()?.let {
                try {
                    callback.onSuccess(User.from(it.string()))
                } catch (e: Exception) {
                    callback.onError(e) // 这里可能是解析异常
                }
            }?: callback.onError(NullPointerException("ResponseBody is null."))
        }
    })
}













suspend fun getUserCoroutine2() = suspendCancellableCoroutine<User> { continuation ->
    val call = OkHttpClient().newCall(
            Request.Builder()
                    .get().url("https://api.github.com/users/bennyhuo")
                    .build())

    continuation.invokeOnCancellation { // ①
        log("invokeOnCancellation: cancel the request.")
        call.cancel()
    }

    call.enqueue(object : okhttp3.Callback {
        override fun onFailure(call: Call, e: IOException) {
            log("onFailure: $e")
            continuation.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            log("onResponse: ${response.code()}")
            response.body()?.let {
                try {
                    continuation.resume(User.from(it.string()))
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            } ?: continuation.resumeWithException(NullPointerException("ResponseBody is null."))
        }
    })
}


//我们这里用到了 suspendCancellableCoroutine，而不是之前的 suspendCoroutine，
//这就是为了让我们的挂起函数支持协程的取消。该方法将获取到的 Continuation
//包装成了一个 CancellableContinuation，通过调用它的 invokeOnCancellation 方法可以设置一个取消事件的回调，
//一旦这个回调被调用，那么意味着 getUserCoroutine 调用所在的协程被取消了，这时候我们也要相应的做出取消的响应，
//也就是把 OkHttp 发出去的请求给取消掉。

suspend fun main() {
    val job1 = GlobalScope.launch { //①
        log(1)
        val user = getUserCoroutine2()
        log(user)
        log(2)
    }
    delay(10)
    log(3)
    job1.cancel()
    log(4)

    Thread.sleep(1000)
}
//07:31:30:751 [main] 1
//07:31:31:120 [main] 3
//07:31:31:124 [main] invokeOnCancellation: cancel the request.
//07:31:31:129 [main] 4
//07:31:31:131 [OkHttp https://api.github.com/...] onFailure: java.io.IOException: Canceled


//我们发现，取消的回调被调用了，OkHttp 在收到我们的取消指令之后，也确实停止了网络请求，并且回调给我们一个 IO 异常，
//这时候我们的协程已经被取消，在处于取消状态的协程上调用 Continuation.resume 、 Continuation.resumeWithException 或者
//Continuation.resumeWith 都会被忽略，因此 OkHttp 回调中我们收到 IO 异常后调用的 continuation.resumeWithException(e) 不会有任何副作用。





































































