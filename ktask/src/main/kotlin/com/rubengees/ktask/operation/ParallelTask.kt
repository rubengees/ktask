package com.rubengees.ktask.operation

import com.rubengees.ktask.base.DelegateBranchTask
import com.rubengees.ktask.base.Task
import com.rubengees.ktask.util.TaskException

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class ParallelTask<LI, RI, LM, RM, O>(leftInnerTask: Task<LI, LM>, rightInnerTask: Task<RI, RM>,
                                      private val zipFunction: (LM, RM) -> O,
                                      private val awaitLeftResultOnError: Boolean = false,
                                      private val awaitRightResultOnError: Boolean = false) :
        DelegateBranchTask<Pair<LI, RI>, O, LI, RI, LM, RM>(leftInnerTask, rightInnerTask) {

    private var leftResult: LM? = null
    private var rightResult: RM? = null

    private var leftError: Throwable? = null
    private var rightError: Throwable? = null

    init {
        leftInnerTask.onSuccess {
            val safeRightResult = rightResult
            val safeRightError = rightError

            if (safeRightResult != null) {
                cancel()

                try {
                    finishSuccessful(zipFunction.invoke(it, safeRightResult))
                } catch(error: Exception) {
                    finishWithError(PartialTaskException(error, safeRightResult))
                }
            } else if (safeRightError != null) {
                cancel()

                finishWithError(PartialTaskException(safeRightError, it))
            } else {
                leftResult = it
            }
        }

        leftInnerTask.onError {
            val safeRightResult = rightResult

            if (safeRightResult != null) {
                cancel()

                finishWithError(PartialTaskException(it, safeRightResult))
            } else {
                if (awaitRightResultOnError) {
                    leftError = it
                } else {
                    cancel()

                    finishWithError(it)
                }
            }
        }

        rightInnerTask.onSuccess {
            val safeLeftResult = leftResult
            val safeLeftError = leftError

            if (safeLeftResult != null) {
                cancel()

                try {
                    finishSuccessful(zipFunction.invoke(safeLeftResult, it))
                } catch(error: Exception) {
                    finishWithError(PartialTaskException(error, safeLeftResult))
                }
            } else if (safeLeftError != null) {
                cancel()

                finishWithError(PartialTaskException(safeLeftError, it))
            } else {
                rightResult = it
            }
        }

        rightInnerTask.onError {
            val safeLeftResult = leftResult

            if (safeLeftResult != null) {
                cancel()

                finishWithError(PartialTaskException(it, safeLeftResult))
            } else {
                if (awaitLeftResultOnError) {
                    rightError = it
                } else {
                    cancel()

                    finishWithError(it)
                }
            }
        }
    }

    override fun execute(input: Pair<LI, RI>) {
        start {
            leftInnerTask.execute(input.first)
            rightInnerTask.execute(input.second)
        }
    }

    override fun cancel() {
        super.cancel()

        leftResult = null
        rightResult = null
        leftError = null
        rightError = null
    }

    override fun reset() {
        super.reset()

        leftResult = null
        rightResult = null
        leftError = null
        rightError = null
    }

    override fun destroy() {
        super.destroy()

        leftResult = null
        rightResult = null
        leftError = null
        rightError = null
    }

    // Why, oh why can't we have generics in Exceptions? :/
    class PartialTaskException(cause: Throwable, val partialData: Any?) : TaskException(cause)
}
