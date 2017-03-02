package com.rubengees.ktask.operation

import com.rubengees.ktask.base.DelegateTask
import com.rubengees.ktask.base.Task

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class InputEchoTask<I, O>(innerTask: Task<I, O>) : DelegateTask<I, Pair<I, O>, I, O>(innerTask) {

    private var currentInput: I? = null

    init {
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

    override fun reset() {
        super.reset()

        currentInput = null
    }

    override fun destroy() {
        super.destroy()

        currentInput = null
    }
}