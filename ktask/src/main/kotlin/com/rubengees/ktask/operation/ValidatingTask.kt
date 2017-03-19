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
class ValidatingTask<I, O>(override val innerTask: Task<I, O>, validationFunction: (I) -> Unit) :
        BranchTask<I, O, I, O>() {

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

    @Suppress("UNCHECKED_CAST")
    override fun restoreCallbacks(from: Task<I, O>) {
        super.restoreCallbacks(from)

        if (from !is ValidatingTask<*, *>) {
            throw IllegalArgumentException("The passed task must have the same type.")
        }

        validationFunction = from.validationFunction as ((I) -> Unit)?

        innerTask.onSuccess {
            finishSuccessful(it)
        }

        innerTask.onError {
            finishWithError(it)
        }
    }
}
