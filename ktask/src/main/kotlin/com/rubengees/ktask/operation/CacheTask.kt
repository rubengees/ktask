package com.rubengees.ktask.operation

import com.rubengees.ktask.base.BranchTask
import com.rubengees.ktask.base.Task
import com.rubengees.ktask.operation.CacheTask.CacheStrategy

/**
 * Task which caches the result of the passed [innerTask].
 *
 * Note, that the cache does not cache based on the input, but always caches the result (or error) of the last execution
 * in which no cached result was present.
 *
 * To control what to cache, the cacheStrategy can be used. It can either be specified to cache only the result, only
 * the error or both. The default is both.
 *
 * To use the task with another input when a result (or error) has already been cached, call the [reset] function.
 *
 * Unlike other tasks, a current execution is not canceled, if [execute] is invoked again.
 *
 * @param I The type of input.
 * @param O The type of output.
 *
 * @author Ruben Gees
 */
class CacheTask<I, O, T : Task<I, O, T>>(override val innerTask: T, cacheStrategy: CacheStrategy = CacheStrategy.FULL) :
        BranchTask<I, O, I, O, T, CacheTask<I, O, T>>() {

    private val shouldCacheResult = when (cacheStrategy) {
        CacheStrategy.FULL, CacheStrategy.RESULT -> true
        else -> false
    }

    private val shouldCacheException = when (cacheStrategy) {
        CacheStrategy.FULL, CacheStrategy.ERROR -> true
        else -> false
    }

    /**
     * The currently cached result.
     */
    var cachedResult: O? = null
        private set

    /**
     * The currently cached error.
     */
    var cachedError: Throwable? = null
        private set

    init {
        restoreCallbacks(this)
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

        start {
            innerTask.execute(input)
        }
    }

    override fun reset() {
        super.reset()

        cachedResult = null
        cachedError = null
    }

    override fun restoreCallbacks(from: CacheTask<I, O, T>) {
        super.restoreCallbacks(from)

        innerTask.onSuccess {
            cachedResult = if (shouldCacheResult) it else null

            finishSuccessful(it)
        }

        innerTask.onError {
            cachedError = if (shouldCacheException) it else null

            finishWithError(it)
        }
    }

    /**
     * Allows to mutate the cached result with the given [operation]. It is possible to delete the result from the
     * [operation].
     */
    fun mutate(operation: (O?) -> O?) {
        cachedResult = operation.invoke(cachedResult)
    }

    /**
     * Allows to mutate the cached error with the given [operation]. It is possible to delete the error from the
     * [operation].
     */
    fun mutateError(operation: (Throwable?) -> Throwable?) {
        cachedError = operation.invoke(cachedError)
    }

    /**
     * Clears the cache. The [strategy] specifies which parts. The default is everything.
     */
    fun clear(strategy: CacheStrategy = CacheStrategy.FULL) = when (strategy) {
        CacheStrategy.FULL -> {
            cachedResult = null
            cachedError = null
        }
        CacheStrategy.RESULT -> cachedResult = null
        CacheStrategy.ERROR -> cachedError = null
    }

    /**
     * Enum representing the possible strategies for caching.
     */
    enum class CacheStrategy {

        /**
         * Everything shall be cached.
         */
        FULL,

        /**
         * Only results shall be cached.
         */
        RESULT,

        /**
         * Only errors shall be cached
         */
        ERROR
    }
}
