package com.keepon.benyhuo.异常4_3异常传播.kt

import com.keepon.coroutines.utils.log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * createBy	keepon
 */
val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
    log("${coroutineContext[CoroutineName]} $throwable")
}

suspend fun main() {
    log(1)
    try {
        supervisorScope() {
            //①
            log(2)
            launch(exceptionHandler + CoroutineName("②")) {
                // ②
                log(3)
                launch(exceptionHandler + CoroutineName("③")) {
                    // ③
                    log(4)
                    delay(100)
                    throw ArithmeticException("Hey!!")
                }
                log(5)
            }
            log(6)
            val job = launch {
                // ④
                log(7)
                try {
                    delay(1000)
                } catch (e: java.lang.Exception) {
                    log("④ Exception " + e.message)
                }

            }
            try {
                log(8)
                log("job is Active " + job.isActive)
                job.join()
                log("9")
            } catch (e: Exception) {
                log("10. $e")
            }
        }
        log(11)
    } catch (e: Exception) {
        log("12. $e")
    }
    log(13)
}

//再运行这段程序，结果就比较有意思了
//
//...
//07:30:11:519 [DefaultDispatcher-worker-1] CoroutineName(②) java.lang.ArithmeticException: Hey!!
//...


//我们发现触发的 CoroutineExceptionHandler 竟然是协程 ② 的，意外吗？不意外，因为我们前面已经提到，
//对于 supervisorScope 的子协程 （例如 ②）的子协程（例如 ③），如果没有明确指出，它是遵循默认的作用于规则的，
//也就是 coroutineScope 的规则了，出现未捕获的异常会尝试传递给父协程并尝试取消父协程。
//
//
//究竟使用什么 Scope，大家自己根据实际情况来确定，我给出一些建议：
//
//对于没有协程作用域，但需要启动协程的时候，适合用 GlobalScope
//对于已经有协程作用域的情况（例如通过 GlobalScope 启动的协程体内），直接用协程启动器启动
//对于明确要求子协程之间相互独立不干扰时，使用 supervisorScope
//对于通过标准库 API 创建的协程，这样的协程比较底层，没有 Job、作用域等概念的支撑，例如我们前面提到过 suspend main 就是这种情况，对于这种情况优先考虑通过 coroutineScope 创建作用域；更进一步，大家尽量不要直接使用标准库 API，除非你对 Kotlin 的协程机制非常熟悉。
//当然，对于可能出异常的情况，请大家尽量做好异常处理，不要将问题复杂化
//
//
//作者：bennyhuo
//链接：http://www.imooc.com/article/286070
//来源：慕课网
//本文原创发布于慕课网 ，转载请注明出处，谢谢合作
