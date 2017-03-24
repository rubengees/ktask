package com.rubengees.ktask.android

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ContextWrapper
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.view.View
import com.rubengees.ktask.base.BranchTask
import com.rubengees.ktask.base.Task

/**
 * Task for working in a safe manner with the Android Lifecycle.
 *
 * Tasks continue to work, if an orientation change occurs. They are also safely delivered. If the Activity is not
 * resumed or the fragment is not attached, no results are delivered. Moreover it destroys itself automatically, if the
 * Activity finishes. A call to [cancel] in onDestroy is not necessary.
 *
 * Note that you have to pass the same [innerTask] if you reassign the task after an orientation change.
 *
 * @param I The type of input.
 * @param O The type of output.
 *
 * @author Ruben Gees
 */
class AndroidLifecycleTask<I, O> : BranchTask<I, O, I, O> {

    override val isWorking: Boolean
        get() = innerTask.isWorking

    override val innerTask: Task<I, O>
        get() = workerFragment.innerTask

    private val workerFragment: RetainedWorkerFragment<I, O>
    private var context: Activity?

    private val handler = Handler(Looper.getMainLooper())

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

    /**
     * Constructor for binding the task to an [FragmentActivity].
     *
     * @param tag The tag of this task. This has to be unique.
     */
    @SuppressLint("CommitTransaction")
    constructor(context: FragmentActivity, innerTask: Task<I, O>, tag: String) {
        this.context = context

        val existingWorker = context.supportFragmentManager.findFragmentByTag(tag)

        @Suppress("UNCHECKED_CAST")
        if (existingWorker is RetainedWorkerFragment<*, *>) {
            workerFragment = existingWorker as RetainedWorkerFragment<I, O>

            this.innerTask.restoreCallbacks(innerTask)
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

        this.innerTask.onSuccess { finishSuccessful(it) }
        this.innerTask.onError { finishWithError(it) }
    }

    /**
     * Constructor for binding the task to a [Fragment].
     *
     * @param tag The tag of this task. This has to be unique.
     */
    constructor(context: Fragment, innerTask: Task<I, O>, tag: String) {
        this.context = context.activity

        val existingWorker = context.childFragmentManager.findFragmentByTag(tag)

        @Suppress("UNCHECKED_CAST")
        if (existingWorker is RetainedWorkerFragment<*, *>) {
            workerFragment = existingWorker as RetainedWorkerFragment<I, O>

            this.innerTask.restoreCallbacks(innerTask)
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

                    fragmentManager.unregisterFragmentLifecycleCallbacks(this)
                }
            }
        }

        context.activity.supportFragmentManager.registerFragmentLifecycleCallbacks(lifecycleCallbacks, true)

        this.innerTask.onSuccess { finishSuccessful(it) }
        this.innerTask.onError { finishWithError(it) }
    }

    /**
     * Convenience constructor for binding this task to a [View].
     *
     * @param tag The tag of this task. This has to be unique.
     */
    constructor(context: View, innerTask: Task<I, O>, tag: String) : this(findActivityForView(context), innerTask, tag)

    override fun onInnerStart(callback: () -> Unit) = this.apply {
        innerTask.onInnerStart {
            safelyDeliver { callback.invoke() }
        }
    }

    override fun start(action: () -> Unit) {
        if (!(isWorking || (context?.isFinishing ?: false))) {
            isCancelled = false

            safelyDeliver {
                startCallbacks.forEach { it.invoke() }
            }

            action.invoke()
        }
    }

    override fun finishSuccessful(result: O) {
        safelyDeliver {
            successCallbacks.forEach { it.invoke(result) }
            finishCallbacks.forEach { it.invoke() }
        }
    }

    override fun finishWithError(error: Throwable) {
        safelyDeliver {
            errorCallbacks.forEach { it.invoke(error) }
            finishCallbacks.forEach { it.invoke() }
        }
    }

    override fun execute(input: I) {
        start {
            this.innerTask.execute(input)
        }
    }

    override fun retainingDestroy() {
        super.retainingDestroy()

        context = null
    }

    private fun safelyDeliver(action: () -> Unit) {
        if (!isCancelled) {
            context?.apply {
                handler.post { action.invoke() }
            }
        }
    }

    internal class RetainedWorkerFragment<I, O>(var innerTask: Task<I, O>) : Fragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            retainInstance = true
        }
    }
}
