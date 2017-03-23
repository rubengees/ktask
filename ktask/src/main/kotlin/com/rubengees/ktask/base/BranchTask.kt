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
abstract class BranchTask<I, O, TI, TO> : BaseTask<I, O>() {

    abstract val innerTask: Task<TI, TO>

    override val isWorking: Boolean
        get() = innerTask.isWorking

    override fun onInnerStart(callback: () -> Unit) = this.apply { innerTask.onInnerStart(callback) }

    override fun cancel() {
        innerTask.cancel()
    }

    override fun reset() {
        innerTask.reset()
    }

    override fun retainingDestroy() {
        super.retainingDestroy()

        innerTask.retainingDestroy()
    }

    override fun destroy() {
        super.destroy()

        innerTask.destroy()
    }

    @Suppress("UNCHECKED_CAST")
    override fun restoreCallbacks(from: Task<I, O>) {
        super.restoreCallbacks(from)

        if (from !is BranchTask<*, *, *, *>) {
            throw IllegalArgumentException("The passed task must have the same type.")
        }

        innerTask.restoreCallbacks(from.innerTask as Task<TI, TO>)
    }
}
