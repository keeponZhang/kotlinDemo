package kotlinx.coroutines.guide.context11

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

val threadLocal = ThreadLocal<String?>() // declare thread-local variable

//有时，能够将一些线程局部数据传递到协程与协程之间是很方便的。 然而，由于它们不受任何特定线程的约束，如果手动完成，可能会导致出现样板代码。
//
//ThreadLocal， asContextElement 扩展函数在这里会充当救兵。它创建了额外的上下文元素， 且保留给定 ThreadLocal 的值，并在每次协程切换其上下文时恢复它。
//
//它很容易在下面的代码中演示：


fun main() = runBlocking<Unit> {
    threadLocal.set("main")
    println(
        "Pre-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
    val job = launch(Dispatchers.Default + threadLocal.asContextElement(value = "launch")) {
        println(
            "Launch start, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
        yield()
        println(
            "After yield, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
    }
    job.join()
    println(
        "Post-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
}


//在这个例子中我们使用 Dispatchers.Default 在后台线程池中启动了一个新的协程，所以它工作在线程池中的不同线程中，但它仍然具有线程局部变量的值， 我们指定使用 threadLocal.asContextElement(value = "launch")，
//无论协程执行在什么线程中都是没有问题的。 因此，其输出如（调试）所示：
//
//Pre-main, current thread: Thread[main @coroutine#1,5,main], thread local value: 'main'
//Launch start, current thread: Thread[DefaultDispatcher-worker-1 @coroutine#2,5,main], thread local value: 'launch'
//After yield, current thread: Thread[DefaultDispatcher-worker-2 @coroutine#2,5,main], thread local value: 'launch'
//Post-main, current thread: Thread[main @coroutine#1,5,main], thread local value: 'main'

//这很容易忘记去设置相应的上下文元素。如果运行协程的线程不同， 在协程中访问的线程局部变量则可能会产生意外的值。 为了避免这种情况，建议使用 ensurePresent 方法并且在不正确的使用时快速失败。
//
//
//ThreadLocal 具有一流的支持，可以与任何 kotlinx.coroutines 提供的原语一起使用。 但它有一个关键限制，即：当一个线程局部变量变化时，则这个新值不会传播给协程调用者（因为上下文元素无法追踪所有 ThreadLocal 对象访问），并且下次挂起时更新的值将丢失。 使用 withContext 在协程中更新线程局部变量，详见 asContextElement。
//
//另外，一个值可以存储在一个可变的域中，例如 class Counter(var i: Int)，是的，反过来， 可以存储在线程局部的变量中。然而，在这个案例中你完全有责任来进行同步可能的对这个可变的域进行的并发的修改。
//
//对于高级的使用，例如，那些在内部使用线程局部传递数据的用于与日志记录 MDC 集成，以及事务上下文或任何其它库，请参见需要实现的 ThreadContextElement 接口的文档。
//









