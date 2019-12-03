package com.keepon.coroutines

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * createBy	 keepon
 */


fun main() {
    GlobalScope.launch() {
        println("Unconfined      : I'm working in thread ${Thread.currentThread().name}")
        kotlinx.coroutines.delay(100)
        println("Unconfined      : After delay in thread ${Thread.currentThread().name}")
    }
}

//2. 协程之间的关系

//官方文档中有提到协程之间可能存在父子关系，取消父协程时，也会取消所有子协程。在Job的源码中有这样一段话描述协程间父子关系：
//
//* A parent-child relation has the following effect:
//*
//* * Cancellation of parent with [cancel] or its exceptional completion (failure)
//*   immediately cancels all its children.
//* * Parent cannot complete until all its children are complete. Parent waits for all its children to
//*   complete in _completing_ or _cancelling_ state.
//* * Uncaught exception in a child, by default, cancels parent. In particular, this applies to
//*   children created with [launch][CoroutineScope.launch] coroutine builder. Note, that
//*   [async][CoroutineScope.async] and other future-like
//*   coroutine builders do not have uncaught exceptions by definition, since all their exceptions are
//*   caught and are encapsulated in their result.


//所以协程间父子关系有三种影响：
//
//父协程手动调用cancel()或者异常结束，会立即取消它的所有子协程。
//
//父协程必须等待所有子协程完成（处于完成或者取消状态）才能完成。
//
//子协程抛出未捕获的异常时，默认情况下会取消其父协程。
//
//下面先来看看协程是如何建立父子关系的，launch和async新建协程时，首先都是newCoroutineContext(context)新建协程的
//CoroutineContext 上下文，下面看其具体细节：
//
//public actual fun CoroutineScope.newCoroutineContext(context: CoroutineContext): CoroutineContext {
//    // 新协程继承了原来 CoroutineScope 的 coroutineContext
//    val combined = coroutineContext + context
//    val debug = if (DEBUG) combined + CoroutineId(COROUTINE_ID.incrementAndGet()) else combined
//    // 当新协程没有指定线程调度器时，会默认使用 Dispatchers.Default
//    return if (combined !== Dispatchers.Default && combined[ContinuationInterceptor] == null)
//        debug + Dispatchers.Default else debug
//}
//所以新的协程的 CoroutineContext 都继承了原来 CoroutineScope 的 coroutineContext，然后launch和async新建协程最后都会调用
//start(start: CoroutineStart, receiver: R, block: suspend R.() -> T)，里面第一行是initParentJob()，
//通过注释可以知道就是这个函数建立父子关系的，下面看其实现细节：
//
//// AbstractCoroutine.kt
//internal fun initParentJob() {
//    initParentJobInternal(parentContext[Job])
//}
//
//// JobSupport.kt
//internal fun initParentJobInternal(parent: Job?) {
//    check(parentHandle == null)
//    if (parent == null) {
//        parentHandle = NonDisposableHandle
//        return
//    }
//    parent.start() // make sure the parent is started
//    @Suppress("DEPRECATION")
//    // 关键在于 parent.attachChild(this)
//    val handle = parent.attachChild(this)
//    parentHandle = handle
//    // now check our state _after_ registering (see tryFinalizeSimpleState order of actions)
//    if (isCompleted) {
//        handle.dispose()
//        parentHandle = NonDisposableHandle // release it just in case, to aid GC
//    }
//}

//这里需要注意的是GlobalScope和普通协程的CoroutineScope的区别，GlobalScope的 Job 是为空的，GlobalScope.launch{}
//和GlobalScope.async{}新建的协程是没有父协程的。
//
//下面继续看attachChild的实现：
//public final override fun attachChild(child: ChildJob): ChildHandle {
//    return invokeOnCompletion(onCancelling = true, handler = ChildHandleNode(this, child).asHandler) as ChildHandle
//}
//invokeOnCompletion()函数在前一篇解析 Deferred.await() 中有提到，关键是将 handler 节点添加到父协程 state.list 的末尾。

//2.1 父协程（Job）手动调用cancel()或者异常结束，会立即取消它的所有子协程
//跟踪父协程的cancel()调用过程，其中关键过程为 cancel() -> cancel(null) -> cancelImpl(null) -> makeCancelling(null) ->
//tryMakeCancelling(state, causeException) -> notifyCancelling(list, rootCause)，下面继续分析notifyCancelling(list, rootCause)的实现
//：
// JobSupport.kt
//private fun notifyCancelling(list: NodeList, cause: Throwable) {
//    // first cancel our own children
//    onCancellation(cause)
//    // 这里会调用所有子协程绑定的 ChildHandleNode.invoke(cause) -> childJob.parentCancelled(parentJob) 来取消所有子协程
//    notifyHandlers<JobCancellingNode<*>>(list, cause)
//    // then cancel parent
//    // cancelParent(cause) 不一定会取消父协程，cancel() 时不会取消父协程，因为此时产生 cause 的是 JobCancellationException，属于 CancellationException
//    cancelParent(cause) // tentative cancellation -- does not matter if there is no parent
//}
//
//public final override fun parentCancelled(parentJob: ParentJob) {
//    // 父协程取消时，子协程会通过 parentCancelled 来取消自己
//    cancelImpl(parentJob)
//}
//
//private fun cancelParent(cause: Throwable): Boolean {
//    // CancellationException is considered "normal" and parent is not cancelled when child produces it.
//    // This allow parent to cancel its children (normally) without being cancelled itself, unless
//    // child crashes and produce some other exception during its completion.
//    if (cause is CancellationException) return true
//    if (!cancelsParent) return false
//    // 当 cancelsParent 为 true, 且子线程抛出未捕获的异常时，默认情况下 childCancelled() 会取消其父协程。
//    return parentHandle?.childCancelled(cause) == true
//}




