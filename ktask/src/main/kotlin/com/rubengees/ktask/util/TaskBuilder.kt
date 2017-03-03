package com.rubengees.ktask.util

import com.rubengees.ktask.base.BaseTask
import com.rubengees.ktask.base.Task
import com.rubengees.ktask.operation.*

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class TaskBuilder<I, O> private constructor(private var currentTask: BaseTask<I, O>) {

    companion object {
        fun <I, O> task(task: BaseTask<I, O>): TaskBuilder<I, O> {
            return TaskBuilder(task)
        }
    }

    fun cache(strategy: CacheTask.CacheStrategy = CacheTask.CacheStrategy.FULL): TaskBuilder<I, O> {
        return this.apply { currentTask = CacheTask(currentTask, strategy) }
    }

    fun inputEcho(): TaskBuilder<I, Pair<I, O>> {
        return TaskBuilder(InputEchoTask(currentTask))
    }

    fun <M> map(function: (O) -> M): TaskBuilder<I, M> {
        return TaskBuilder<I, M>(MapTask<I, O, M>(currentTask, function))
    }

    fun <RI, RO, NO> parallelWith(other: Task<RI, RO>,
                                  zipFunction: (O, RO) -> NO): TaskBuilder<Pair<I, RI>, NO> {
        return TaskBuilder(ParallelTask(currentTask, other, zipFunction))
    }

    fun <RI, RO, NO> parallelWith(other: TaskBuilder<RI, RO>,
                                  zipFunction: (O, RO) -> NO,
                                  awaitLeftResultOnError: Boolean = false,
                                  awaitRightResultOnError: Boolean = false): TaskBuilder<Pair<I, RI>, NO> {

        return TaskBuilder(ParallelTask(currentTask, other.build(), zipFunction, awaitLeftResultOnError,
                awaitRightResultOnError))
    }

    fun <RI, RO> then(other: Task<RI, RO>, mapFunction: (O) -> RI = {
        @Suppress("UNCHECKED_CAST")
        it as RI
    }): TaskBuilder<I, RO> {
        return TaskBuilder(StreamTask(currentTask, other, mapFunction))
    }

    fun <RI, RO> then(other: TaskBuilder<RI, RO>, mapFunction: (O) -> RI = {
        @Suppress("UNCHECKED_CAST")
        it as RI
    }): TaskBuilder<I, RO> {
        return TaskBuilder(StreamTask(currentTask, other.build(), mapFunction))
    }

    fun validateBefore(function: (I) -> Unit): TaskBuilder<I, O> {
        return this.apply { currentTask = ValidatingTask(currentTask, function) }
    }

    fun onStart(callback: () -> Unit) = this.apply { currentTask.onStart(callback) }
    fun onSuccess(callback: (O) -> Unit) = this.apply { currentTask.onSuccess(callback) }
    fun onError(callback: (Throwable) -> Unit) = this.apply { currentTask.onError(callback) }
    fun onFinish(callback: () -> Unit) = this.apply { currentTask.onFinish(callback) }
    fun onInnerStart(callback: () -> Unit) = this.apply { currentTask.onInnerStart(callback) }

    fun build(): BaseTask<I, O> {
        return currentTask
    }

}
