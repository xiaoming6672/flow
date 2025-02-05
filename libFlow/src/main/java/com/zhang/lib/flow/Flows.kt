package com.zhang.lib.flow

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/*
 * 流的拓展函数
 *
 * @author ZhangXiaoMing 2024-11-29 11:42 周五
 */


//<editor-fold desc="流收集监听">
/**
 * 流收集监听
 *
 * @param owner 生命周期所有者
 * @param block 收集回调
 *
 * @param T 泛型
 */
inline fun <T , FLOW : Flow<T>> FLOW.collect(
    owner : LifecycleOwner ,
    crossinline block : (T) -> Unit ,
) = owner.lifecycleScope.launch {
    collect { block(it) }
}

/**
 * 流收集监听
 *
 * @param owner 生命周期所有者
 * @param minActiveState 流收集的最低[Lifecycle.State]，如果低于该状态则停止收集，如果高于等于该状态则重新开始收集
 * @param block 收集回调
 *
 * @param T 泛型
 */
inline fun <T , FLOW : Flow<T>> FLOW.collect(
    owner : LifecycleOwner ,
    minActiveState : Lifecycle.State = Lifecycle.State.STARTED ,
    crossinline block : (T) -> Unit ,
) = owner.lifecycleScope.launch {
    flowWithLifecycle(owner.lifecycle , minActiveState)
        .collect {
            block(it)
        }
}


/**
 * 流收集监听，且限制次数
 *
 * @param owner 生命周期所有者
 * @param maxCount 最大次数上限
 * @param block 收集回调
 *
 * @param T 数据泛型
 * @param FLOW Flow流类型
 */
inline fun <T , FLOW : Flow<T>> FLOW.collectWithCount(
    owner : LifecycleOwner ,
    maxCount : Int ,
    crossinline block : (T) -> Unit ,
) = apply {
    var job : Job? = null
    var count = 0

    job = owner.lifecycleScope.launch {
        collect {
            count++
            block(it)

            if (count >= maxCount) {
                job?.cancel()
                job = null
            }
        }
    }
}

/**
 * 流收集监听，且仅监听一次
 *
 * @param owner 生命周期所有者
 * @param block 收集回调
 *
 * @param T 泛型
 * @param FLOW Flow流类型
 */
inline fun <T , FLOW : Flow<T>> FLOW.collectOnlyOnce(
    owner : LifecycleOwner ,
    crossinline block : (T) -> Unit ,
) = collectWithCount(owner = owner , maxCount = 1 , block = block)


/**
 * 流收集监听，且限制次数
 *
 * @param owner 生命周期所有者
 * @param minActiveState 流收集的最低[Lifecycle.State]，如果低于该状态则停止收集，如果高于等于该状态则重新开始收集
 * @param maxCount 最大次数上限
 * @param block 收集回调
 *
 * @param T 数据泛型
 * @param FLOW Flow流类型
 */
inline fun <T , FLOW : Flow<T>> FLOW.collectWithCount(
    owner : LifecycleOwner ,
    minActiveState : Lifecycle.State = Lifecycle.State.STARTED ,
    maxCount : Int ,
    crossinline block : (T) -> Unit ,
) = apply {
    var job : Job? = null
    var count = 0

    job = owner.lifecycleScope.launch {
        flowWithLifecycle(owner.lifecycle , minActiveState)
            .collect {
                count++
                block(it)

                if (count >= maxCount) {
                    job?.cancel()
                    job = null
                }
            }
    }
}

/**
 * 流收集监听，且仅监听一次
 *
 * @param owner 生命周期所有者
 * @param minActiveState 流收集的最低[Lifecycle.State]，如果低于该状态则停止收集，如果高于等于该状态则重新开始收集
 * @param block 收集回调
 *
 * @param T 泛型
 */
inline fun <T , FLOW : Flow<T>> FLOW.collectOnlyOnce(
    owner : LifecycleOwner ,
    minActiveState : Lifecycle.State = Lifecycle.State.STARTED ,
    crossinline block : (T) -> Unit ,
) = apply {
    var job : Job? = null
    job = owner.lifecycleScope.launch {
        flowWithLifecycle(owner.lifecycle , minActiveState)
            .collect {
                block(it)

                job?.cancel()
                job = null
            }
    }
}


