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
abstract class BaseTask<I, O, SELF : BaseTask<I, O, SELF>> : Task<I, O, SELF>() {

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

    override fun onStart(callback: (() -> Unit)?) = me.apply { startCallback = callback }
    override fun onSuccess(callback: ((O) -> Unit)?) = me.apply { successCallback = callback }
    override fun onError(callback: ((Throwable) -> Unit)?) = me.apply { errorCallback = callback }
    override fun onFinish(callback: (() -> Unit)?) = me.apply { finishCallback = callback }

    override fun forceExecute(input: I) {
        cancel()

        execute(input)
    }

    override fun freshExecute(input: I) {
        reset()

        execute(input)
    }

    override fun retainingDestroy() {
        startCallback = null
        successCallback = null
        errorCallback = null
        finishCallback = null
    }

    override fun destroy() {
        startCallback = null
        successCallback = null
        errorCallback = null
        finishCallback = null
    }

    /**
     * Convenience function for inheritors to start the task. The logic is specified by the [action] parameter.
     *
     * The [action] is only invoked, if the task is not working (Determined by calling [isWorking]) currently.
     * Before invoking the [action], the [startCallback] is invoked.
     */
    open protected fun start(action: () -> Unit) {
        if (!isWorking) {
            startCallback?.invoke()

            action.invoke()
        }
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

    override fun restoreCallbacks(from: SELF) {
        startCallback = from.startCallback
        finishCallback = from.finishCallback
    }
}
