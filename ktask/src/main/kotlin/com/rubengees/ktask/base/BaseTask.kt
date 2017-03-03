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
abstract class BaseTask<I, O> : Task<I, O> {

    /**
     * The callback to be invoked when execution of the task starts.
     */
    protected var startCallback: (() -> Unit)? = null

    /**
     * The callback to be invoked when execution of the task finishes.
     */
    protected var finishCallback: (() -> Unit)? = null

    /**
     * The callback to be invoked when execution of the task finishes with an error.
     */
    protected var errorCallback: ((Throwable) -> Unit)? = null

    /**
     * The callback to be invoked when execution of the task finishes successful.
     */
    protected var successCallback: ((O) -> Unit)? = null

    override fun onStart(callback: () -> Unit) = this.apply { startCallback = callback }
    override fun onSuccess(callback: (O) -> Unit) = this.apply { successCallback = callback }
    override fun onError(callback: (Throwable) -> Unit) = this.apply { errorCallback = callback }
    override fun onFinish(callback: () -> Unit) = this.apply { finishCallback = callback }
    abstract override fun onInnerStart(callback: () -> Unit): BaseTask<I, O>

    override fun destroy() {
        startCallback = null
        successCallback = null
        errorCallback = null
        finishCallback = null
    }

    /**
     * Convenience function for inheritors to start the task. The logic is specified by the [action] parameter.
     *
     * If there is an ongoing execution, it is cancelled. Furthermore the [startCallback] is invoked.
     */
    open protected fun start(action: () -> Unit) {
        cancel()

        startCallback?.invoke()

        action.invoke()
    }

    /**
     * Convenience function for inheritors to mark the task as finished with success.
     *
     * This calls the appropriate callbacks with the [result].
     */
    open protected fun finishSuccessful(result: O) {
        successCallback?.invoke(result)
        finishCallback?.invoke()
    }

    /**
     * Convenience function for inheritors to mark the task as finished with an error.
     *
     * This calls the appropriate callbacks with the [error].
     */
    open protected fun finishWithError(error: Throwable) {
        errorCallback?.invoke(error)
        finishCallback?.invoke()
    }
}
