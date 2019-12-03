package com.keepon.coroutines

/**
 * createBy	 keepon
 */
/*
协程的线程调度是通过拦截器实现的，前面提到了协程启动调用到了(Cancellable)startCoroutineCancellable，该方法实现为：
internal fun <T> (suspend () -> T).startCoroutineCancellable(completion: Continuation<T>) =
        createCoroutineUnintercepted(completion).intercepted().resumeCancellable(Unit)   //这里调用的是DispatchedContinuation的resumeCancellable方法
// createCoroutineUnintercepted(completion) 会创建一个新的协程，返回值类型为 Continuation
// intercepted() 是给 Continuation 加上 ContinuationInterceptor 拦截器，也是线程调度的关键
// resumeCancellable(Unit) 最终将调用 resume(Unit) 启动协程

再看intercepted()的具体实现：
blic actual fun <T> Continuation<T>.intercepted(): Continuation<T> =
        (this as? ContinuationImpl)?.intercepted() ?: this
// ContinuationImpl 是 SuspendLambda 的父类

internal abstract class ContinuationImpl(...) : BaseContinuationImpl(completion) {
    @Transient
    private var intercepted: Continuation<Any?>? = null

    public fun intercepted(): Continuation<Any?> =
            intercepted
                    ?: (context[ContinuationInterceptor]?.interceptContinuation(this) ?: this)
                            .also { intercepted = it }
    // intercepted() 方法关键是 context[ContinuationInterceptor]?.interceptContinuation(this)
    // context[ContinuationInterceptor] 就是协程的 CoroutineDispatcher
}
//静态代理
public abstract class CoroutineDispatcher :
        AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor {
    */
/**
     * Returns continuation that wraps the original [continuation], thus intercepting all resumptions.
     *//*

    public final override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> =
            DispatchedContinuation(this, continuation)
}

所以intercepted()最终会使用协程的CoroutineDispatcher的interceptContinuation方法包装原来的 Continuation，拦截所有的协程运行操作

DispatchedContinuation拦截了协程的启动和恢复，分别是resumeCancellable(Unit)和重写的resumeWith(Result)：
*/

/*
//continuation是传进来的
internal class DispatchedContinuation<in T>(
        @JvmField val dispatcher: CoroutineDispatcher,
        @JvmField val continuation: Continuation<T>
) : Continuation<T> by continuation, DispatchedTask<T> {
    inline fun resumeCancellable(value: T) {
        // 判断是否需要线程调度
        if (dispatcher.isDispatchNeeded(context)) {
            ...
            // 将协程的运算分发到另一个线程
            dispatcher.dispatch(context, this)
        } else {
            ...
            // 如果不需要调度，直接在当前线程执行协程运算
            resumeUndispatched(value)
        }
    }

    override fun resumeWith(result: Result<T>) {
        // 判断是否需要线程调度
        if (dispatcher.isDispatchNeeded(context)) {
            ...
            // 将协程的运算分发到另一个线程
            dispatcher.dispatch(context, this)
        } else {
            ...
            // 如果不需要调度，直接在当前线程执行协程运算
            continuation.resumeWith(result)
        }
    }
}

internal interface DispatchedTask<in T> : Runnable {
    public override fun run() {
        ...
        // 封装了 continuation.resume 逻辑
    }
}

继续跟踪newSingleThreadContext()、Dispatchers.IO等dispatch方法的实现，发现其实都调用了Executor.execute(Runnable)方法，
而Dispatchers.Unconfined的实现更简单，关键在于isDispatchNeeded()返回为false。*/

















































































