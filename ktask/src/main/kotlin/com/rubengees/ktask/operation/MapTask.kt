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
class MapTask<I, M, O, T : Task<I, M, T>>(override val innerTask: T, mapFunction: (M) -> O) :
        BranchTask<I, O, I, M, T, MapTask<I, M, O, T>>() {

    var mapFunction: ((M) -> O)? = mapFunction

    init {
        restoreCallbacks(this)
    }

    override fun execute(input: I) {
        start {
            innerTask.execute(input)
        }
    }

    override fun restoreCallbacks(from: MapTask<I, M, O, T>) {
        super.restoreCallbacks(from)

        mapFunction = from.mapFunction

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
