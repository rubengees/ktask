package com.rubengees.ktask.operation

import com.rubengees.ktask.base.DelegateTask
import com.rubengees.ktask.base.Task
import com.rubengees.ktask.operation.CacheTask.CacheStrategy

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class CacheTask<I, O>(innerTask: Task<I, O>, cacheStrategy: CacheStrategy = CacheStrategy.FULL) :
        DelegateTask<I, O, I, O>(innerTask) {

    private val shouldCachedResult = when (cacheStrategy) {
        CacheStrategy.FULL, CacheStrategy.RESULT -> true
        else -> false
    }

    private val shouldCacheException = when (cacheStrategy) {
        CacheStrategy.FULL, CacheStrategy.EXCEPTION -> true
        else -> false
    }

    var cachedResult: O? = null
        private set
    var cachedError: Throwable? = null
        private set

    init {
        innerTask.onSuccess {
            cachedResult = if (shouldCachedResult) it else null

            finishSuccessful(it)
        }

        innerTask.onError {
            cachedError = if (shouldCacheException) it else null

            finishWithError(it)
        }
    }

    override fun execute(input: I) {
        cachedResult?.let {
            finishSuccessful(it)

            return
        }

        cachedError?.let {
            finishWithError(it)

            return
        }

        if (isWorking) {
            return
        }

        start {
            innerTask.execute(input)
        }
    }

    override fun reset() {
        super.reset()

        cachedResult = null
        cachedError = null
    }

    override fun destroy() {
        super.destroy()

        cachedResult = null
        cachedError = null
    }

    fun mutate(operation: (O?) -> O?) {
        cachedResult = operation.invoke(cachedResult)
    }

    fun clear(strategy: CacheStrategy) = when (strategy) {
        CacheStrategy.FULL -> {
            cachedResult = null
            cachedError = null
        }
        CacheStrategy.RESULT -> cachedResult = null
        CacheStrategy.EXCEPTION -> cachedError = null
    }

    enum class CacheStrategy {FULL, RESULT, EXCEPTION }
}