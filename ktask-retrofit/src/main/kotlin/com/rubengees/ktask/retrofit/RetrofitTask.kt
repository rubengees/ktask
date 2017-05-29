package com.rubengees.ktask.retrofit

import com.rubengees.ktask.base.LeafTask
import retrofit2.Call

/**
 * Task for safely running Retrofit calls inside the task framework.
 *
 * @author Ruben Gees
 */
class RetrofitTask<O> : LeafTask<Call<O>, O>() {

    override val isWorking: Boolean
        get() = call != null

    private var call: Call<O>? = null

    override fun execute(input: Call<O>) {
        start {
            call = input

            val response = try {
                val result = input.execute()

                when (result.isSuccessful) {
                    true -> result
                    false -> throw RetrofitTaskException(result.code(), result.message())
                }
            } catch (error: Throwable) {
                internalCancel()
                finishWithError(error)

                return@start
            }

            internalCancel()
            finishSuccessful(response.body() as O)
        }
    }

    override fun cancel() {
        super.cancel()

        internalCancel()
    }

    private fun internalCancel() {
        call?.cancel()
        call = null
    }

    class RetrofitTaskException(val code: Int, message: String?) : Exception(message)
}
