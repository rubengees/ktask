package com.rubengees.ktask.operation

import com.rubengees.ktask.base.BranchTask
import com.rubengees.ktask.base.Task

/**
 * Task for delivering the input back onSuccess of the passed [innerTask].
 *
 * The input is wrapped in an [Pair] together with the result. [Pair.first] is the input and [Pair.second] is the
 * result.
 * If an error occurs, the input is not passed back.
 *
 * @param I The type of input.
 * @param O The type of output.
 *
 * @author Ruben Gees
 */
class InputEchoTask<I, O>(override val innerTask: Task<I, O>) : BranchTask<I, Pair<I, O>, I, O>() {

    private var currentInput: I? = null

    init {
        restoreCallbacks(this)
    }

    override fun execute(input: I) {
        start {
            currentInput = input

            innerTask.execute(input)
        }
    }

    override fun cancel() {
        super.cancel()

        currentInput = null
    }

    override fun restoreCallbacks(from: Task<I, Pair<I, O>>) {
        super.restoreCallbacks(from)

        innerTask.onSuccess {
            val safeInput = currentInput

            if (safeInput != null) {
                finishSuccessful(safeInput to it)
            } else {
                finishWithError(IllegalStateException("currentInput is null"))
            }

            currentInput = null
        }

        innerTask.onError {
            finishWithError(it)

            currentInput = null
        }
    }
}
