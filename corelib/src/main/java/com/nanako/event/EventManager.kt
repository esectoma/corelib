package com.nanako.event

/**
 * Created by barry on 2022/12/16
 */
class EventManager private constructor() {

    companion object {
        val em = EventManager()
    }

    private val listeners = mutableListOf<Listener>()

    fun registListener(listener: Listener) {
        listeners.add(listener)
    }

    fun unRegistListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun notify(event: Any) {
        for (i in listeners.size - 1 downTo 0) {
            listeners[i].onEvent(event)
        }
    }

    interface Listener {
        fun onEvent(event: Any)
    }
}