package com.rubengees.ktask.operation

import com.rubengees.ktask.base.BranchTask
import com.rubengees.ktask.base.Task

/**
 * Task for running the passed [innerTask] asynchronously in it's own thread.
 *
 * Note that a raw thread is used for this, so running many tasks simultaneously can lead to slow execution of
 * individual tasks.
 *
 * @param I The type of input.
 * @param O The type of output.
 *
 * @author Ruben Gees
 */
class AsynchronousTask<I, O>(override val innerTask: Task<I, O>) : BranchTask<I, O, I, O>() {

    private var thread: Thread? = null

    override val isWorking: Boolean
        get() = innerTask.isWorking

    init {
        initCallbacks()
    }

    override fun execute(input: I) {
        start {
            thread = Thread({
                innerTask.execute(input)
            }).apply { start() }
        }
    }

    override fun cancel() {
        super.cancel()

        internalCancel()
    }

    override fun restoreCallbacks(from: Task<I, O>) {
        super.restoreCallbacks(from)

        initCallbacks()
    }

    private fun initCallbacks() {
        innerTask.onSuccess {
            internalCancel()

            finishSuccessful(it)
        }

        innerTask.onError {
            internalCancel()

            finishWithError(it)
        }
    }

    private fun internalCancel() {
        innerTask.cancel()
        thread?.interrupt()
        thread = null
    }
}
