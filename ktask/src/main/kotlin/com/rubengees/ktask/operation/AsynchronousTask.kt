package com.rubengees.ktask.operation

import com.rubengees.ktask.base.BranchTask
import com.rubengees.ktask.base.Task

/**
 * TODO: Describe class
 *
 * @author Ruben Gees.
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
