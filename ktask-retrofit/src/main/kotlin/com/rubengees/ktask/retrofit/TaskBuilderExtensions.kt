package com.rubengees.ktask.retrofit

import com.rubengees.ktask.util.TaskBuilder
import retrofit2.Call

fun <O> TaskBuilder.Companion.retrofitTask(): TaskBuilder<Call<O>, O> {
    return TaskBuilder.task(RetrofitTask<O>())
}
