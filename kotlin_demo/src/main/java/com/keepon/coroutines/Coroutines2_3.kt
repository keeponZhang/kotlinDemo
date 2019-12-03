package com.keepon.coroutines

import kotlinx.coroutines.*

/**
 * createBy	 keepon
 */
//Kotlin 编译器会生成继承自SuspendLambda的子类，协程的真正运算逻辑都在invokeSuspend中。
//但是协程挂起的具体实现是如何呢？先看下面示例代码：

fun main(args: Array<String>) = runBlocking<Unit> { // 新建并启动 blocking 协程，运行在 main 线程上，等待所有子协程运行完成后才会结束
    launch() { // 新建并启动 launch 协程，没有指定所运行线程，一开始运行在调用者所在的 main 线程上
        println("${Thread.currentThread().name} : launch start")
        async(Dispatchers.Default) { // 新建并启动 async 协程，运行在 Dispatchers.Default 的线程池中
            println("${Thread.currentThread().name} : async start")
            delay(100)  // 挂起 async 协程 100 ms
            println("${Thread.currentThread().name} : async end")
        }.await() // 挂起 launch 协程，直到 async 协程结束
        println("${Thread.currentThread().name} : launch end")
    }
}


//其中 launch 协程编译生成的 SuspendLambda 子类的invokeSuspend方法如下：

/*
public final Object invokeSuspend(@NotNull Object result) {
    Object coroutine_suspended = IntrinsicsKt.getCOROUTINE_SUSPENDED();
    switch (this.label) {
        case 0:
        ...
        System.out.println(stringBuilder.append(currentThread.getName()).append(" : launch start").toString());
        // 新建并启动 async 协程
        Deferred async$default = BuildersKt.async$default(coroutineScope, (CoroutineContext) Dispatchers.getDefault(), null, (Function2) new 1(null), 2, null);
        this.label = 1;
        // 调用 await() 挂起函数
        if (async$default.await(this) == coroutine_suspended) {
        return coroutine_suspended;
    }
        break;
        case 1:
        if (result instanceof Failure) {
            throw ((Failure) result).exception;
        }
        // 恢复协程后再执行一次 resumeWith()，然后无异常的话执行最后的 println()
        break;
        default:
        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
    }
    ...
    System.out.println(stringBuilder2.append(currentThread2.getName()).append(" : launch end").toString());
    return Unit.INSTANCE;
}
*/

/*
上面代码中 launch 协程挂起的关键在于async$default.await(this) == coroutine_suspended，
如果此时 async 线程未执行完成，await()返回为IntrinsicsKt.getCOROUTINE_SUSPENDED()，
就会 return，launch 协程的invokeSuspend方法执行完成，协程所在线程继续往下运行，
此时 launch 线程处于挂起状态。所以协程挂起就是协程挂起点之前逻辑执行完成，
协程的运算关键方法resumeWith()执行完成，线程继续执行往下执行其他逻辑。

协程挂起有三点需要注意的：

1.启动其他协程并不会挂起当前协程，所以launch和async启动线程时，除非新协程运行在当前线程，则当前协程只能在新协程运行完成后继续执行，否则当前协程都会马上继续运行。

2.协程挂起并不会阻塞线程，因为协程挂起时相当于执行完协程的方法，线程继续执行其他之后的逻辑。

3.挂起函数并一定都会挂起协程，例如await()挂起函数如果返回值不等于IntrinsicsKt.getCOROUTINE_SUSPENDED()，则协程继续执行挂起点之后逻辑。
*/


//下面继续分析await()的实现原理，它的实现中关键是调用了JobSupport.awaitSuspend()方法：
/*private suspend fun awaitSuspend(): Any? = suspendCoroutineUninterceptedOrReturn { uCont ->
    *//*
        * Custom code here, so that parent coroutine that is using await
        * on its child deferred (async) coroutine would throw the exception that this child had
        * thrown and not a JobCancellationException.
        *//*
    val cont = AwaitContinuation(uCont.intercepted(), this)
    cont.initCancellability()
    invokeOnCompletion(ResumeAwaitOnCompletion(this, cont).asHandler)
    cont.getResult()
}

private class ResumeAwaitOnCompletion<T>(
        job: JobSupport,
        private val continuation: AbstractContinuation<T>
) : JobNode<JobSupport>(job) {
    override fun invoke(cause: Throwable?) {
        val state = job.state
        check(state !is Incomplete)
        if (state is CompletedExceptionally) {
            // Resume with exception in atomic way to preserve exception
            continuation.resumeWithExceptionMode(state.cause, MODE_ATOMIC_DEFAULT)
        } else {
            // Resuming with value in a cancellable way (AwaitContinuation is configured for this mode).
            @Suppress("UNCHECKED_CAST")
            //这里恢复协程
            continuation.resume(state as T)
        }
    }
    override fun toString() = "ResumeAwaitOnCompletion[$continuation]"
}*/

