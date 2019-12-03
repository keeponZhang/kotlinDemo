1. AbstractCoroutineContextElement 其实就是重写了key
2. CoroutineDispatcher  它本身是协程上下文的子类，同时实现了拦截器的接口， dispatch 方法会在拦截器的方法 interceptContinuation 中调用，进而实现协程的调度。所以如果我们想要实现自己的调度器，继承这个类就可以了，不过通常我们都用现成的，它们定义在 Dispatchers 当中：
   ```java 
   val Default: CoroutineDispatcher
    val Main: MainCoroutineDispatcher
    val Unconfined: CoroutineDispatcher
    ```
    这个类的定义涉及到了 Kotlin MPP 的支持，因此你在 Jvm 版本当中还会看到 val IO: CoroutineDispatcher，在 js 和 native 当中就只有前面提到的这三个了（对 Jvm 好偏心呐）。
    









































































































