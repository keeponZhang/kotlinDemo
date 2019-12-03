package com.keepon.coroutines.interfaces;

interface Callback<T> {
    fun onSuccess(value: T)

    fun onError(t: Throwable)
}

