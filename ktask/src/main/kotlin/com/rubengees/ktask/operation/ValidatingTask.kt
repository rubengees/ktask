package com.rubengees.ktask.operation

import com.rubengees.ktask.base.BranchTask
import com.rubengees.ktask.base.Task

/**
 * Task for validating the input with the given [validationFunction] before actually executing.
 *
 * If the input is not valid, the [validationFunction] is expected to throw an error, which is then delivered.
 *
 * @param I The type of input.
 * @param O The type of output.
 *
 * @author Ruben Gees
 */
class ValidatingTask<I, O, T : Task<I, O, T>>(override val innerTask: T, validationFunction: (I) -> Unit) :
        BranchTask<I, O, I, O, T, ValidatingTask<I, O, T>>() {

    var validationFunction: ((I) -> Unit)? = validationFunction

    init {
        restoreCallbacks(this)
    }

    override fun execute(input: I) {
        try {
            this.validationFunction?.invoke(input)
        } catch (error: Exception) {
            finishWithError(error)

            return
        }

        start {
            innerTask.execute(input)
        }
    }

    override fun retainingDestroy() {
        super.retainingDestroy()

        validationFunction = null
    }

    override fun restoreCallbacks(from: ValidatingTask<I, O, T>) {
        super.restoreCallbacks(from)

        validationFunction = from.validationFunction

        innerTask.onSuccess {
            finishSuccessful(it)
        }

        innerTask.onError {
            finishWithError(it)
        }
    }
}
