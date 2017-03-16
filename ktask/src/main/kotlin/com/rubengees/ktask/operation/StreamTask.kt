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
class StreamTask<LI, LO, RI, RO, LT : Task<LI, LO, LT>, RT : Task<RI, RO, RT>>(override val leftInnerTask: LT,
                                                                               override val rightInnerTask: RT,
                                                                               mapFunction: (LO) -> RI = {
                                                                                   @Suppress("UNCHECKED_CAST")
                                                                                   it as RI
                                                                               }) :
        MultiBranchTask<LI, RO, LI, RI, LO, RO, LT, RT, StreamTask<LI, LO, RI, RO, LT, RT>>() {

    private var mapFunction: ((LO) -> RI)? = mapFunction

    init {
        restoreCallbacks(this)
    }

    override fun execute(input: LI) {
        start {
            leftInnerTask.execute(input)
        }
    }

    override fun retainingDestroy() {
        super.retainingDestroy()

        mapFunction = null
    }

    override fun restoreCallbacks(from: StreamTask<LI, LO, RI, RO, LT, RT>) {
        super.restoreCallbacks(from)

        mapFunction = from.mapFunction

        leftInnerTask.onSuccess {
            this.mapFunction?.let { function ->
                try {
                    rightInnerTask.execute(function.invoke(it))
                } catch(exception: Exception) {
                    finishWithError(exception)
                }
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
}
