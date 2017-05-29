package com.rubengees.ktask.android

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.View
import com.rubengees.ktask.base.Task
import com.rubengees.ktask.util.TaskBuilder

fun <I, O, T : Task<I, O>> TaskBuilder<I, O, T>.bindToLifecycle(
        context: FragmentActivity,
        tag: String = context.localClassName
) = TaskBuilder.task(AndroidLifecycleTask(context, build(), tag))

fun <I, O, T : Task<I, O>> TaskBuilder<I, O, T>.bindToLifecycle(
        context: Fragment, tag: String = fragmentTag(context)
) = TaskBuilder.task(AndroidLifecycleTask(context, build(), tag))

fun <I, O, T : Task<I, O>> TaskBuilder<I, O, T>.bindToLifecycle(
        context: View,
        tag: String = context.id.toString()
) = TaskBuilder.task(AndroidLifecycleTask(context, build(), tag))

private fun fragmentTag(context: Fragment) = "${context.activity.localClassName}${context.id}"
