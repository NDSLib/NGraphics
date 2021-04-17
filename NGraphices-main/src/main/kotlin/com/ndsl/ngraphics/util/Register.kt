package com.ndsl.ngraphics.util

/**
 * Simple Register
 */
class Register<E>() {
    private val callBacks:MutableList<(E)->Unit> = mutableListOf()

    fun invoke(e:E){
        callBacks.forEach { it(e) }
    }

    fun register(f:(E)->Unit){
        callBacks.add(f)
    }
}

/**
 * Typed Register
 * E -> Real Event
 * T -> Event Type(maybe Enum)
 */
class TypedRegister<E,T>(){
    private val callBacks:MutableMap<T, MutableList<(E)->Unit>> = mutableMapOf()

    fun register(type:T,f:(E)->Unit){
        if(!callBacks.containsKey(type)) callBacks[type] = mutableListOf()
        callBacks[type]!!.add(f)
    }

    fun register(f:(E)->Unit,vararg type: T){
        type.forEach {
            register(it,f)
        }
    }

    fun invoke(t:T,e:E){
        if(callBacks.containsKey(t)){
            callBacks[t]!!.forEach { it(e) }
        }
    }
}