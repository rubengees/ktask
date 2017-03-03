package com.rubengees.ktask.base

/**
 * Base class for tasks with one child task: [innerTask].
 *
 * The property [isWorking] and the functions [onInnerStart], [cancel], [reset] and [destroy] have a default
 * implementation in which they simply delegate to the [innerTask].
 *
 * @param I The type of input.
 * @param O The type of output.
 * @param TI The type of input of the [innerTask].
 * @param TO The type of output of the [innerTask].
 *
 * @property innerTask The child task.
 *
 * @author Ruben Gees
 */
abstract class BranchTask<I, O, TI, TO>(val innerTask: Task<TI, TO>) : BaseTask<I, O>() {

    override val isWorking: Boolean
        get() = innerTask.isWorking

    override fun onStart(callback: (() -> Unit)?) = this.apply { super.onStart(callback) }
    override fun onSuccess(callback: ((O) -> Unit)?) = this.apply { super.onSuccess(callback) }
    override fun onError(callback: ((Throwable) -> Unit)?) = this.apply { super.onError(callback) }
    override fun onFinish(callback: (() -> Unit)?) = this.apply { super.onFinish(callback) }
    override fun onInnerStart(callback: (() -> Unit)?) = this.apply { innerTask.onInnerStart(callback) }

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
