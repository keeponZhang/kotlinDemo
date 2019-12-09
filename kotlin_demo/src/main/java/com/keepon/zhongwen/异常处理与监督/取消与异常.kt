package kotlinx.coroutines.guide.exceptions03

import kotlinx.coroutines.*
import java.lang.Exception

fun main() = runBlocking {
    val job = launch {
        val child = launch {
            try {
                delay(Long.MAX_VALUE)
            } catch (e: Exception) {
                println("e Exception "+e.message) //Job was cancelled
            }  finally {
                println("Child is cancelled "+ isActive)
            }
        }
        yield()
        println("Cancelling child isActive= " + child.isActive)
        child.cancel()
        child.join()
        println("Cancelling child after isActive= " + child.isActive)
        yield()
        println("Parent is not cancelled  isActive= " + isActive)
    }
    job.join()
}

//这段代码的输出如下：
//Cancelling child
//Child is cancelled
//Parent is not cancelled


//取消与异常紧密相关。协程内部使用 CancellationException 来进行取消，这个异常会被所有的处理者忽略，
//所以那些可以被 catch 代码块捕获的异常仅仅应该被用来作为额外调试信息的资源。
//当一个协程使用 Job.cancel 取消的时候，它会被终止，但是它不会取消它的父协程。

//如果协程遇到除 CancellationException 以外的异常，它将取消具有该异常的父协程。
//这种行为不能被覆盖，且它被用来提供一个稳定的协程层次结构来进行结构化并发而无需依赖
//CoroutineExceptionHandler 的实现。
//且当所有的子协程被终止的时候，原本的异常被父协程所处理。




















