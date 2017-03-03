package com.rubengees.ktask.operation

import com.rubengees.ktask.base.MultiBranchTask
import com.rubengees.ktask.base.Task

/**
 * Task for running the passed [leftInnerTask] and [rightInnerTask] in series.
 *
 * The [mapFunction] is invoked to map the output of the [leftInnerTask] to the input of the [rightInnerTask]. it is a
 * default implementation provided, which just casts the output to the input. You have to provide your own function,
 * if this is not sufficient.
 *
 * @LI The type of input of the left task.
 * @LO The type of output of the left task.
 * @RI The type of input of the right task.
 * @RO The type of input of the right task.
 *
 * @author Ruben Gees
 */
class StreamTask<LI, LO, RI, RO>(leftInnerTask: Task<LI, LO>, rightInnerTask: Task<RI, RO>,
                                 private val mapFunction: (LO) -> RI = {
                                     @Suppress("UNCHECKED_CAST")
                                     it as RI
                                 }) : MultiBranchTask<LI, RO, LI, RI, LO, RO>(leftInnerTask, rightInnerTask) {

    init {
        leftInnerTask.onSuccess {
            try {
                rightInnerTask.execute(mapFunction.invoke(it))
            } catch(exception: Exception) {
                finishWithError(exception)
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
