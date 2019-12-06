package com.keepon.zhongwen

import com.keepon.coroutines.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

/**
 * createBy	 keepon
 */
/*fun main() {
//    你的第一个协程程序()
//    桥接阻塞与非阻塞的世界()
}*/
fun main() = runBlocking {
    //    等待一个作业()


    //结构化的并发 注意，这里使用的是launch，而不是GlobalScope.launch
    //我们可以在执行操作所在的指定作用域内启动协程， 而不是像通常使用线程（线程总是全局的）那样在 GlobalScope 中启动
    /*  launch {
          // 在 runBlocking 作用域中启动一个新协程
          delay(1000L)
          println("World!")
      }
      println("Hello,")*/


//    作用域构建器
//    除了由不同的构建器提供协程作用域之外，还可以使用 coroutineScope 构建器声明自己的作用域。
//    它会创建一个协程作用域并且在所有已启动子协程执行完毕之前不会结束。
//    runBlocking 与 coroutineScope 的主要区别在于后者在等待所有子协程执行完毕时不会阻塞当前线程
    /*launch {
        delay(200L)
        println("Task from runBlocking")
    }

    coroutineScope {
        // 创建一个协程作用域
        launch {
            delay(500L)
            println("Task from nested launch")
        }

        delay(100L)
        println("Task from coroutine scope") // 这一行会在内嵌 launch 之前输出
    }

    println("Coroutine scope is over") // 这一行在内嵌 launch 执行完毕后才输出*/


//    提取函数重构
//    我们来将 launch { …… } 内部的代码块提取到独立的函数中。当你对这段代码执行“提取函数”重构时，
//    你会得到一个带有 suspend 修饰符的新函数。 那是你的第一个挂起函数。在协程内部可以像普通函数一样使用挂起函数，
//    不过其额外特性是，同样可以使用其他挂起函数（如本例中的 delay）来挂起协程的执行

    /* launch { doWorld() }
     println("Hello,")*/


//    协程很轻量
    /*   repeat(100_000) {
           // 启动大量的协程
           launch {
               delay(1000L)
               print(".")
           }
       }*/


//    全局协程像守护线程

    GlobalScope.launch {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
    }
    delay(1300L) // 在延迟后退出
}

// 这是你的第一个挂起函数
suspend fun doWorld() {
    delay(1000L)
    println("World!")
}

//调用挂起函数必须是挂起函数
suspend fun 等待一个作业() {
    val job = GlobalScope.launch {
        // 启动一个新协程并保持对这个作业的引用
        delay(1000L)
        println("World!")
    }
    println("Hello,")
    job.join() // 等待直到子协程执行结束
}
//这个示例可以使用更合乎惯用法的方式重写
/*fun main() = runBlocking<Unit> {
    // 开始执行主协程
    GlobalScope.launch {
        // 在后台启动一个新的协程并继续
        delay(1000L)
        println("World!")
    }
    println("Hello,") // 主协程在这里会立即执行
    delay(2000L)      // 延迟 2 秒来保证 JVM 存活
}*/

fun 桥接阻塞与非阻塞的世界() {
    println("桥接阻塞与非阻塞的世界")
    GlobalScope.launch {
        // 在后台启动一个新的协程并继续
        delay(1000L)
        println("World!")
    }
    println("Hello,") // 主线程中的代码会立即执行
    runBlocking {
        // 但是这个表达式阻塞了主线程
        delay(2000L)  // ……我们延迟 2 秒来保证 JVM 的存活
    }
}

fun 你的第一个协程程序() {
    println("你的第一个协程程序")
    GlobalScope.launch {
        // 在后台启动一个新的协程并继续
        delay(1000L) // 非阻塞的等待 1 秒钟（默认时间单位是毫秒）
        println("World!") // 在延迟后打印输出
    }
    println("Hello,") // 协程已在等待时主线程还在继续
    Thread.sleep(2000L) // 阻塞主线程 2 秒钟来保证 JVM 存活
}

