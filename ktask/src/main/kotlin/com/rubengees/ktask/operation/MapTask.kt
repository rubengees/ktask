package com.rubengees.ktask.operation

import com.rubengees.ktask.base.BranchTask
import com.rubengees.ktask.base.Task

/**
 * Task for mapping the result of the [innerTask] to another type or value, specified by the [mapFunction].
 *
 * @param I The type of input.
 * @param O The type of output.
 * @param M The type of result of the [innerTask].
 *
 * @author Ruben Gees
 */
class MapTask<I, M, O>(override val innerTask: Task<I, M>, mapFunction: (M) -> O) : BranchTask<I, O, I, M>() {

    var mapFunction: ((M) -> O)? = mapFunction

    init {
        restoreCallbacks(this)
    }

    override fun execute(input: I) {
        start {
            innerTask.execute(input)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun restoreCallbacks(from: Task<I, O>) {
        super.restoreCallbacks(from)

        if (from !is MapTask<*, *, *>) {
            throw IllegalArgumentException("The passed task must have the same type.")
        }

        mapFunction = from.mapFunction as ((M) -> O)?

        innerTask.onSuccess {
            this.mapFunction?.let { function ->
                try {
                    finishSuccessful(function.invoke(it))
                } catch(error: Throwable) {
                    finishWithError(error)
                }
            }
        }

        innerTask.onError {
            finishWithError(it)
        }
    }
}
