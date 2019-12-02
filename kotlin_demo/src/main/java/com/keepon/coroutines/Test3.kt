package com.keepon.coroutines

import com.keepon.coroutines.utils.log
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * createBy	 keepon
 */
suspend fun main() {
    //default
/*    log("default "+1)
    val job0 = GlobalScope.launch {
        log("default "+2)
    }
    log("default "+3)
    //如果是 DEFAULT 模式，在第一次调度该协程时如果 cancel 就已经调用，那么协程就会直接被 cancel 而不会有任何调用,当然也有可能协程开始时尚未被 cancel，那么它就可以正常启动了
    //作者：bennyhuo
    //链接：http://www.imooc.com/article/285100
    //来源：慕课网
    //本文原创发布于慕课网 ，转载请注明出处，谢谢合作
    job0.cancel()
//    job0.join()  如果main没结束，不调用join也可以执行
    log("default "+4)*/



    //LAZY
/*    log("lazy "+1)
    val job1 = GlobalScope.launch(start = CoroutineStart.LAZY) {
        log("lazy "+2)
    }
    log("lazy "+3)
    //lazy没调用start log(2)不会调用
//    job1.start()
    log("lazy "+4)*/


    //ATOMIC
    //我们创建了协程后立即 cancel，但由于是 ATOMIC 模式，因此协程一定会被调度(把sleep注释掉也可以)，因此 1、2、3 一定都会输出，只是 2 和 3 的顺序就难说了
    //作者：bennyhuo
    //链接：http://www.imooc.com/article/285100
    //来源：慕课网
    //本文原创发布于慕课网 ，转载请注明出处，谢谢合作
/*
    log("ATOMIC "+1)
    val job2 = GlobalScope.launch(start = CoroutineStart.ATOMIC) {
        log("ATOMIC "+2)
    }
    job2.cancel()
    log("ATOMIC "+3)
*/



//    需要注意的是，cancel 调用一定会将该 job 的状态置为 cancelling，只不过ATOMIC 模式的协程在启动时无视了这一状态。为了证明这一点，我们可以让例子稍微复杂一些：
/*    log("ATOMIC 复杂 "+1)
    val job3 = GlobalScope.launch(start = CoroutineStart.ATOMIC) {
        log("ATOMIC 复杂 "+2)
        delay(1000)
        log("ATOMIC 复杂 "+3)
    }
    job3.cancel()
    log("ATOMIC 复杂 "+4)
    job3.join()*/

//    我们在 2 和 3 之间加了一个 delay，delay 会使得协程体的执行被挂起，1000ms 之后再次调度后面的部分，
//    因此 3 会在 2 执行之后 1000ms 时输出。对于 ATOMIC 模式，我们已经讨论过它一定会被启动，实际上在遇到第一个挂起点之前，
//    它的执行是不会停止的，而 delay 是一个 suspend 函数，这时我们的协程迎来了自己的第一个挂起点，恰好 delay 是支持 cancel 的，
//    因此后面的 3 将不会被打印。


//    我们使用线程的时候，想要让线程里面的任务停止执行也会面临类似的问题，但遗憾的是线程中看上去与 cancel 相近的
//    stop 接口已经被废弃，因为存在一些安全的问题。不过随着我们不断地深入探讨，你就会发现协程的 cancel 某种意义上更像线程的 interrupt。


    val thread = Thread({

    })
    thread.name = "keepon"
    thread.start()

    log(" UNDISPATCHED "+1)
    val job = GlobalScope.launch(start = CoroutineStart.UNDISPATCHED) {
        log(" UNDISPATCHED "+2)
        delay(100)
        log(" UNDISPATCHED "+3)
    }
    log(" UNDISPATCHED "+4)
    job.join()
    log(" UNDISPATCHED "+5)


//    我们还是以这样一个例子来认识下 UNDISPATCHED 模式，按照我们前面的讨论，协程启动后会立即在当前线程执行，
//    因此 1、2 会连续在同一线程中执行，delay 是挂起点，因此 3 会等 100ms 后再次调度，这时候 4 执行，
//    join 要求等待协程执行完，因此等 3 输出后再执行 5。以下是运行结果：

//    16:23:51:092 [main]  UNDISPATCHED 1
//    16:23:51:140 [main]  UNDISPATCHED 2
//    16:23:51:161 [main]  UNDISPATCHED 4
//    16:23:51:265 [DefaultDispatcher-worker-1]  UNDISPATCHED 3
//    16:23:51:267 [DefaultDispatcher-worker-1]  UNDISPATCHED 5
//    方括号当中是线程名，我们发现协程执行时会修改线程名来让自己显得颇有存在感。运行结果看上去还有一个细节可能会让人困惑，join 之后的 5 的线程与 3 一样，这是为什么？
//    我们在前面提到我们的示例都运行在 suspend main 函数当中，所以 suspend main 函数会帮我们直接启动一个协程，
//    而我们示例的协程都是它的子协程，所以这里 5 的调度取决于这个最外层的协程的调度规则了。关于协程的调度，我们后面再聊。



    Thread.sleep(3000)
}

fun test(){
    log("keepon")
}