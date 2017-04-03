package com.rubengees.ktask.operation

import com.rubengees.ktask.base.MultiBranchTask
import com.rubengees.ktask.base.Task
import com.rubengees.ktask.util.FullTaskException
import com.rubengees.ktask.util.PartialTaskException

/**
 * Task for running the passed [leftInnerTask] and [rightInnerTask] in parallel.
 *
 * The [leftInnerTask] is defined to be started always before the [rightInnerTask].
 * Upon completion of both tasks, the results are zipped by the provided [zipFunction] and delivered.
 *
 * If an error occurs in one of the tasks, the execution is immediately aborted and the error delivered.
 * This behaviour can be altered with the [awaitLeftResultOnError] and [awaitRightResultOnError]. If one of those flags
 * is set. The task waits for the result of specified task. In that case a
 * [com.rubengees.ktask.util.PartialTaskException] is delivered with the data of the other task. If both tasks fail, a
 * [com.rubengees.ktask.util.FullTaskException] is delivered.
 *
 * @LI The type of input of the left task.
 * @RI The type of input of the right task.
 * @LM The type of output of the left task.
 * @RM The type of input of the right task.
 * @O The type of the zipped input.
 *
 * @author Ruben Gees
 */
class ParallelTask<LI, RI, LM, RM, O>(override val leftInnerTask: Task<LI, LM>,
                                      override val rightInnerTask: Task<RI, RM>,
                                      zipFunction: (LM, RM) -> O,
                                      private val awaitLeftResultOnError: Boolean = false,
                                      private val awaitRightResultOnError: Boolean = false) :
        MultiBranchTask<Pair<LI, RI>, O, LI, RI, LM, RM>() {

    private var zipFunction: ((LM, RM) -> O)? = zipFunction

    private var leftResult: LM? = null
    private var rightResult: RM? = null

    private var leftError: Throwable? = null
    private var rightError: Throwable? = null

    init {
        initCallbacks()
    }

    override fun execute(input: Pair<LI, RI>) {
        start {
            leftInnerTask.execute(input.first)
            rightInnerTask.execute(input.second)
        }
    }

    override fun cancel() {
        super.cancel()

        internalCancel()
    }

    @Suppress("UNCHECKED_CAST")
    override fun restoreCallbacks(from: Task<Pair<LI, RI>, O>) {
        super.restoreCallbacks(from)

        if (from !is ParallelTask<*, *, *, *, *>) {
            throw IllegalArgumentException("The passed task must have the same type.")
        }

        zipFunction = from.zipFunction as ((LM, RM) -> O)?

        initCallbacks()
    }

    private fun initCallbacks() {
        leftInnerTask.onSuccess {
            val safeRightResult = rightResult
            val safeRightError = rightError

            if (safeRightResult != null) {
                internalCancel()

                this.zipFunction?.let { function ->
                    try {
                        finishSuccessful(function.invoke(it, safeRightResult))
                    } catch(error: Throwable) {
                        finishWithError(PartialTaskException(error, safeRightResult))
                    }
                }
            } else if (safeRightError != null) {
                internalCancel()

                finishWithError(PartialTaskException(safeRightError, it))
            } else {
                leftResult = it
            }
        }

        leftInnerTask.onError {
            val safeRightResult = rightResult
            val safeRightError = rightError

            if (safeRightResult != null) {
                internalCancel()

                finishWithError(PartialTaskException(it, safeRightResult))
            } else if (safeRightError != null) {
                internalCancel()

                finishWithError(FullTaskException(safeRightError, it))
            } else {
                if (awaitRightResultOnError) {
                    leftError = it
                } else {
                    internalCancel()

                    finishWithError(it)
                }
            }
        }

        rightInnerTask.onSuccess {
            val safeLeftResult = leftResult
            val safeLeftError = leftError

            if (safeLeftResult != null) {
                internalCancel()

                this.zipFunction?.let { function ->
                    try {
                        finishSuccessful(function.invoke(safeLeftResult, it))
                    } catch(error: Throwable) {
                        finishWithError(PartialTaskException(error, safeLeftResult))
                    }
                }
            } else if (safeLeftError != null) {
                internalCancel()

                finishWithError(PartialTaskException(safeLeftError, it))
            } else {
                rightResult = it
            }
        }

        rightInnerTask.onError {
            val safeLeftResult = leftResult
            val safeLeftError = leftError

            if (safeLeftResult != null) {
                internalCancel()

                finishWithError(PartialTaskException(it, safeLeftResult))
            } else if (safeLeftError != null) {
                internalCancel()

                finishWithError(FullTaskException(safeLeftError, it))
            } else {
                if (awaitLeftResultOnError) {
                    rightError = it
                } else {
                    internalCancel()

                    finishWithError(it)
                }
            }
        }
    }

    private fun internalCancel() {
        leftInnerTask.cancel()
        rightInnerTask.cancel()

        leftResult = null
        rightResult = null
        leftError = null
        rightError = null
    }
}
