package com.rubengees.ktask.util

import com.rubengees.ktask.base.Task
import com.rubengees.ktask.operation.*

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class TaskBuilder<I, O, T : Task<I, O>> private constructor(private var currentTask: T) {

    companion object {
        fun <I, O, T : Task<I, O>> task(task: T) = TaskBuilder(task)
    }

    fun cache(strategy: CacheTask.CacheStrategy = CacheTask.CacheStrategy.FULL) = task(CacheTask(currentTask, strategy))

    fun inputEcho() = task(InputEchoTask(currentTask))

    fun <M> map(function: (O) -> M) = task(MapTask(currentTask, function))

    fun <OI, OO, FO> parallelWith(other: Task<OI, OO>, zipFunction: (O, OO) -> FO) =
            task(ParallelTask(currentTask, other, zipFunction))

    fun <OI, OO, FO> parallelWith(other: TaskBuilder<OI, OO, Task<OI, OO>>,
                                  zipFunction: (O, OO) -> FO,
                                  awaitLeftResultOnError: Boolean = false,
                                  awaitRightResultOnError: Boolean = false)
            = task(ParallelTask(currentTask, other.build(), zipFunction, awaitLeftResultOnError,
            awaitRightResultOnError))

    fun <OI, OO> then(other: Task<OI, OO>, mapFunction: (O) -> OI = {
        @Suppress("UNCHECKED_CAST")
        it as OI
    }) = task(StreamTask(currentTask, other, mapFunction))

    fun <OI, OO> then(other: TaskBuilder<OI, OO, Task<OI, OO>>, mapFunction: (O) -> OI = {
        @Suppress("UNCHECKED_CAST")
        it as OI
    }) = task(StreamTask(currentTask, other.build(), mapFunction))

    fun validateBefore(function: (I) -> Unit) = task(ValidatingTask(currentTask, function))

    fun onStart(callback: () -> Unit) = this.apply { currentTask.onStart(callback) }
    fun onSuccess(callback: (O) -> Unit) = this.apply { currentTask.onSuccess(callback) }
    fun onError(callback: (Throwable) -> Unit) = this.apply { currentTask.onError(callback) }
    fun onFinish(callback: () -> Unit) = this.apply { currentTask.onFinish(callback) }
    fun onInnerStart(callback: () -> Unit) = this.apply { currentTask.onInnerStart(callback) }

    fun build(): T {
        return currentTask
    }
}
