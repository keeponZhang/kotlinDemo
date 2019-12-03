package com.keepon.coroutines

import com.keepon.coroutines.utils.log
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * createBy	 keepon
 */

suspend fun main() {
//    test1()
//    test12()

//    test13()
    test14()


}



private suspend fun test12() {
    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        log("Throws an exception with message: ${throwable.message}")
    }

    log(1)
    GlobalScope.launch(exceptionHandler) {
        throw ArithmeticException("Hey!")
    }.join()
    log(2)
}

//CoroutineExceptionHandler 竟然也是一个上下文，协程的这个上下文可真是灵魂一般的存在，这倒是一点儿也不让人感到意外。
//
//当然，这并不算是一个全局的异常捕获，因为它只能捕获对应协程内未捕获的异常，如果你想做到真正的全局捕获，
//在 Jvm 上我们可以自己定义一个捕获类实现：

//然后在 classpath 中创建 META-INF/services/kotlinx.coroutines.CoroutineExceptionHandler，文件名实际上就是
//CoroutineExceptionHandler 的全类名，文件内容就写我们的实现类的全类名：com.bennyhuo.coroutines.sample2.exceptions.GlobalCoroutineExceptionHandler
//
//这样协程中没有被捕获的异常就会最终交给它处理。
//Jvm 上全局 CoroutineExceptionHandler 的配置，本质上是对 ServiceLoader 的应用，之前我们在讲 Dispatchers.Main 的时候提到过，
//Jvm 上它的实现也是通过 ServiceLoader 来加载的。
//需要明确的一点是，通过 async 启动的协程出现未捕获的异常时会忽略 CoroutineExceptionHandler，这与 launch 的设计思路是不同的。







class GlobalCoroutineExceptionHandler: CoroutineExceptionHandler {
    override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        println("Coroutine exception: $exception")
    }
}



private suspend fun test1() {
    GlobalScope.launch(MyContinuationInterceptor()) {
        log(1)
        val job = async {
            log(2)
            delay(1000)
            log(3)
            "Hello"
        }
        log(4)
        val result = job.await()
        log("5. $result")
    }.join()
    log(6)
}

//与main加suspend关键字无关
/*10:27:03:681 [main] <MyContinuation> Success(kotlin.Unit)
10:27:03:682 [main] 1
10:27:03:686 [main] <MyContinuation> Success(kotlin.Unit)
10:27:03:686 [main] 2
10:27:03:699 [main] 4
10:27:04:703 [kotlinx.coroutines.DefaultExecutor] <MyContinuation> Success(kotlin.Unit)
10:27:04:703 [kotlinx.coroutines.DefaultExecutor] 3
10:27:04:708 [kotlinx.coroutines.DefaultExecutor] <MyContinuation> Success(Hello)
10:27:04:708 [kotlinx.coroutines.DefaultExecutor] 5. Hello
10:27:04:709 [kotlinx.coroutines.DefaultExecutor] 6*/

//大家可能就要奇怪了，你不是说 Continuation 是回调么，这里面回调调用也就一次啊（await 那里），怎么日志打印了四次呢？
//
//别慌，我们按顺序给大家介绍。
//
//首先，所有协程启动的时候，都会有一次 Continuation.resumeWith 的操作，这一次操作对于调度器来说就是一次调度的机会，我们的协程有机会调度到其他线程的关键之处就在于此。 ①、② 两处都是这种情况。
//
//其次，delay 是挂起点，1000ms 之后需要继续调度执行该协程，因此就有了 ③ 处的日志。
//
//最后，④ 处的日志就很容易理解了，正是我们的返回结果。
//
//
//可能有朋友还会有疑问，我并没有在拦截器当中切换线程，为什么从 ③ 处开始有了线程切换的操作？这个切换线程的逻辑源自于
//delay，在 JVM 上 delay 实际上是在一个 ScheduledExcecutor 里面添加了一个延时任务，因此会发生线程切换；而在 JavaScript
//环境中则是基于 setTimeout，如果运行在 Nodejs 上，delay 就不会切线程了，毕竟人家是单线程的













//异常传播还涉及到协程作用域的概念，例如我们启动协程的时候一直都是用的 GlobalScope，意味着这是一个独立的顶级协程作用域，
//此外还有 coroutineScope { ... } 以及 supervisorScope { ... }。