/*
2.2 父协程必须等待所有子协程完成（处于完成或者取消状态）才能完成
前一篇文章有提到协程的完成通过AbstractCoroutine.resumeWith(result)实现，调用过程为 makeCompletingOnce(result.toState(), defaultResumeMode) ->
tryMakeCompleting()，其中关键源码如下

// JobSupport.kt
private fun tryMakeCompleting(state: Any?, proposedUpdate: Any?, mode: Int): Int {
    ...
    // now wait for children
    val child = firstChild(state)
    // 等待子协程完成
    if (child != null && tryWaitForChild(finishing, child, proposedUpdate))
        return COMPLETING_WAITING_CHILDREN
    // otherwise -- we have not children left (all were already cancelled?)
    if (tryFinalizeFinishingState(finishing, proposedUpdate, mode))
        return COMPLETING_COMPLETED
    // otherwise retry
    return COMPLETING_RETRY
}

private tailrec fun tryWaitForChild(state: Finishing, child: ChildHandleNode, proposedUpdate: Any?): Boolean {
    // 添加 ChildCompletion 节点到子协程的 state.list 末尾，当子协程完成时会调用 ChildCompletion.invoke()
    val handle = child.childJob.invokeOnCompletion(
            invokeImmediately = false,
            handler = ChildCompletion(this, state, child, proposedUpdate).asHandler
    )
    if (handle !== NonDisposableHandle) return true // child is not complete and we've started waiting for it
    // 循环设置所有其他子协程
    val nextChild = child.nextChild() ?: return false
    return tryWaitForChild(state, nextChild, proposedUpdate)
}

tryWaitForChild()也是通过invokeOnCompletion()添加节点到子协程的 state.list 中，当子协程完成时会调用 ChildCompletion.invoke(）：
// ChildCompletion class
override fun invoke(cause: Throwable?) {
    parent.continueCompleting(state, child, proposedUpdate)
}

private fun continueCompleting(state: Finishing, lastChild: ChildHandleNode, proposedUpdate: Any?) {
    require(this.state === state) // consistency check -- it cannot change while we are waiting for children
    // figure out if we need to wait for next child
    val waitChild = lastChild.nextChild()
    // try wait for next child
    if (waitChild != null && tryWaitForChild(state, waitChild, proposedUpdate)) return // waiting for next child
    // no more children to wait -- try update state
    // 当所有子协程都完成时，才会 tryFinalizeFinishingState() 完成自己
    if (tryFinalizeFinishingState(state, proposedUpdate, MODE_ATOMIC_DEFAULT)) return
}
*/


//2.3 子协程抛出未捕获的异常时，默认情况下会取消其父协程。
//子线程抛出未捕获的异常时，后续的处理会如何呢？在前一篇解析中协程的运算在第二层包装 BaseContinuationImpl 中，我们再看一次：
//internal abstract class BaseContinuationImpl(
//        public val completion: Continuation<Any?>?
//) : Continuation<Any?>, CoroutineStackFrame, Serializable {
//    public final override fun resumeWith(result: Result<Any?>) {
//        ...
//        var param = result
//        while (true) {
//            with(current) {
//                val completion = completion!!
//                val outcome: Result<Any?> =
//                        try {
//                            // 调用 invokeSuspend 方法执行，执行协程的真正运算逻辑
//                            val outcome = invokeSuspend(param)
//                            if (outcome === COROUTINE_SUSPENDED) return
//                            Result.success(outcome)
//                        } catch (exception: Throwable) {
//                            // 协程抛出未捕获的异常，会在这里被拦截，然后作为结果完成协程
//                            Result.failure(exception)
//                        }
//                releaseIntercepted() // this state machine instance is terminating
//                if (completion is BaseContinuationImpl) {
//                    // unrolling recursion via loop
//                    current = completion
//                    param = outcome
//                } else {
//                    // 协程的状态修改在 AbstractCoroutine.resumeWith() 中
//                    completion.resumeWith(outcome)
//                    return
//                }
//            }
//        }
//    }
//}

//所以协程有未捕获的异常中，会在第二层包装中的resumeWith()捕获到，然后调用第一层包装 AbstractCoroutine.resumeWith() 来取消当前协程，
//处理过程为 AbstractCoroutine.resumeWith(Result.failure(exception)) -> JobSupport.makeCompletingOnce(CompletedExceptionally(exception),
//defaultResumeMode) -> tryMakeCompleting(state, CompletedExceptionally(exception), defaultResumeMode) -> notifyCancelling(list, exception) ->
//cancelParent(exception)，所以出现未捕获的异常时，和手动调用cancel()一样会调用到 notifyCancelling(list, exception) 来取消当前协程，
//和手动调用cancel()的区别在于 exception 不是 CancellationException。

//private fun cancelParent(cause: Throwable): Boolean {
//    // CancellationException is considered "normal" and parent is not cancelled when child produces it.
//    // This allow parent to cancel its children (normally) without being cancelled itself, unless
//    // child crashes and produce some other exception during its completion.
//    if (cause is CancellationException) return true
//    if (!cancelsParent) return false
//    // launch 和 async 新建的协程的 cancelsParent 都为 true， 所以子线程抛出未捕获的异常时，默认情况下 childCancelled() 会取消其父协程。
//    return parentHandle?.childCancelled(cause) == true
//}
//
//// 默认情况下 childCancelled() 会取消取消协程
//public open fun childCancelled(cause: Throwable): Boolean =
//        cancelImpl(cause) && handlesException































