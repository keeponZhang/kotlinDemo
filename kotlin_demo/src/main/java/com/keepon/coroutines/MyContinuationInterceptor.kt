package com.keepon.coroutines

import com.keepon.coroutines.utils.log
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor

/**
 * createBy	 keepon
 */
class MyContinuationInterceptor: ContinuationInterceptor {
    override val key = ContinuationInterceptor
    override fun <T> interceptContinuation(continuation: Continuation<T>) = MyContinuation(continuation)
}

class MyContinuation<T>(val continuation: Continuation<T>): Continuation<T> {
    override val context = continuation.context
    override fun resumeWith(result: Result<T>) {
        log("<MyContinuation> $result" )
        continuation.resumeWith(result)
    }
}

