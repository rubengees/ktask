package com.rubengees.ktask.operation

import com.rubengees.ktask.base.DelegateBranchTask
import com.rubengees.ktask.base.Task
import com.rubengees.ktask.util.TaskException

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class StreamTask<LI, LO, RI, RO>(leftInnerTask: Task<LI, LO>, rightInnerTask: Task<RI, RO>,
                                 private val mapFunction: (LO) -> RI = {
                                     @Suppress("UNCHECKED_CAST")
                                     it as RI
                                 }) :
        DelegateBranchTask<LI, RO, LI, RI, LO, RO>(leftInnerTask, rightInnerTask) {

    init {
        leftInnerTask.onSuccess {
            try {
                rightInnerTask.execute(mapFunction.invoke(it))
            } catch(exception: Exception) {
                finishWithError(TaskException(exception))
            }
        }

        leftInnerTask.onError {
            finishWithError(it)
        }

        rightInnerTask.onSuccess {
            finishSuccessful(it)
        }

        rightInnerTask.onError {
            finishWithError(it)
        }
    }

    override fun execute(input: LI) {
        start {
            leftInnerTask.execute(input)
        }
    }
}
