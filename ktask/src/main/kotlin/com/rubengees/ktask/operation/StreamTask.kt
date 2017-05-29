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
class StreamTask<LI, LO, RI, RO>(override val leftInnerTask: Task<LI, LO>,
                                 override val rightInnerTask: Task<RI, RO>,
                                 mapFunction: (LO) -> RI = {
                                     @Suppress("UNCHECKED_CAST")
                                     it as RI
                                 }) : MultiBranchTask<LI, RO, LI, RI, LO, RO>() {

    private var mapFunction: ((LO) -> RI)? = mapFunction

    init {
        initCallbacks()
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

    @Suppress("UNCHECKED_CAST")
    override fun restoreCallbacks(from: Task<LI, RO>) {
        super.restoreCallbacks(from)

        if (from !is StreamTask<*, *, *, *>) {
            throw IllegalArgumentException("The passed task must have the same type.")
        }

        mapFunction = from.mapFunction as ((LO) -> RI)?

        initCallbacks()
    }

    private fun initCallbacks() {
        leftInnerTask.onSuccess {
            this.mapFunction?.let { function ->
                val mappedInput = try {
                    function.invoke(it)
                } catch(exception: Exception) {
                    finishWithError(exception)

                    return@let
                }

                rightInnerTask.execute(mappedInput)
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
