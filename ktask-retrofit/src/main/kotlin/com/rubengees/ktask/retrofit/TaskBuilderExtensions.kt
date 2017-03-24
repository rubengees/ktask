package com.rubengees.ktask.retrofit

import com.rubengees.ktask.util.TaskBuilder

fun <O> TaskBuilder.Companion.retrofitTask() = TaskBuilder.task(RetrofitTask<O>())
fun <O> TaskBuilder.Companion.asyncRetrofitTask() = TaskBuilder.task(RetrofitTask<O>()).async()
