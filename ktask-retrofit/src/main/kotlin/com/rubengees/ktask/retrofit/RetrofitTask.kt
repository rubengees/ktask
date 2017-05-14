package com.rubengees.ktask.retrofit

import com.rubengees.ktask.base.LeafTask
import retrofit2.Call
import java.io.IOException

/**
 * TODO: Describe class
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

            try {
                val response = input.execute()

                internalCancel()

                if (response.isSuccessful) {
                    finishSuccessful(response.body())
                } else {
                    finishWithError(IOException())
                }
            } catch (error: Throwable) {
                internalCancel()

                finishWithError(error)
            }
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
}