/**
 * 流收集监听
 *
 * @param owner 生命周期所有者
 * @param minActiveState 流收集的最低[Lifecycle.State]，如果低于该状态则停止收集，如果高于等于该状态则重新开始收集
 * @param block 收集回调
 *
 * @param T 泛型
 */
inline fun <T> MutableStateFlow<T>.collectBoundLifecycle(
    owner : LifecycleOwner ,
    minActiveState : Lifecycle.State = Lifecycle.State.STARTED ,
    crossinline block : (T) -> Unit ,
) : LifecycleBoundMutableStateFlow<T> = LifecycleBoundMutableStateFlow(owner , minActiveState , this).apply {
    collect(block)
}


/**
 * 流收集监听
 *
 * @param owner 生命周期所有者
 * @param block 收集回调
 *
 * @param T 泛型
 */
inline fun <T> Flow<T>.collectBoundLifecycle(
    owner : LifecycleOwner ,
    crossinline block : (T) -> Unit ,
) : LifecycleBoundFlow<T> {
    return LifecycleBoundFlow(owner = owner , flow = this).apply {
        collect(block)
    }
}
//</editor-fold>


//<editor-fold desc="计时器">

/**
 * 计时器
 *
 * @param interval  发送间隔
 * @param timeUnit  时间单位
 */
fun timer(interval : Int , timeUnit : TimeUnit) = when (timeUnit) {
    TimeUnit.NANOSECONDS  -> timer(interval.div(1000L).div(1000))
    TimeUnit.MICROSECONDS -> timer(interval.div(1000L))
    TimeUnit.MILLISECONDS -> timer(interval.toLong())
    TimeUnit.SECONDS      -> timer(interval.times(1000L))
    TimeUnit.MINUTES      -> timer(interval.times(60).times(1000L))
    TimeUnit.HOURS        -> timer(interval.times(60).times(60).times(1000L))
    TimeUnit.DAYS         -> timer(interval.times(24).times(60).times(60).times(1000L))
}

/**
 * 计时器
 *
 * @param interval 发送间隔，单位：毫秒
 */
fun timer(interval : Long) = flow {
    var value = 0
    while (true) {
        emit(value++)
        delay(interval)
    }
}

/**
 * 计时器
 *
 * @param interval  发送间隔
 * @param timeUnit  时间单位
 * @param map       数据转换
 *
 * @param T         泛型
 */
inline fun <T> timer(interval : Int , timeUnit : TimeUnit , crossinline map : (Int) -> T) = when (timeUnit) {
    TimeUnit.NANOSECONDS  -> timer(interval.div(1000L).div(1000) , map)
    TimeUnit.MICROSECONDS -> timer(interval.div(1000L) , map)
    TimeUnit.MILLISECONDS -> timer(interval.toLong() , map)
    TimeUnit.SECONDS      -> timer(interval.times(1000L) , map)
    TimeUnit.MINUTES      -> timer(interval.times(60).times(1000L) , map)
    TimeUnit.HOURS        -> timer(interval.times(60).times(60).times(1000L) , map)
    TimeUnit.DAYS         -> timer(interval.times(24).times(60).times(60).times(1000L) , map)
}


/**
 * 计时器
 *
 * @param interval  发送间隔，单位：毫秒
 * @param map       数据转换
 *
 * @param T         泛型
 */
inline fun <T> timer(interval : Long , crossinline map : (Int) -> T) = flow {
    var value = 0
    while (true) {
        emit(map(value++))
        delay(interval)
    }
}

/**
 * 计时器——倒数
 *
 * @param seconds 秒数
 */
fun timerDown(seconds : Int) = flow {
    var count = seconds
    while (count >= 0) {
        emit(count--)
        delay(timeMillis = 1000)
    }
}

/**
 * 计时器——正数
 *
 * @param seconds 秒数
 */
fun timerUp(seconds : Int) = flow {
    var count = 0
    while (count <= seconds) {
        emit(count++)
        delay(timeMillis = 1000)
    }
}
//</editor-fold>

