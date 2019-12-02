package com.keepon.coroutines.my

/**
 * createBy	 keepon
 */
open  interface IMyContext {
    fun test():Unit
    //这个接口只是标志，没有方法
    public interface Key<E : Element>
    public operator fun <E : Element> get(key: Key<E>): E?
    public interface Element : IMyContext {
        public val key: Key<*>
    }


}

public abstract class AbstractCoroutineContextElement(public override val key: IMyContext.Key<*>) : IMyContext.Element