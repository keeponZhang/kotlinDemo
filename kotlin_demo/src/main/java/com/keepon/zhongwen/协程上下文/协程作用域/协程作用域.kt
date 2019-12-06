package com.keepon.zhongwen.协程上下文.协程作用域

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * createBy	 keepon
 */
fun main() = runBlocking {
    val activity = Activity()
    activity.doSomething() // 运行测试函数
    println("Launched coroutines")
    delay(500L) // 延迟半秒钟
    println("Destroying activity!")
    activity.destroy() // 取消所有的协程
    delay(1000) // 为了在视觉上确认它们没有工作
}
//这个示例的输出如下所示：
//Launched coroutines
//Coroutine 0 is done
//Coroutine 1 is done
//Destroying activity!
//
//你可以看到，只有前两个协程打印了消息，而另一个协程在 Activity.destroy() 中单次调用了 job.cancel()。













