package com.zhang.lib.flow

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 生命周期绑定的流
 *
 * @author ZhangXiaoMing 2024-12-07 20:14 周六
 */
class LifecycleBoundFlow<T>(
    val owner : LifecycleOwner ,
    val flow : Flow<T> ,
) : DefaultLifecycleObserver {


    @PublishedApi
    internal var collectJob : Job? = null

    val job get() = collectJob


    init {
        owner.lifecycle.addObserver(observer = this)
    }

    override fun onDestroy(owner : LifecycleOwner) {
        owner.lifecycle.removeObserver(observer = this)
        cancel()
    }


    inline fun collect(crossinline block : (T) -> Unit) {
        collectJob = owner.lifecycleScope.launch {
            flow.collect {
                block(it)
            }
        }
    }

    fun cancel() {
        collectJob?.cancel()
        collectJob = null
    }

}