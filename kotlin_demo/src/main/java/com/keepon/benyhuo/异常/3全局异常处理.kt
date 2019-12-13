package com.keepon.benyhuo.异常3全局异常处理.kt

import com.keepon.coroutines.utils.log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * createBy	keepon
 */
 suspend fun main(){
    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        log("Throws an exception with message: ${throwable.message}")
    }

    log(1)
    GlobalScope.launch(exceptionHandler) {
        delay(10)
        throw ArithmeticException("Hey!")
    }.join()
    log(2)
}
