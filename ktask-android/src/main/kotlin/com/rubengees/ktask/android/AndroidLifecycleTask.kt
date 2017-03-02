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
import com.rubengees.ktask.base.Task

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class AndroidLifecycleTask<I, O> : BaseTask<I, O> {

    override val isWorking: Boolean
        get() = workerFragment.innerTask.isWorking

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

            throw IllegalArgumentException("The passed View must reside in a FragmentActivity " +
                    "(or AppcompatActivity)")
        }
    }

    @SuppressLint("CommitTransaction")
    constructor(context: FragmentActivity, innerTask: Task<I, O>, tag: String) {
        this.context = context

        val existingWorker = context.supportFragmentManager.findFragmentByTag(tag)

        @Suppress("UNCHECKED_CAST")
        if (existingWorker is RetainedWorkerFragment<*, *>) {
            workerFragment = existingWorker as RetainedWorkerFragment<I, O>
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
                if (activity == context) {
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

    constructor(context: Fragment, innerTask: Task<I, O>, tag: String) {
        this.context = context.activity

        val existingWorker = context.childFragmentManager.findFragmentByTag(tag)

        @Suppress("UNCHECKED_CAST")
        if (existingWorker is RetainedWorkerFragment<*, *>) {
            workerFragment = existingWorker as RetainedWorkerFragment<I, O>
        } else {
            workerFragment = RetainedWorkerFragment(innerTask).apply {
                context.childFragmentManager.beginTransaction().add(this, tag).commitNow()
            }
        }

        val lifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fragmentManager: FragmentManager, fragment: Fragment) {
                if (fragment == context) {
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

    constructor(context: View, innerTask: Task<I, O>, tag: String) :
            this(findActivityForView(context), innerTask, tag)

    override fun execute(input: I) {
        workerFragment.innerTask.execute(input)
    }

    override fun cancel() {
        workerFragment.innerTask.cancel()
    }

    override fun reset() {
        workerFragment.innerTask.reset()
    }

    override fun destroy() {
        workerFragment.innerTask.destroy()
        context = null

        super.destroy()
    }

    fun retainingDestroy() {
        context = null

        super.destroy()
    }

    override fun onStart(callback: () -> Unit): Task<I, O> {
        workerFragment.innerTask.onStart(callback)

        return this
    }

    internal class RetainedWorkerFragment<I, O>(val innerTask: Task<I, O>) : Fragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            retainInstance = true
        }
    }
}
