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

    override fun onInnerStart(callback: () -> Unit) = this.apply { leftInnerTask.onInnerStart({ callback.invoke() }) }

    override fun cancel() {
        super.cancel()

        leftInnerTask.cancel()
        rightInnerTask.cancel()
    }

    override fun reset() {
        super.reset()

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

    /**
     * Exception which holds the cause of failure of a child of a [MultiBranchTask] and the partial result of the
     * other child.
     *
     * @param innerError The error, causing the task to fail.
     * @property partialResult The result of the other task, which completed successfully.
     *
     * @author Ruben Gees
     */
    class PartialTaskException(val innerError: Throwable, val partialResult: Any?) : Exception()

    /**
     * Exception which holds the two causes for a [MultiBranchTask] to fail.
     *
     * @param firstInnerError The first error, causing the task to fail.
     * @property secondInnerError The second error, causing the task to fail.
     *
     * @author Ruben Gees
     */
    class FullTaskException(val firstInnerError: Throwable, val secondInnerError: Throwable) : Exception()
}
