package com.keepon.zhongwen.协程上下文与调度器

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

/**
 * createBy	 keepon
 */
//调度器与线程
//协程上下文包含一个 协程调度器 （参见 CoroutineDispatcher）它确定了哪些线程或与线程相对应的协程执行。协程调度器可以将协程限制在一个特定的线程执行，或将它分派到一个线程池，亦或是让它不受限地运行。
//
//所有的协程构建器诸如 launch 和 async 接收一个可选的 CoroutineContext 参数，它可以被用来显式的为一个新协程或其它上下文元素指定一个调度器。

fun main() = runBlocking<Unit> {
    launch {
        // 运行在父协程的上下文中，即 runBlocking 主协程
        println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Unconfined) {
        // 不受限的——将工作在主线程中
        println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Default) {
        // 将会获取默认调度器
        println("Default               : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(newSingleThreadContext("MyOwnThread")) {
        // 将使它获得一个新的线程
        println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
    }

//    它执行后得到了如下输出（也许顺序会有所不同）：
//    Unconfined            : I'm working in thread main
//    Default               : I'm working in thread DefaultDispatcher-worker-1
//    newSingleThreadContext: I'm working in thread MyOwnThread
//    main runBlocking      : I'm working in thread main

//    当调用 launch { …… } 时不传参数，它从启动了它的 CoroutineScope 中承袭了上下文（以及调度器）。
//    在这个案例中，它从 main 线程中的 runBlocking 主协程承袭了上下文。
//
//    Dispatchers.Unconfined 是一个特殊的调度器且似乎也运行在 main 线程中，但实际上， 它是一种不同的机制，这会在后文中讲到。
//
//    该默认调度器，当协程在 GlobalScope 中启动的时候使用， 它代表 Dispatchers.Default 使用了共享的后台线程池， 所以 GlobalScope.launch { …… }
//    也可以使用相同的调度器—— launch(Dispatchers.Default) { …… }。
//
//    newSingleThreadContext 为协程的运行启动了一个线程。 一个专用的线程是一种非常昂贵的资源。 在真实的应用程序中两者都必须被释放，当不再需要的时候，使用 close 函数，或存储在一个顶层变量中使它在整个应用程序中被重用。
}



















































