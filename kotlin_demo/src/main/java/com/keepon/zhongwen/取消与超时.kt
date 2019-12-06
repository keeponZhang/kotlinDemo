package com.keepon.zhongwen.取消与超时

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

/**
 * createBy	 keepon
 */
fun main() = runBlocking {
    //    取消协程的执行()

//    取消是协作的()

//    使计算代码可取消()

//    在finally中释放资源()

//    运行不能取消的代码块()

//    超时()
    超时withTimeoutOrNull()
}


//由于取消只是一个例外，所有的资源都使用常用的方法来关闭。 如果你需要做一些各类使用超时的特别的额外操作，
//可以使用类似 withTimeout 的 withTimeoutOrNull 函数，并把这些会超时的代码包装在 try {...} catch (e: TimeoutCancellationException) {...}
//代码块中，而 withTimeoutOrNull 通过返回 null 来进行超时操作，从而替代抛出一个异常
suspend fun 超时withTimeoutOrNull() {
    val result = withTimeoutOrNull(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
        "Done" // 在它运行得到结果之前取消它
    }
    println("Result is $result")
}

suspend fun 超时() {
    withTimeout(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
    //运行后得到如下输出：
//    I'm sleeping 0 ...
//    I'm sleeping 1 ...
//    I'm sleeping 2 ...
//    Exception in thread "main" kotlinx.coroutines.TimeoutCancellationException: Timed out waiting for 1300 ms

//    withTimeout 抛出了 TimeoutCancellationException，它是 CancellationException 的子类。
//    我们之前没有在控制台上看到堆栈跟踪信息的打印。这是因为在被取消的协程中 CancellationException 被认为是协程执行结束的正常原因。
//    然而，在这个示例中我们在 main 函数中正确地使用了 withTimeout。
}

//运行不能取消的代码块
//    在前一个例子中任何尝试在 finally 块中调用挂起函数的行为都会抛出 CancellationException，
//    因为这里持续运行的代码是可以被取消的。通常，这并不是一个问题，所有良好的关闭操作
//    （关闭一个文件、取消一个作业、或是关闭任何一种通信通道）通常都是非阻塞的，并且不会调用任何挂起函数。然而，在真实的案例中，
//    当你需要挂起一个被取消的协程，你可以将相应的代码包装在 withContext(NonCancellable) {……} 中，
//    并使用withContext 函数以及 NonCancellable 上下文，见如下示例所示：
suspend fun 运行不能取消的代码块() {
    val job = GlobalScope.launch {
        try {
            repeat(1000) { i ->
                println("job: I'm sleeping $i ...")
                delay(500L)
            }
        } finally {
            withContext(NonCancellable) {
                println("job: I'm running finally")
                delay(1000L)
                println("job: And I've just delayed for 1 sec because I'm non-cancellable")
            }
        }
    }
    delay(1300L) // 延迟一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消该作业并等待它结束
    println("main: Now I can quit.")
}

//在finally中释放资源
suspend fun 在finally中释放资源() {
    val job = GlobalScope.launch {
        try {
            repeat(1000) { i ->
                println("job: I'm sleeping $i ...isActive ==" + isActive)
                delay(500L)
            }
        } finally {
//            delay(500L) 这里调用挂起函数，下面这条执行不到，所以有了下面这个例子
            println("job: I'm running finally  isActive ==" + isActive)
        }
    }
    delay(1300L) // 延迟一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消该作业并且等待它结束
    println("main: Now I can quit.")
}

//    使计算代码可取消
//    我们有两种方法来使执行计算的代码可以被取消。第一种方法是定期调用挂起函数来检查取消。
//    对于这种目的 yield 是一个好的选择。 另一种方法是显式的检查取消状态。让我们试试第二种方法。
//在 finally 中释放资源 ，注意try的位置
suspend fun 使计算代码可取消() {
    val startTime = System.currentTimeMillis()
    val job = GlobalScope.launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (isActive) { // 可以被取消的计算循环
            // 每秒打印消息两次
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L) // 等待一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消该作业并等待它结束
    println("main: Now I can quit.")
}

//    取消是协作的
//    协程的取消是 协作 的。一段协程代码必须协作才能被取消。 所有 kotlinx.coroutines 中的挂起函数都是 可被取消的 。
//    它们检查协程的取消， 并在取消时抛出 CancellationException。 然而，如果协程正在执行计算任务，并且没有检查取消的话，
//    那么它是不能被取消的
suspend fun 取消是协作的() {
    val startTime = System.currentTimeMillis()
    val job = GlobalScope.launch(Dispatchers.Default) {
        var nextPrintTime = startTime
        var i = 0
        while (i < 5) { // 一个执行计算的循环，只是为了占用 CPU
            // 每秒打印消息两次
            if (System.currentTimeMillis() >= nextPrintTime) {
                println("job: I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    }
    delay(1300L) // 等待一段时间
    println("main: I'm tired of waiting!")
    job.cancelAndJoin() // 取消一个作业并且等待它结束
    println("main: Now I can quit.")
}

//    取消协程的执行
suspend fun 取消协程的执行() {
    val job = GlobalScope.launch {
        repeat(1000) { i ->
            println("job: I'm sleeping $i ...")
            //delay(500L)  如果try catch 了，会继续执行下去

            //            try {
            //                delay(500L)
            //            } catch (e: Exception) {
            //                println(" Exception " + e.message)
            //            }

            //这个也只会调用3次，但是每次都会打印I'm running finally
//            try {
//                delay(500L)
//            } finally {
//                println("job: I'm running finally")
//            }
        }
    }
    delay(1300L) // 延迟一段时间
    println("main: I'm tired of waiting!")
    job.cancel() // 取消该作业
    job.join() // 等待作业执行结束
    println("main: Now I can quit.")

    //    一旦 main 函数调用了 job.cancel，我们在其它的协程中就看不到任何输出，因为它被取消了。
//    这里也有一个可以使 Job 挂起的函数 cancelAndJoin 它合并了对 cancel 以及 join 的调用
}







