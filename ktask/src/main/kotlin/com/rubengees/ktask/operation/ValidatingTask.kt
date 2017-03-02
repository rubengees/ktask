package com.rubengees.ktask.operation

import com.rubengees.ktask.base.DelegateTask
import com.rubengees.ktask.base.Task

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class ValidatingTask<I, O>(innerTask: Task<I, O>, private val function: (I) -> Unit) :
        DelegateTask<I, O, I, O>(innerTask) {

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
