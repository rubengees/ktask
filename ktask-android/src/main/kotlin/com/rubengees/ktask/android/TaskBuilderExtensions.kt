package com.rubengees.ktask.android

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.View
import com.rubengees.ktask.util.TaskBuilder

fun <I, O> TaskBuilder<I, O>.bindToLifecycle(context: FragmentActivity, tag: String = context.localClassName):
        TaskBuilder<I, O> {
    return TaskBuilder.task(AndroidLifecycleTask(context, build(), tag))
}

fun <I, O> TaskBuilder<I, O>.bindToLifecycle(context: Fragment,
                                             tag: String = "${context.activity.localClassName}${context.id}"):
        TaskBuilder<I, O> {
    return TaskBuilder.task(AndroidLifecycleTask(context, build(), tag))
}

fun <I, O> TaskBuilder<I, O>.bindToLifecycle(context: View, tag: String = context.id.toString()):
        TaskBuilder<I, O> {
    return TaskBuilder.task(AndroidLifecycleTask(context, build(), tag))
}
