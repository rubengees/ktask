package com.rubengees.ktask.operation

import com.rubengees.ktask.base.DelegateTask
import com.rubengees.ktask.base.Task
import com.rubengees.ktask.util.TaskException

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class MapTask<I, M, O>(innerTask: Task<I, M>, private val function: (M) -> O) :
        DelegateTask<I, O, I, M>(innerTask) {

    init {
        innerTask.onSuccess {
            try {
                finishSuccessful(function.invoke(it))
            } catch(error: Exception) {
                finishWithError(TaskException(error))
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
