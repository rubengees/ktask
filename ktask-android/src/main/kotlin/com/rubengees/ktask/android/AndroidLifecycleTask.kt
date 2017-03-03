package com.rubengees.ktask.android

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ContextWrapper
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.view.View
import com.rubengees.ktask.base.BaseTask
import com.rubengees.ktask.base.BranchTask
import com.rubengees.ktask.base.MultiBranchTask
import com.rubengees.ktask.base.Task

/**
 * Task for working in a safe manner with the Android Lifecycle.
 *
 * Tasks continue to work, if an orientation change occurs. They are also safely delivered. If the Activity is not
 * resumed or the fragment is not attached, no results are delivered. Moreover it destroys itself automatically, if the
 * Activity finishes. A call to [cancel] in onDestroy is not necessary.
 *
 * Unlike other tasks, a current execution is not canceled, if [execute] is invoked again. This is to make the API more
 * fluent.
 *
 * @param I The type of input.
 * @param O The type of output.
 *
 * @author Ruben Gees
 */
class AndroidLifecycleTask<I, O> : BaseTask<I, O> {

    override val isWorking: Boolean
        get() = innerTask.isWorking

    val innerTask: Task<I, O>
        get() = workerFragment.innerTask

    private val workerFragment: RetainedWorkerFragment<I, O>
    private var context: Activity?

    private companion object {
        private fun findActivityForView(view: View): FragmentActivity {
            var context = view.context

            while (context is ContextWrapper) {
                if (context is FragmentActivity) {
                    return context
                }

                context = context.baseContext
            }

            throw IllegalArgumentException("The passed View must reside in a FragmentActivity (or AppcompatActivity)")
        }
    }

    @SuppressLint("CommitTransaction")
    constructor(context: FragmentActivity, innerTask: BaseTask<I, O>, tag: String) {
        this.context = context

        val existingWorker = context.supportFragmentManager.findFragmentByTag(tag)

        @Suppress("UNCHECKED_CAST")
        if (existingWorker is RetainedWorkerFragment<*, *>) {
            workerFragment = existingWorker as RetainedWorkerFragment<I, O>

            copyCallbacks(innerTask, workerFragment.innerTask)
        } else {
            workerFragment = RetainedWorkerFragment(innerTask).apply {
                context.supportFragmentManager.beginTransaction().add(this, tag).commitNow()
            }
        }

        val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}

            override fun onActivityDestroyed(activity: Activity) {
                if (activity === context) {
                    if (activity.isChangingConfigurations) {
                        retainingDestroy()
                    } else {
                        destroy()
                    }

                    activity.application.unregisterActivityLifecycleCallbacks(this)
                }
            }
        }

        context.application.registerActivityLifecycleCallbacks(lifecycleCallbacks)

        workerFragment.innerTask.onSuccess {
            if (!context.isFinishing) {
                finishSuccessful(it)
            }
        }

        workerFragment.innerTask.onError {
            if (!context.isFinishing) {
                finishWithError(it)
            }
        }
    }

    constructor(context: Fragment, innerTask: BaseTask<I, O>, tag: String) {
        this.context = context.activity

        val existingWorker = context.childFragmentManager.findFragmentByTag(tag)

        @Suppress("UNCHECKED_CAST")
        if (existingWorker is RetainedWorkerFragment<*, *>) {
            workerFragment = existingWorker as RetainedWorkerFragment<I, O>

            copyCallbacks(innerTask, workerFragment.innerTask)
        } else {
            workerFragment = RetainedWorkerFragment(innerTask).apply {
                context.childFragmentManager.beginTransaction().add(this, tag).commitNow()
            }
        }

        val lifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fragmentManager: FragmentManager, fragment: Fragment) {
                if (fragment === context) {
                    if (fragment.activity.isChangingConfigurations) {
                        retainingDestroy()
                    } else {
                        destroy()
                    }

                    fragment.isAdded

                    fragmentManager.unregisterFragmentLifecycleCallbacks(this)
                }
            }
        }

        context.activity.supportFragmentManager.registerFragmentLifecycleCallbacks(lifecycleCallbacks, true)

        workerFragment.innerTask.onSuccess {
            if (context.isAdded) {
                finishSuccessful(it)
            }
        }

        workerFragment.innerTask.onError {
            if (context.isAdded) {
                finishWithError(it)
            }
        }
    }

    constructor(context: View, innerTask: BaseTask<I, O>, tag: String) :
            this(findActivityForView(context), innerTask, tag)

    override fun onInnerStart(callback: (() -> Unit)?) = this.apply { innerTask.onInnerStart(callback) }

    override fun execute(input: I) {
        startCallback?.invoke()

        innerTask.execute(input)
    }

    override fun cancel() {
        innerTask.cancel()
    }

    override fun reset() {
        innerTask.reset()
    }

    override fun destroy() {
        innerTask.destroy()
        context = null

        super.destroy()
    }

    fun retainingDestroy() {
        context = null

        super.destroy()
    }

    private fun copyCallbacks(newTask: BaseTask<*, *>, existingTask: BaseTask<*, *>) {
        if (newTask::class.java != existingTask::class.java) {
            throw IllegalArgumentException("The passed task must have the same type as the existing task")
        }

        newTask.copyCallbacksFrom(existingTask)

        if (newTask is BranchTask<*, *, *, *> && newTask.innerTask is BaseTask<*, *>) {
            if (existingTask !is BranchTask<*, *, *, *> || existingTask.innerTask !is BaseTask<*, *>) {
                throw IllegalArgumentException("The passed task must have the same type as the existing task")
            }

            copyCallbacks(newTask.innerTask as BaseTask<*, *>, existingTask.innerTask as BaseTask<*, *>)
        }

        if (newTask is MultiBranchTask<*, *, *, *, *, *> && newTask.leftInnerTask is BaseTask<*, *> &&
                newTask.rightInnerTask is BaseTask<*, *>) {
            if (existingTask !is MultiBranchTask<*, *, *, *, *, *> || existingTask.leftInnerTask !is BaseTask<*, *> ||
                    existingTask.rightInnerTask !is BaseTask<*, *>) {
                throw IllegalArgumentException("The passed task must have the same type as the existing task")
            }

            copyCallbacks(newTask.leftInnerTask as BaseTask<*, *>, existingTask.leftInnerTask as BaseTask<*, *>)
            copyCallbacks(newTask.rightInnerTask as BaseTask<*, *>, existingTask.rightInnerTask as BaseTask<*, *>)
        }
    }

    internal class RetainedWorkerFragment<I, O>(var innerTask: BaseTask<I, O>) : Fragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            retainInstance = true
        }
    }
}
