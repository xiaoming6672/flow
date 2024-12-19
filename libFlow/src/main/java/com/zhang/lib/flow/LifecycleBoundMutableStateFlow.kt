package com.zhang.lib.flow

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

/**
 * 生命周期绑定的流
 *
 * @author ZhangXiaoMing 2024-12-07 15:08 周六
 */
class LifecycleBoundMutableStateFlow<T>(
    val owner : LifecycleOwner ,
    val minActiveState : Lifecycle.State = Lifecycle.State.STARTED ,
    val flow : MutableStateFlow<T> ,
) : DefaultLifecycleObserver {

    @PublishedApi
    internal var collectJob : Job? = null

    val job get() = collectJob


    init {
        owner.lifecycle.addObserver(observer = this)
    }

    /**
     * The current value of this state flow.
     *
     * Setting a value that is [equal][Any.equals] to the previous one does nothing.
     *
     * This property is **thread-safe** and can be safely updated from concurrent coroutines without
     * external synchronization.
     */
    var value : T
        get() = flow.value
        set(value) {
            flow.value = value
        }

    override fun onDestroy(owner : LifecycleOwner) {
        owner.lifecycle.removeObserver(observer = this)
        cancel()
    }


    inline fun collect(crossinline block : (T) -> Unit) {
        collectJob = owner.lifecycleScope.launch {
            flow.flowWithLifecycle(owner.lifecycle , minActiveState)
                .collect { block(it) }
        }
    }

    fun cancel() {
        collectJob?.cancel()
        collectJob = null
    }
}


/**
 * Updates the [MutableStateFlow.value] atomically using the specified [function] of its value, and returns the new
 * value.
 *
 * [function] may be evaluated multiple times, if [value] is being concurrently updated.
 */
inline fun <T> LifecycleBoundMutableStateFlow<T>.updateAndGet(function : (T) -> T) : T =
    flow.updateAndGet(function)

/**
 * Updates the [MutableStateFlow.value] atomically using the specified [function] of its value, and returns its
 * prior value.
 *
 * [function] may be evaluated multiple times, if [value] is being concurrently updated.
 */
inline fun <T> LifecycleBoundMutableStateFlow<T>.getAndUpdate(function : (T) -> T) : T =
    flow.getAndUpdate(function)

/**
 * Updates the [MutableStateFlow.value] atomically using the specified [function] of its value.
 *
 * [function] may be evaluated multiple times, if [value] is being concurrently updated.
 */
inline fun <T> LifecycleBoundMutableStateFlow<T>.update(function : (T) -> T) =
    flow.update(function)