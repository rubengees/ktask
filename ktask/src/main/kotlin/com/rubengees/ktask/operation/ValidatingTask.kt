package com.rubengees.ktask.operation

import com.rubengees.ktask.base.BranchTask
import com.rubengees.ktask.base.Task

/**
 * Task for validating the input with the given [function] before actually executing.
 *
 * If the input is not valid, the [function] is expected to throw an error, which is then delivered.
 *
 * @param I The type of input.
 * @param O The type of output.
 *
 * @author Ruben Gees
 */
class ValidatingTask<I, O>(innerTask: Task<I, O>, private val function: (I) -> Unit) :
        BranchTask<I, O, I, O>(innerTask) {

    init {
        innerTask.onSuccess {
            finishSuccessful(it)
        }

        innerTask.onError {
            finishWithError(it)
        }
    }

    override fun execute(input: I) {
        try {
            function.invoke(input)
        } catch (error: Exception) {
            finishWithError(error)

            return
        }

        start {
            innerTask.execute(input)
        }
    }
}
