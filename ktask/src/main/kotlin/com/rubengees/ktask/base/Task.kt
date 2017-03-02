package com.rubengees.ktask.base

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
interface Task<I, O> {

    val isWorking: Boolean

    fun execute(input: I)

    fun cancel()
    fun reset()
    fun destroy()

    fun onStart(callback: () -> Unit): Task<I, O>
    fun onSuccess(callback: (O) -> Unit): Task<I, O>
    fun onError(callback: (Throwable) -> Unit): Task<I, O>
    fun onFinish(callback: () -> Unit): Task<I, O>
}