package com.rubengees.ktask.base

/**
 * Base interface for all tasks.
 *
 * Tasks are structured in a tree, which defines altogether the action to take.
 * The [BranchTask] allows to pass a single task, which is then modified or a specific action is performed in the
 * lifecycle.
 * The [MultiBranchTask] allows to pass two tasks. Subclasses can then be used for modifying the execution flow. One
 * example is the [com.rubengees.ktask.operation.StreamTask], which simply runs the passed tasks in order.
 * All tasks run synchronous, apart from the [com.rubengees.ktask.operation.AsynchronousTask], which allows for tasks
 * to run in their own thread.
 *
 * @param I The type of input.
 * @param O The type of output.
 *
 * @author Ruben Gees
 */
interface Task<I, O> {

    /**
     * Property which returns true, if the current task is working.
     */
    val isWorking: Boolean

    /**
     * Executes the task with the given [input] if no execution is running already.
     *
     * The lifecycle callbacks are defined to be invoked in the following order:
     * - onStart
     * - onSuccess or onError
     * - onFinish
     */
    fun execute(input: I)

    /**
     *  Executes the task with the given [input]. If an execution is ongoing already, it is cancelled.
     *
     *  This has the same effect as calling
     *  ```
     *  task.cancel()
     *  task.execute(input)
     *  ```
     *
     *  @see [execute]
     */
    fun forceExecute(input: I)

    /**
     *  Executes the task with the given [input]. The task is reset before executing.
     *
     *  This has the same effect as calling
     *  ```
     *  task.reset()
     *  task.execute(input)
     *  ```
     *
     *  @see [execute]
     */
    fun freshExecute(input: I)

    /**
     * Serially executes the task and returns the result immediately. The task is cancelled before executing.
     *
     * This method is always blocking, even if it contains an [com.rubengees.ktask.operation.AsynchronousTask].
     * All callbacks are still called, but it is recommended to stick to normal control flow with this method.
     */
    fun serialExecute(input: I): O

    /**
     * Cancels the task and its children if present and deletes any data, directly associated with the current
     * execution.
     *
     * [isWorking] should return false immediately after calling this function.
     *
     * Cancel can be invoked multiple times on a task, even if it currently not executing. In that case, nothing
     * happens.
     *
     * Cancel does not necessarily guarantee the task to stop immediately, since the [LeafTask]s are responsible for
     * cancelling, which are client code.
     */
    fun cancel()

    /**
     * Resets the task. This means deleting all data, not directly associated with a single execution.
     *
     * Implicitly cancels the task.
     */
    fun reset()

    /**
     * Destroys all callbacks and functions of the task, but keeps local data and the current execution.
     */
    fun retainingDestroy()

    /**
     * Destroys the task. This means deleting all data and callbacks.
     *
     * Implicitly resets the task.
     */
    fun destroy()

    /**
     * Assigns the [callback] to be called when the task is started. Unlike [onInnerStart], the callback is exactly set
     * on this task, which means that upon an invocation of the callback, child tasks not have started yet.
     *
     * @return This task.
     */
    fun onStart(callback: () -> Unit): Task<I, O>

    /**
     * Assigns the [callback] to be called when the task executed successfully.
     *
     * @return This task.
     */
    fun onSuccess(callback: (O) -> Unit): Task<I, O>

    /**
     * Assigns the [callback] to be called when the task failed with an error.
     *
     * @return This task.
     */
    fun onError(callback: (Throwable) -> Unit): Task<I, O>

    /**
     * Assigns the [callback] to be called when the task finished. This means, that it either executed successfully or
     * failed with an error.
     *
     * The callback is always invoked after [onSuccess] and [onError].
     *
     * @return This task.
     */
    fun onFinish(callback: () -> Unit): Task<I, O>

    /**
     * Assigns the [callback] to be called when the first [LeafTask] is started. This is the leftmost leaf in the tree
     * of the task. Unlike [onStart], no callback is set on this task if it does not match the above restrictions.
     *
     * @return This task.
     */
    fun onInnerStart(callback: () -> Unit): Task<I, O>

    /**
     * Only for internal use.
     *
     * Copies functions from the given [from] task to this one and re-applies the internal callbacks. The type of the
     * passed has to be checked.
     * This is useful, when [retainingDestroy] has been called, and this task should be re-initialized.
     */
    fun restoreCallbacks(from: Task<I, O>)
}
