package com.rubengees.ktask.base

/**
 * Base class for all tasks with some common implementation.
 *
 * It implements the `onLifecycle` callback functions and has properties for each. It also implements [destroy], which
 * resets the callbacks.
 *
 * Moreover it adds the [start], [finishSuccessful] and [finishWithError] functions, which are useful for inheritors.
 *
 * This task also handles basic cancellation, as it does not deliver results if the [isCancelled] flag is set.
 *
 * @param I The type of input.
 * @param O The type of output.
 *
 * @author Ruben Gees
 */
abstract class BaseTask<I, O> : Task<I, O> {

    @Volatile
    protected var isCancelled = false

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

    override fun cancel() {
        isCancelled = true
    }

    override fun reset() {

    }

    override fun retainingDestroy() {
        internalDestroy()
    }

    override fun destroy() {
        internalDestroy()
    }

    /**
     * Convenience function for inheritors to start the task. The logic is specified by the [action] parameter.
     *
     * The [action] is only invoked, if the task is not working (Determined by calling [isWorking]) currently.
     * Before invoking the [action], the [startCallbacks] are invoked.
     *
     * Moreover, [isCancelled] is set back to false.
     */
    open protected fun start(action: () -> Unit) {
        if (!isWorking) {
            isCancelled = false
            startCallbacks.forEach { it.invoke() }

            action.invoke()
        }
    }

    /**
     * Convenience function for inheritors to mark the task as finished with success.
     *
     * This calls the appropriate callbacks with the [result] if the task is not cancelled yet.
     */
    open protected fun finishSuccessful(result: O) {
        if (!isCancelled) {
            successCallbacks.forEach { it.invoke(result) }
        }

        finishCallbacks.forEach { it.invoke() }
    }

    /**
     * Convenience function for inheritors to mark the task as finished with an error.
     *
     * This calls the appropriate callbacks with the [error] if the task is not cancelled yet.
     */
    open protected fun finishWithError(error: Throwable) {
        if (!isCancelled) {
            errorCallbacks.forEach { it.invoke(error) }
        }

        finishCallbacks.forEach { it.invoke() }
    }

    override fun restoreCallbacks(from: Task<I, O>) {
        if (from !is BaseTask) {
            throw IllegalArgumentException("The passed task must have the same type.")
        }

        startCallbacks.addAll(from.startCallbacks)
        finishCallbacks.addAll(from.finishCallbacks)
    }

    private fun internalDestroy() {
        startCallbacks.clear()
        successCallbacks.clear()
        errorCallbacks.clear()
        finishCallbacks.clear()
    }
}
