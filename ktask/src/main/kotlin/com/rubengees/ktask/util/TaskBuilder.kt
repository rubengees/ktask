package com.rubengees.ktask.util

import com.rubengees.ktask.base.Task
import com.rubengees.ktask.operation.*
import com.rubengees.ktask.operation.CacheTask.CacheStrategy

/**
 * Utility class for constructing tasks in a fluent way.
 *
 * @author Ruben Gees
 */
class TaskBuilder<I, O, T : Task<I, O>> private constructor(private val currentTask: T) {

    companion object {

        /**
         * Creates a new [TaskBuilder] from the given task.
         */
        fun <I, O, T : Task<I, O>> task(task: T) = TaskBuilder(task)

        /**
         * Creates a new [TaskBuilder] with an [AttemptTask] and given [leftTask] and [rightTask] as root.
         */
        fun <I, O> attemptTask(leftTask: Task<I, O>, rightTask: Task<I, O>) = task(AttemptTask(leftTask, rightTask))
    }

    /**
     * Caches results of the previous tasks.
     */
    fun cache(strategy: CacheStrategy = CacheStrategy.FULL) = task(CacheTask(currentTask, strategy))

    /**
     * Modifies the result line, to also return the input of the previous task.
     */
    fun inputEcho() = task(InputEchoTask(currentTask))

    /**
     * Maps the result to a new type.
     */
    fun <M> map(function: (O) -> M) = task(MapTask(currentTask, function))

    /**
     * Maps the input to a new type.
     */
    fun <OI> mapInput(function: (OI) -> I) = TaskBuilder.task(MapInputTask(currentTask, function))

    /**
     * Runs the previous task in parallel with another given task.
     *
     * Note that this requires both tasks, to be [AsynchronousTask]s at some point.
     */
    fun <OI, OO, FO> parallelWith(
            other: Task<OI, OO>, zipFunction: (O, OO) -> FO,
            awaitLeftResultOnError: Boolean = false,
            awaitRightResultOnError: Boolean = false
    ) = task(ParallelTask(currentTask, other, zipFunction, awaitLeftResultOnError, awaitRightResultOnError))

    /**
     * Runs the previous task in parallel with another given [TaskBuilder].
     *
     * Note that this requires both tasks, to be [AsynchronousTask]s at some point.
     */
    fun <OI, OO, T : Task<OI, OO>, FO> parallelWith(
            other: TaskBuilder<OI, OO, T>, zipFunction: (O, OO) -> FO,
            awaitLeftResultOnError: Boolean = false,
            awaitRightResultOnError: Boolean = false
    ) = task(ParallelTask(currentTask, other.build(), zipFunction, awaitLeftResultOnError, awaitRightResultOnError))

    /**
     * Runs the previous task with the given in series (the previous task first).
     */
    fun <OI, OO> then(other: Task<OI, OO>, mapFunction: (O) -> OI = {
        @Suppress("UNCHECKED_CAST")
        it as OI
    }) = task(StreamTask(currentTask, other, mapFunction))


    /**
     * Runs the previous task with the given [TaskBuilder] in series (the previous task first).
     */
    fun <OI, OO, T : Task<OI, OO>> then(other: TaskBuilder<OI, OO, T>, mapFunction: (O) -> OI = {
        @Suppress("UNCHECKED_CAST")
        it as OI
    }) = task(StreamTask(currentTask, other.build(), mapFunction))

    /**
     * Validates the input before the previous task.
     */
    fun validateBefore(function: (I) -> Unit) = task(ValidatingTask(currentTask, function))

    /**
     * Makes the previous task run in a separate thread.
     */
    fun async() = task(AsynchronousTask(build()))

    /**
     * Sets a new callback on the current task, called on start.
     */
    fun onStart(callback: () -> Unit) = this.apply { currentTask.onStart(callback) }

    /**
     * Sets a new callback on the current task, called on success.
     */
    fun onSuccess(callback: (O) -> Unit) = this.apply { currentTask.onSuccess(callback) }

    /**
     * Sets a new callback on the current task, called on error.
     */
    fun onError(callback: (Throwable) -> Unit) = this.apply { currentTask.onError(callback) }

    /**
     * Sets a new callback on the current task, called on finish.
     */
    fun onFinish(callback: () -> Unit) = this.apply { currentTask.onFinish(callback) }

    /**
     * Sets a new callback on the leftmost inner [com.rubengees.ktask.base.LeafTask], called on start.
     */
    fun onInnerStart(callback: () -> Unit) = this.apply { currentTask.onInnerStart(callback) }

    /**
     * Finally constructs the usable task.
     */
    fun build(): T {
        return currentTask
    }
}
