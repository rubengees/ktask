package com.rubengees.ktask.base

/**
 * Base class for all tasks with some common implementation.
 *
 * It implements the `onLifecycle` callback functions and has properties for each. It also implements [destroy], which
 * resets the callbacks.
 *
 * Moreover it adds the [start], [finishSuccessful] and [finishWithError] functions, which are useful for inheritors.
 *
 * @param I The type of input.
 * @param O The type of output.
 *
 * @author Ruben Gees
 */
abstract class BaseTask<I, O> : Task<I, O>() {

    /**
     * The callback to be invoked when execution of the task starts.
     */
    protected val startCallbacks = mutableListOf<() -> Unit>()

    /**
     * The callback to be invoked when execution of the task finishes.
     */
    protected val finishCallbacks = mutableListOf<() -> Unit>()

    /**
     * The callback to be invoked when execution of the task finishes with an error.
     */
    protected val errorCallbacks = mutableListOf<(Throwable) -> Unit>()

    /**
     * The callback to be invoked when execution of the task finishes successful.
     */
    protected val successCallbacks = mutableListOf<(O) -> Unit>()

    override fun onStart(callback: () -> Unit) = this.apply { startCallbacks.add(callback) }
    override fun onSuccess(callback: (O) -> Unit) = this.apply { successCallbacks.add(callback) }
    override fun onError(callback: (Throwable) -> Unit) = this.apply { errorCallbacks.add(callback) }
    override fun onFinish(callback: () -> Unit) = this.apply { finishCallbacks.add(callback) }

    override fun forceExecute(input: I) {
        cancel()

        execute(input)
    }

    override fun freshExecute(input: I) {
        reset()

        execute(input)
    }

    override fun retainingDestroy() {
        startCallbacks.clear()
        successCallbacks.clear()
        errorCallbacks.clear()
        finishCallbacks.clear()
    }

    override fun destroy() {
        startCallbacks.clear()
        successCallbacks.clear()
        errorCallbacks.clear()
        finishCallbacks.clear()
    }

    /**
     * Convenience function for inheritors to start the task. The logic is specified by the [action] parameter.
     *
     * The [action] is only invoked, if the task is not working (Determined by calling [isWorking]) currently.
     * Before invoking the [action], the [startCallback] is invoked.
     */
    open protected fun start(action: () -> Unit) {
        if (!isWorking) {
            startCallbacks.forEach { it.invoke() }

            action.invoke()
        }
    }

    /**
     * Convenience function for inheritors to mark the task as finished with success.
     *
     * This calls the appropriate callbacks with the [result].
     */
    open protected fun finishSuccessful(result: O) {
        successCallbacks.forEach { it.invoke(result) }
        finishCallbacks.forEach { it.invoke() }
    }

    /**
     * Convenience function for inheritors to mark the task as finished with an error.
     *
     * This calls the appropriate callbacks with the [error].
     */
    open protected fun finishWithError(error: Throwable) {
        errorCallbacks.forEach { it.invoke(error) }
        finishCallbacks.forEach { it.invoke() }
    }

    override fun restoreCallbacks(from: Task<I, O>) {
        if (from !is BaseTask) {
            throw IllegalArgumentException("The passed task must have the same type.")
        }

        startCallbacks.addAll(from.startCallbacks)
        finishCallbacks.addAll(from.finishCallbacks)
    }
}
