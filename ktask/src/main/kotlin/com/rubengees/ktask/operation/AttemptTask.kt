package com.rubengees.ktask.operation

import com.rubengees.ktask.base.MultiBranchTask
import com.rubengees.ktask.base.Task

/**
 * Task which tries to execute the first task, and on failure executes the second task.
 *
 * A failure is defined as the task finishing with an error.
 *
 * @param I The type of input.
 * @param O The type of output.
 *
 * @author Ruben Gees
 */
class AttemptTask<I, O>(override val leftInnerTask: Task<I, O>,
                        override val rightInnerTask: Task<I, O>) : MultiBranchTask<I, O, I, I, O, O>() {

    private var currentInput: I? = null

    init {
        initCallbacks()
    }

    override fun execute(input: I) {
        start {
            currentInput = input

            leftInnerTask.execute(input)
        }
    }

    override fun cancel() {
        super.cancel()

        currentInput = null
    }

    override fun restoreCallbacks(from: Task<I, O>) {
        super.restoreCallbacks(from)

        initCallbacks()
    }

    private fun initCallbacks() {
        leftInnerTask.onSuccess {
            finishSuccessful(it)
        }

        leftInnerTask.onError {
            val safeInput = currentInput

            if (safeInput != null) {
                rightInnerTask.execute(safeInput)
            } else {
                finishWithError(IllegalStateException("currentInput is null"))
            }
        }

        rightInnerTask.onSuccess {
            finishSuccessful(it)
        }

        rightInnerTask.onError {
            finishWithError(it)
        }
    }
}
