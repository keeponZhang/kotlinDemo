package com.keepon.coroutines

import kotlinx.coroutines.*

/**
 * createBy	 keepon
 */

//3. 协程的取消
//前面分析协程父子关系中的取消协程时，可以知道协程的取消只是在协程的第一层包装中 AbstractCoroutine 中修改协程的状态，
//并没有影响到第二层包装中 BaseContinuationImpl 中协程的实际运算逻辑。所以协程的取消只是状态的变化，
//并不会取消协程的实际运算逻辑，看下面的代码示例：

fun main(args: Array<String>) = runBlocking {
    val job1 = launch(Dispatchers.Default) {
        repeat(5) {
            println("job1 sleep ${it + 1} times")
            delay(500)
        }
    }
    delay(700)
    job1.cancel()

    val job2 = launch(Dispatchers.Default) {
        var nextPrintTime = 0L
        var i = 1
        while (i <= 13) {
            val currentTime = System.currentTimeMillis()
            if (currentTime >= nextPrintTime) {
                println("job2 sleep ${i++} ...")
                nextPrintTime = currentTime + 500L
            }
        }
        while (isActive) {
            val currentTime = System.currentTimeMillis()
            if (currentTime >= nextPrintTime) {
                println("job3 sleep ${i++} ...")
                nextPrintTime = currentTime + 500L
            }
        }
    }
    delay(700)
    job2.cancel()
}


//输出结果如下：
//job1 sleep 1 times
//job1 sleep 2 times
//job2 sleep 1 ...
//job2 sleep 2 ...
//job2 sleep 3 ...


//上面代码中 job1 取消后，delay()会检测协程是否已取消，所以 job1 之后的运算就结束了；而 job2 取消后，没有检测协程状态的逻辑，都是计算逻辑，所以 job2 的运算逻辑还是会继续运行。
//
//所以为了可以及时取消协程的运算逻辑，可以检测协程的状态，使用isActive来判断，上面示例中可以将while(i <= 3)替换为while(isActive)。







//4小结
//
//最后总结下本文的内容，封装异步代码为挂起函数其实非常简单，只需要用suspendCoroutine{}或suspendCancellableCoroutine{}，还要异步逻辑完成用resume()或resumeWithException来恢复协程。
//
//新建协程时需要协程间关系，GlobalScope.launch{}和GlobalScope.async{}新建的协程是没有父协程的，而在协程中使用launch{}和aysnc{}一般都是子协程。对于父子协程需要注意下面三种关系：
//
//父协程手动调用cancel()或者异常结束，会立即取消它的所有子协程。
//
//父协程必须等待所有子协程完成（处于完成或者取消状态）才能完成。
//
//子协程抛出未捕获的异常时，默认情况下会取消其父协程。
//
//对于协程的取消，cancel()只是将协程的状态修改为已取消状态，并不能取消协程的运算逻辑，协程库中很多挂起函数都会检测协程状态，如果想及时取消协程的运算，最好使用isActive判断协程状态。
//
//作者：JohnnyShieh
//链接：https://www.jianshu.com/p/2857993af646
//来源：简书
//著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。


















