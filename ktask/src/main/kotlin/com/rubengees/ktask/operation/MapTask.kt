package com.rubengees.ktask.operation

import com.rubengees.ktask.base.BranchTask
import com.rubengees.ktask.base.Task

/**
 * Task for mapping the result of the [innerTask] to another type or value, specified by the [function].
 *
 * @param I The type of input.
 * @param O The type of output.
 * @param M The type of result of the [innerTask].
 *
 * @author Ruben Gees
 */
class MapTask<I, M, O>(innerTask: Task<I, M>, private val function: (M) -> O) :
        BranchTask<I, O, I, M>(innerTask) {

    init {
        innerTask.onSuccess {
            try {
                finishSuccessful(function.invoke(it))
            } catch(error: Throwable) {
                finishWithError(error)
            }
        }

        innerTask.onError {
            finishWithError(it)
        }
    }

    override fun execute(input: I) {
        start {
            innerTask.execute(input)
        }
    }
}
