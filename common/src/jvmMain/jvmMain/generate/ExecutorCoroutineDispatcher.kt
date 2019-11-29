// IntelliJ API Decompiler stub source generated from a class file
// Implementation of methods is not available

package kotlinx.coroutines

public abstract class ExecutorCoroutineDispatcher public constructor() : kotlinx.coroutines.CoroutineDispatcher, java.io.Closeable {
    public abstract val executor: java.util.concurrent.Executor

    public abstract fun close(): kotlin.Unit
}

