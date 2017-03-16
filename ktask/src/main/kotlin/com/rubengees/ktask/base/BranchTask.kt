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
abstract class BranchTask<I, O, TI, TO, T : Task<TI, TO, T>,
        SELF : BranchTask<I, O, TI, TO, T, SELF>> : BaseTask<I, O, SELF>() {

    abstract val innerTask: T

    override val isWorking: Boolean
        get() = innerTask.isWorking

    override fun onInnerStart(callback: (() -> Unit)?) = me.apply { innerTask.onInnerStart(callback) }

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

    override fun restoreCallbacks(from: SELF) {
        super.restoreCallbacks(from)

        innerTask.restoreCallbacks(from.innerTask)
    }
}
