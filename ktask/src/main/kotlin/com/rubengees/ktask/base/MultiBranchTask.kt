package com.rubengees.ktask.base

/**
 * Base class for task with two child tasks: [leftInnerTask] and [rightInnerTask].
 *
 * The property [isWorking] and the functions [onInnerStart], [cancel], [reset] and [destroy] have a default
 * implementation in which they simply delegate to the [leftInnerTask] and [rightInnerTask]. The [onInnerStart] function
 * is special in this regard, as it only registers a callback on the [leftInnerTask]. This prevents onStart getting
 * called multiple times.
 *
 * @param I The type of input.
 * @param O The type of output.
 * @param LI The type of input of the [leftInnerTask].
 * @param RI The type of input of the [rightInnerTask].
 * @param LO The type of output of the [leftInnerTask].
 * @param RO The type of output of the [rightInnerTask].
 *
 * @property leftInnerTask The left child task.
 * @property rightInnerTask The right child task.
 *
 * @author Ruben Gees
 */
abstract class MultiBranchTask<I, O, LI, RI, LO, RO> : BaseTask<I, O>() {

    abstract val leftInnerTask: Task<LI, LO>
    abstract val rightInnerTask: Task<RI, RO>

    override val isWorking: Boolean
        get() = leftInnerTask.isWorking || rightInnerTask.isWorking

    override fun onInnerStart(callback: () -> Unit) = this.apply { leftInnerTask.onInnerStart(callback) }

    override fun cancel() {
        leftInnerTask.cancel()
        rightInnerTask.cancel()
    }

    override fun reset() {
        leftInnerTask.reset()
        rightInnerTask.reset()
    }

    override fun retainingDestroy() {
        super.retainingDestroy()

        leftInnerTask.retainingDestroy()
        rightInnerTask.retainingDestroy()
    }

    override fun destroy() {
        super.destroy()

        leftInnerTask.destroy()
        rightInnerTask.destroy()
    }

    @Suppress("UNCHECKED_CAST")
    override fun restoreCallbacks(from: Task<I, O>) {
        super.restoreCallbacks(from)

        if (from !is MultiBranchTask<*, *, *, *, *, *>) {
            throw IllegalArgumentException("The passed task must have the same type.")
        }

        leftInnerTask.restoreCallbacks(from.leftInnerTask as Task<LI, LO>)
        rightInnerTask.restoreCallbacks(from.rightInnerTask as Task<RI, RO>)
    }
}
