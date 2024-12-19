package com.zhang.lib.flow

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

/*
 * LifecycleOwner收集Flow的拓展函数
 *
 * @author ZhangXiaoMing 2024-12-19 12:00 周四
 */


/**
 * 收集流
 *
 * @param flow 流
 * @param collector 收集监听
 *
 * @param T 泛型
 *
 * @return 返回[Job]，可以用于取消收集
 */
fun <T> LifecycleOwner.collectFlow(flow : Flow<T> , collector : FlowCollector<T>) : Job = lifecycleScope.launch {
    flow.collect(collector)
}

/**
 * 收集流
 *
 * @param flow 流
 * @param minActiveState 流收集的最低[Lifecycle.State]，如果低于该状态则停止收集，如果高于等于该状态则重新开始收集
 * @param collector 收集监听
 *
 * @param T 泛型
 */
fun <T> LifecycleOwner.collectFlow(
    flow : Flow<T> ,
    minActiveState : Lifecycle.State = Lifecycle.State.STARTED ,
    collector : FlowCollector<T>
) = lifecycleScope.launch {
    flow.flowWithLifecycle(lifecycle , minActiveState)
        .collect(collector)
}


/**
 * 流收集监听，且限制次数
 *
 * @param flow 流
 * @param maxCount 最大次数上限
 * @param block 收集回调
 *
 * @param T 数据泛型
 * @param FLOW Flow流类型
 */
fun <T> LifecycleOwner.collectFlowWithCount(
    flow : Flow<T> ,
    maxCount : Int ,
    block : (T) -> Unit ,
) = flow.collectWithCount(owner = this , maxCount = maxCount , block = block)

/**
 * 流收集监听，且限制次数
 *
 * @param flow 流
 * @param minActiveState 流收集的最低[Lifecycle.State]，如果低于该状态则停止收集，如果高于等于该状态则重新开始收集
 * @param maxCount 最大次数上限
 * @param block 收集回调
 *
 * @param T 数据泛型
 * @param FLOW Flow流类型
 */
fun <T> LifecycleOwner.collectFlowWithCount(
    flow : Flow<T> ,
    minActiveState : Lifecycle.State = Lifecycle.State.STARTED ,
    maxCount : Int ,
    block : (T) -> Unit ,
) = flow.collectWithCount(owner = this , minActiveState = minActiveState , maxCount = maxCount , block = block)