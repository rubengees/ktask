package com.rubengees.ktask.base

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
abstract class BaseTask<I, O> : Task<I, O> {

    private var startCallback: (() -> Unit)? = null
    private var finishCallback: (() -> Unit)? = null
    private var errorCallback: ((Throwable) -> Unit)? = null
    private var successCallback: ((O) -> Unit)? = null

    override fun onStart(callback: () -> Unit): Task<I, O> {
        return this.apply { startCallback = callback }
    }

    override fun onSuccess(callback: (O) -> Unit): Task<I, O> {
        return this.apply { successCallback = callback }
    }

    override fun onError(callback: (Throwable) -> Unit): Task<I, O> {
        return this.apply { errorCallback = callback }
    }

    override fun onFinish(callback: () -> Unit): Task<I, O> {
        return this.apply { finishCallback = callback }
    }

    override fun destroy() {
        startCallback = null
        successCallback = null
        errorCallback = null
        finishCallback = null
    }

    open protected fun start(action: () -> Unit) {
        cancel()

        startCallback?.invoke()

        action.invoke()
    }

    open protected fun finishSuccessful(result: O) {
        successCallback?.invoke(result)
        finishCallback?.invoke()
    }

    open protected fun finishWithError(result: Throwable) {
        errorCallback?.invoke(result)
        finishCallback?.invoke()
    }
}
