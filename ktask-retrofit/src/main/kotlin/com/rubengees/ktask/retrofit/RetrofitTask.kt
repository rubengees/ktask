package com.rubengees.ktask.retrofit

import com.rubengees.ktask.base.LeafTask
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class RetrofitTask<O> : LeafTask<Call<O>, O, RetrofitTask<O>>() {

    override val isWorking: Boolean
        get() = call != null

    private var call: Call<O>? = null

    override fun execute(input: Call<O>) {
        start {
            call = input

            input.enqueue(object : Callback<O> {
                override fun onResponse(call: Call<O>, response: Response<O>) {
                    cancel()

                    if (response.isSuccessful) {
                        finishSuccessful(response.body())
                    } else {
                        finishWithError(IOException())
                    }
                }

                override fun onFailure(call: Call<O>, error: Throwable) {
                    cancel()

                    finishWithError(error as Exception)
                }
            })
        }
    }

    override fun cancel() {
        super.cancel()

        call?.cancel()
        call = null
    }
}
