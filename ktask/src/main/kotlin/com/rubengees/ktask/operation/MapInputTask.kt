package com.rubengees.ktask.operation

import com.rubengees.ktask.base.BranchTask
import com.rubengees.ktask.base.Task

/**
 * Task for mapping the input to another type or value, specified by the [mapFunction].
 *
 * @param NI The new type of input.
 * @param I The type of input of the [innerTask].
 * @param O The type of output.
 *
 * @author Ruben Gees
 */
class MapInputTask<NI, I, O>(override val innerTask: Task<I, O>, mapFunction: (NI) -> I) : BranchTask<NI, O, I, O>() {

    var mapFunction: ((NI) -> I)? = mapFunction

    init {
        initCallbacks()
    }

    override fun execute(input: NI) {
        start {
            this.mapFunction?.let {
                try {
                    innerTask.execute(it.invoke(input))
                } catch (error: Throwable) {
                    finishWithError(error)
                }
            }
        }
    }

    override fun retainingDestroy() {
        super.retainingDestroy()

        mapFunction = null
    }

    @Suppress("UNCHECKED_CAST")
    override fun restoreCallbacks(from: Task<NI, O>) {
        super.restoreCallbacks(from)

        if (from !is MapInputTask<*, *, *>) {
            throw IllegalArgumentException("The passed task must have the same type.")
        }

        mapFunction = from.mapFunction as ((NI) -> I)?

        initCallbacks()
    }

    private fun initCallbacks() {
        innerTask.onSuccess {
            finishSuccessful(it)
        }

        innerTask.onError {
            finishWithError(it)
        }
    }
}
