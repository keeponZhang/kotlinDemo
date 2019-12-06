package kotlinx.coroutines.guide.context09

import kotlinx.coroutines.*


//有时我们需要在协程上下文中定义多个元素。我们可以使用 + 操作符来实现。
// 比如说，我们可以显式指定一个调度器来启动协程并且同时显式指定一个命名：
fun main() = runBlocking<Unit> {
    launch(Dispatchers.Default + CoroutineName("test")) {
        println("I'm working in thread ${Thread.currentThread().name}")
    }
}


//这段代码使用了 -Dkotlinx.coroutines.debug JVM 参数，输出如下所示：
//I'm working in thread DefaultDispatcher-worker-1 @test#2