//上面源码中ResumeAwaitOnCompletion的invoke方法的逻辑就是调用continuation.resume(state as T)恢复协程。
//invokeOnCompletion(JobSupport)函数里面是如何实现 async 协程完成后自动恢复之前协程的呢，源码实现有些复杂，
//因为很多边界情况处理就不全部展开，其中最关键的逻辑如下：
//// handler 就是 ResumeAwaitOnCompletion 的实例，将 handler 作为节点
//val node = nodeCache ?: makeNode(handler, onCancelling).also { nodeCache = it }
//// 将 node 节点添加到 state.list 中
//if (!addLastAtomic(state, list, node)) return@loopOnState // retry

//接下来我断点调试 launch 协程恢复的过程，从 async 协程的SuspendLambda的子类的completion(Continuation).resumeWith(outcome)
//-> AbstractCoroutine.resumeWith(result) ..-> JobSupport.tryFinalizeSimpleState() -> JobSupport.completeStateFinalization() -> state.list?.notifyCompletion(cause) ->
//node.invoke，最后 handler 节点里面通过调用resume(result)恢复协程。

//所以await()挂起函数恢复协程的原理是，将 launch 协程封装为 ResumeAwaitOnCompletion 作为 handler 节点添加到 aynsc 协程的 state.list，
//然后在 async 协程完成时会通知 handler 节点调用 launch 协程的 resume(result) 方法将结果传给 launch 协程，
//并恢复 launch 协程继续执行 await 挂起点之后的逻辑。

//而这过程中有两个final的resumeWith 方法，一个是SuspendLambda的父类BaseContinuationImpl的，我们再来详细分析一篇:
/*internal abstract class BaseContinuationImpl(
        public val completion: Continuation<Any?>?
) : Continuation<Any?>, CoroutineStackFrame, Serializable {
    public final override fun resumeWith(result: Result<Any?>) {
        ...
        var param = result
        while (true) {
            with(current) {
                val completion = completion!!
                val outcome: Result<Any?> =
                        try {
                            // 调用 invokeSuspend 方法执行，执行协程的真正运算逻辑
                            val outcome = invokeSuspend(param)
                            // 协程挂起时 invokeSuspend 才会返回 COROUTINE_SUSPENDED，所以协程挂起时，其实只是协程的 resumeWith 运行逻辑执行完成，再次调用 resumeWith 时，协程挂起点之后的逻辑才能继续执行
                            if (outcome === COROUTINE_SUSPENDED) return
                            Result.success(outcome)
                        } catch (exception: Throwable) {
                            Result.failure(exception)
                        }
                releaseIntercepted() // this state machine instance is terminating
                // 这里可以看出 Continuation 其实分为两类，一种是 BaseContinuationImpl，封装了协程的真正运算逻辑
                if (completion is BaseContinuationImpl) {
                    // unrolling recursion via loop
                    current = completion
                    param = outcome
                } else {
                    // 断点时发现 completion 是 DeferredCoroutine 实例，这里实际调用的是其父类 AbstractCoroutine 的 resumeWith 方法
                    completion.resumeWith(outcome)
                    return
                }
            }
        }
    }
}

接下来再来看另外一类 Continuation，AbstractCoroutine 的resumeWith实现：

public abstract class AbstractCoroutine<in T>(
        @JvmField
        protected val parentContext: CoroutineContext,
        active: Boolean = true
) : JobSupport(active), Job, Continuation<T>, CoroutineScope {
    *//**
     * Completes execution of this with coroutine with the specified result.
     *//*
    public final override fun resumeWith(result: Result<T>) {
        // makeCompletingOnce 大致实现是修改协程状态，如果需要的话还会将结果返回给调用者协程，并恢复调用者协程
        makeCompletingOnce(result.toState(), defaultResumeMode)
    }
}*/


//所以其中一类 Continuation BaseContinuationImpl的resumeWith封装了协程的运算逻辑，用以协程的启动和恢复；
//而另一类 Continuation AbstractCoroutine，主要是负责维护协程的状态和管理，它的resumeWith则是完成协程，恢复调用者协程。



























































































