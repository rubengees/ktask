package com.rubengees.ktask.base

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
abstract class DelegateBranchTask<I, O, LI, RI, LO, RO>(val leftInnerTask: Task<LI, LO>,
                                                        val rightInnerTask: Task<RI, RO>) :
        BaseTask<I, O>() {

    override val isWorking: Boolean
        get() = leftInnerTask.isWorking || rightInnerTask.isWorking

    override fun cancel() {
        leftInnerTask.cancel()
        rightInnerTask.cancel()
    }

    override fun reset() {
        leftInnerTask.reset()
        rightInnerTask.reset()
    }

    override fun destroy() {
        leftInnerTask.destroy()
        rightInnerTask.destroy()

        super.destroy()
    }
}
