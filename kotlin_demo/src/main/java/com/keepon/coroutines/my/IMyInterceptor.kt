package com.keepon.coroutines.my

/**
 * createBy	 keepon
 */
interface  IMyInterceptor : IMyContext.Element {
    //其实是用成员
    companion object Key: IMyContext.Key<IMyInterceptor> {
    }

    // Performance optimization for a singleton Key
    public override operator fun <E : IMyContext.Element> get(key: IMyContext.Key<E>): E? {
        @Suppress("UNCHECKED_CAST")
        System.out.println("IMyInterceptor get:" +key)
      return  if (key === IMyInterceptor) this as E else null
    }

}