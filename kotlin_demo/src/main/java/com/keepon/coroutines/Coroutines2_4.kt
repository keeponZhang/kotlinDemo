package com.keepon.coroutines

/**
 * createBy	 keepon
 */
//2.4 协程的三层包装
//通过一步步的分析，慢慢发现协程其实有三层包装。常用的launch和async返回的Job、Deferred，里面封装了协程状态，
//提供了取消协程接口，而它们的实例都是继承自AbstractCoroutine，它是协程的第一层包装。
//第二层包装是编译器生成的SuspendLambda的子类，封装了协程的真正运算逻辑，继承自BaseContinuationImpl，
//其中completion属性就是协程的第一层包装。第三层包装是前面分析协程的线程调度时提到的DispatchedContinuation，
//封装了线程调度逻辑，包含了协程的第二层包装。三层包装都实现了Continuation接口，通过代理模式将协程的各层包装组合在一起
//，每层负责不同的功能。
//
//经过以上解析之后，再来看协程就是一段可以挂起和恢复执行的运算逻辑，而协程的挂起是通过挂起函数实现的，
//挂起函数用状态机的方式用挂起点将协程的运算逻辑拆分为不同的片段，每次运行协程执行的不同的逻辑片段。
//所以协程有两个很大的好处：一是简化异步编程，支持异步返回；而是挂起不阻塞线程，提供线程利用率。









































































