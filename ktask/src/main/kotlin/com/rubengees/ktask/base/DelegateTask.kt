package com.rubengees.ktask.base

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
abstract class DelegateTask<I, O, TI, TO>(val innerTask: Task<TI, TO>) : BaseTask<I, O>() {

    override val isWorking: Boolean
        get() = innerTask.isWorking

    override fun cancel() {
        innerTask.cancel()
    }

    override fun reset() {
        innerTask.reset()
    }

    override fun destroy() {
        innerTask.destroy()

        super.destroy()
    }
}
