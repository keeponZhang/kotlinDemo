/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines.flow.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.internal.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*
import kotlin.jvm.*

internal fun <T> Flow<T>.asChannelFlow(): ChannelFlow<T> =
    this as? ChannelFlow ?: ChannelFlowOperatorImpl(this)

// Operators that use channels extend this ChannelFlow and are always fused with each other
internal abstract class ChannelFlow<T>(
    // upstream context
    @JvmField val context: CoroutineContext,
    // buffer capacity between upstream and downstream context
    @JvmField val capacity: Int
) : Flow<T> {
    public fun update(
        context: CoroutineContext = EmptyCoroutineContext,
        capacity: Int = Channel.OPTIONAL_CHANNEL
    ): ChannelFlow<T> {
        // note: previous upstream context (specified before) takes precedence
        val newContext = context + this.context
        val newCapacity = when {
            this.capacity == Channel.OPTIONAL_CHANNEL -> capacity
            capacity == Channel.OPTIONAL_CHANNEL -> this.capacity
            this.capacity == Channel.BUFFERED -> capacity
            capacity == Channel.BUFFERED -> this.capacity
            this.capacity == Channel.CONFLATED -> Channel.CONFLATED
            capacity == Channel.CONFLATED -> Channel.CONFLATED
            else -> {
                // sanity checks
                check(this.capacity >= 0) { "Unexpected capacity ${this.capacity}" }
                check(capacity >= 0) { "Unexpected capacity $capacity" }
                // combine capacities clamping to UNLIMITED on overflow
                val sum = this.capacity + capacity
                if (sum >= 0) sum else Channel.UNLIMITED // unlimited on int overflow
            }
        }
        if (newContext == this.context && newCapacity == this.capacity) return this
        return create(newContext, newCapacity)
    }

    protected abstract fun create(context: CoroutineContext, capacity: Int): ChannelFlow<T>

    protected abstract suspend fun collectTo(scope: ProducerScope<T>)

    // shared code to create a suspend lambda from collectTo function in one place
    private val collectToFun: suspend (ProducerScope<T>) -> Unit
        get() = { collectTo(it) }

    private val produceCapacity: Int
        get() = if (capacity == Channel.OPTIONAL_CHANNEL) Channel.BUFFERED else capacity

    fun broadcastImpl(scope: CoroutineScope, start: CoroutineStart): BroadcastChannel<T> =
        scope.broadcast(context, produceCapacity, start, block = collectToFun)

    fun produceImpl(scope: CoroutineScope): ReceiveChannel<T> =
        scope.flowProduce(context, produceCapacity, block = collectToFun)

    override suspend fun collect(collector: FlowCollector<T>) =
        coroutineScope {
            val channel = produceImpl(this)
            channel.consumeEach { collector.emit(it) }
        }

    // debug toString
    override fun toString(): String =
        "$classSimpleName[${additionalToStringProps()}context=$context, capacity=$capacity]"

    open fun additionalToStringProps() = ""
}

// ChannelFlow implementation that operates on another flow before it
internal abstract class ChannelFlowOperator<S, T>(
    @JvmField val flow: Flow<S>,
    context: CoroutineContext,
    capacity: Int
) : ChannelFlow<T>(context, capacity) {
    protected abstract suspend fun flowCollect(collector: FlowCollector<T>)

    // Changes collecting context upstream to the specified newContext, while collecting in the original context
    private suspend fun collectWithContextUndispatched(collector: FlowCollector<T>, newContext: CoroutineContext) {
        val originalContextCollector = collector.withUndispatchedContextCollector(coroutineContext)
        // invoke flowCollect(originalContextCollector) in the newContext
        return withContextUndispatched(newContext, block = { flowCollect(it) }, value = originalContextCollector)
    }

    // Slow path when output channel is required
    protected override suspend fun collectTo(scope: ProducerScope<T>) =
        flowCollect(SendingCollector(scope))

    // Optimizations for fast-path when channel creation is optional
    override suspend fun collect(collector: FlowCollector<T>) {
        // Fast-path: When channel creation is optional (flowOn/flowWith operators without buffer)
        if (capacity == Channel.OPTIONAL_CHANNEL) {
            val collectContext = coroutineContext
            val newContext = collectContext + context // compute resulting collect context
            // #1: If the resulting context happens to be the same as it was -- fallback to plain collect
            if (newContext == collectContext)
                return flowCollect(collector)
            // #2: If we don't need to change the dispatcher we can go without channels
            if (newContext[ContinuationInterceptor] == collectContext[ContinuationInterceptor])
                return collectWithContextUndispatched(collector, newContext)
        }
        // Slow-path: create the actual channel
        super.collect(collector)
    }

    // debug toString
    override fun toString(): String = "$flow -> ${super.toString()}"
}

// Simple channel flow operator: flowOn, buffer, or their fused combination
internal class ChannelFlowOperatorImpl<T>(
    flow: Flow<T>,
    context: CoroutineContext = EmptyCoroutineContext,
    capacity: Int = Channel.OPTIONAL_CHANNEL
) : ChannelFlowOperator<T, T>(flow, context, capacity) {
    override fun create(context: CoroutineContext, capacity: Int): ChannelFlow<T> =
        ChannelFlowOperatorImpl(flow, context, capacity)

    override suspend fun flowCollect(collector: FlowCollector<T>) =
        flow.collect(collector)
}

// Now if the underlying collector was accepting concurrent emits, then this one is too
// todo: we might need to generalize this pattern for "thread-safe" operators that can fuse with channels
private fun <T> FlowCollector<T>.withUndispatchedContextCollector(emitContext: CoroutineContext): FlowCollector<T> = when (this) {
    // SendingCollector does not care about the context at all so can be used as it
    is SendingCollector -> this
    // Original collector is concurrent, so wrap into ConcurrentUndispatchedContextCollector (also concurrent)
    is ConcurrentFlowCollector -> ConcurrentUndispatchedContextCollector(this, emitContext)
    // Otherwise just wrap into UndispatchedContextCollector interface implementation
    else -> UndispatchedContextCollector(this, emitContext)
}

private open class UndispatchedContextCollector<T>(
    downstream: FlowCollector<T>,
    private val emitContext: CoroutineContext
) : FlowCollector<T> {
    private val countOrElement = threadContextElements(emitContext) // precompute for fast withContextUndispatched
    private val emitRef: suspend (T) -> Unit = { downstream.emit(it) } // allocate suspend function ref once on creation

    override suspend fun emit(value: T): Unit =
        withContextUndispatched(emitContext, countOrElement, emitRef, value)
}

// named class for a combination of UndispatchedContextCollector & ConcurrentFlowCollector interface
private class ConcurrentUndispatchedContextCollector<T>(
    downstream: ConcurrentFlowCollector<T>,
    emitContext: CoroutineContext
) : UndispatchedContextCollector<T>(downstream, emitContext), ConcurrentFlowCollector<T>

// Efficiently computes block(value) in the newContext
private suspend fun <T, V> withContextUndispatched(
    newContext: CoroutineContext,
    countOrElement: Any = threadContextElements(newContext), // can be precomputed for speed
    block: suspend (V) -> T, value: V
): T =
    suspendCoroutineUninterceptedOrReturn sc@{ uCont ->
        withCoroutineContext(newContext, countOrElement) {
            block.startCoroutineUninterceptedOrReturn(value, Continuation(newContext) {
                uCont.resumeWith(it)
            })
        }
    }