//1.通过 GlobeScope 启动的协程单独启动一个协程作用域，内部的子协程遵从默认的作用域规则。通过 GlobeScope 启动的协程“自成一派”。
//2.coroutineScope 是继承外部 Job 的上下文创建作用域，在其内部的取消操作是双向传播的，子协程未捕获的异常也会向上传递给父协程。
//  它更适合一系列对等的协程并发的完成一项工作，任何一个子协程异常退出，那么整体都将退出，简单来说就是”一损俱损“。
//  这也是协程内部再启动子协程的默认作用域。
//3.supervisorScope 同样继承外部作用域的上下文，但其内部的取消操作是单向传播的，父协程向子协程传播，
//  反过来则不然，这意味着子协程出了异常并不会影响父协程以及其他兄弟协程。它更适合一些独立不相干的任务，任何一个任务出问题，
//  并不会影响其他任务的工作，简单来说就是”自作自受“，例如 UI，我点击一个按钮出了异常，其实并不会影响手机状态栏的刷新。
//  需要注意的是，supervisorScope 内部启动的子协程内部再启动子协程，如无明确指出，则遵守默认作用域规则，
//  也即 supervisorScope 只作用域其直接子协程。



suspend fun test13() {
    log(1)
    try {
        coroutineScope { //①
            log(2)
            launch { // ②
                log(3)
                launch { // ③

                    log(4)
                    delay(100)
                    throw ArithmeticException("Hey!!")
                }
                log(5)
            }
            log(6)
            val job = launch { // ④
                try {
                    log(7)
                    delay(1000)

                }catch (e: Exception ){
                    log("@4 exception. $e")
                }
                 //注释掉这里不会有10,如果把前面③里面的delay也注释掉，会调用，所以还是结论那个原因
            }
            try {
                log(8)
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

//这例子稍微有点儿复杂，但也不难理解，我们在一个 coroutineScope 当中启动了两个协程 ②④，
//在 ② 当中启动了一个子协程 ③，作用域直接创建的协程记为①。
//那么 ③ 当中抛异常会发生什么呢？我们先来看下输出：

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

//注意两个位置，一个是 10，我们调用 join，收到了一个取消异常，在协程当中支持取消的操作的suspend方法在取消时会抛出一个
//CancellationException，这类似于线程中对 InterruptException 的响应，遇到这种情况表示 join 调用所在的协程已经被取消了，
//那么这个取消究竟是怎么回事呢？
//
//原来协程 ③ 抛出了未捕获的异常，进入了异常完成的状态，它与父协程 ② 之间遵循默认的作用域规则，
//因此 ③ 会通知它的父协程也就是 ② 取消，② 根据作用域规则通知父协程 ① 也就是整个作用域取消，这是一个自下而上的一次传播，
//这样身处 ① 当中的 job.join 调用就会抛异常，也就是 10 处的结果了。
//如果不是很理解这个操作，想一下我们说到的，coroutineScope 内部启动的协程就是“一损俱损”。
//实际上由于父协程 ① 被取消，协程④ 也不能幸免，如果大家有兴趣的话，也可以对 ④ 当中的 delay进行捕获，一样会收获一枚取消异常。

suspend fun test14() {
    log(1)
    try {
        supervisorScope()
             { //①
            log(2)
            launch { // ②
                log(3)
                launch { // ③
                    log(4)
                    delay(100)
                    throw ArithmeticException("Hey!!")
                }
                log(5)
            }
            log(6)
            val job = launch { // ④
                try {
                    log(7)
                    delay(1000)

                }catch (e: Exception ){
                    log("@4 exception. $e")
                }
                //注释掉这里不会有10,如果把前面③里面的delay也注释掉，会调用，所以还是结论那个原因
            }
            try {
                log(8)
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


/*1:52:48:632 [main] 1
11:52:48:694 [main] 2
11:52:48:875 [main] 6
11:52:48:892 [DefaultDispatcher-worker-1 @coroutine#1] 3
11:52:48:895 [DefaultDispatcher-worker-1 @coroutine#1] 5
11:52:48:900 [DefaultDispatcher-worker-3 @coroutine#3] 4
11:52:48:905 [DefaultDispatcher-worker-2 @coroutine#2] 7
11:52:48:907 [main] 8
Exception in thread "DefaultDispatcher-worker-3 @coroutine#3" java.lang.ArithmeticException: Hey!!
at com.bennyhuo.coroutines.sample2.exceptions.ScopesKt$main$2$1$1.invokeSuspend(Scopes.kt:17)
at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
at kotlinx.coroutines.DispatchedTask.run(Dispatched.kt:238)
at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:594)
at kotlinx.coroutines.scheduling.CoroutineScheduler.access$runSafely(CoroutineScheduler.kt:60)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:742)
11:52:49:915 [DefaultDispatcher-worker-3 @coroutine#2] 9
11:52:49:915 [DefaultDispatcher-worker-3 @coroutine#2] 11
11:52:49:915 [DefaultDispatcher-worker-3 @coroutine#2] 13*/

//我们可以看到，1-8 的输出其实没有本质区别，顺序上的差异是线程调度的前后造成的，并不会影响协程的语义。
//差别主要在于 9 与 10、11与12的区别，如果把 scope 换成 supervisorScope，我们发现 ③
//的异常并没有影响作用域以及作用域内的其他子协程的执行，也就是我们所说的“自作自受”。























