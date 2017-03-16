package com.rubengees.ktask.util

import com.rubengees.ktask.base.Task
import com.rubengees.ktask.operation.*

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class TaskBuilder<I, O, T : Task<I, O, T>> private constructor(private var currentTask: T) {

    companion object {
        fun <I, O, T : Task<I, O, T>> task(task: T) = TaskBuilder(task)
    }

    fun cache(strategy: CacheTask.CacheStrategy = CacheTask.CacheStrategy.FULL) = task(CacheTask(currentTask, strategy))

    fun inputEcho() = task(InputEchoTask(currentTask))

    fun <M> map(function: (O) -> M) = task(MapTask(currentTask, function))

    fun <OI, OO, FO, OT : Task<OI, OO, OT>> parallelWith(other: OT, zipFunction: (O, OO) -> FO) =
            task(ParallelTask(currentTask, other, zipFunction))

    fun <OI, OO, FO, OT : Task<OI, OO, OT>> parallelWith(other: TaskBuilder<OI, OO, OT>, zipFunction: (O, OO) -> FO,
                                                         awaitLeftResultOnError: Boolean = false,
                                                         awaitRightResultOnError: Boolean = false) =
            task(ParallelTask(currentTask, other.build(), zipFunction,
                    awaitLeftResultOnError, awaitRightResultOnError))

    fun <OI, OO, OT : Task<OI, OO, OT>> then(other: OT, mapFunction: (O) -> OI = {
        @Suppress("UNCHECKED_CAST")
        it as OI
    }) = task(StreamTask(currentTask, other, mapFunction))

    fun <OI, OO, OT : Task<OI, OO, OT>> then(other: TaskBuilder<OI, OO, OT>, mapFunction: (O) -> OI = {
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
