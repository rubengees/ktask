package com.rubengees.ktask.util

import com.rubengees.ktask.base.LeafTask

/**
 * Simple task to override by users for performing a single unit of work.
 *
 * Note that you must handle cancellation yourself. To do so, check the [isCancelled] flag as often as possible and
 * throw an [InterruptedException] if set.
 *
 * @author Ruben Gees.
 */
abstract class WorkerTask<I, O> : LeafTask<I, O>() {

    @Volatile
    override var isWorking = false

    override fun execute(input: I) {
        start {
            isWorking = true

            try {
                finishSuccessful(work(input))
            } catch(error: Throwable) {
                finishWithError(error)
            }
        }
    }

    override fun finishSuccessful(result: O) {
        isWorking = false

        super.finishSuccessful(result)
    }

    override fun finishWithError(error: Throwable) {
        isWorking = false

        super.finishWithError(error)
    }

    override fun cancel() {
        super.cancel()

        isWorking = false
    }

    abstract fun work(input: I): O
}
