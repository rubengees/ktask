package com.rubengees.ktask.base

/**
 * Base class for all tasks with no children.
 *
 * Only [onInnerStart] has a default implementation here. As this is the bottommost task in the tree, we can assign the
 * onStart callback here.
 *
 * @param I The type of input.
 * @param O The type of output.
 *
 * @author Ruben Gees
 */
abstract class LeafTask<I, O, SELF : LeafTask<I, O, SELF>> : BaseTask<I, O, SELF>() {

    override fun onInnerStart(callback: (() -> Unit)?) = me.apply { startCallback = callback }

    override fun cancel() {

    }

    override fun reset() {
        cancel()
    }

    override fun retainingDestroy() {
        super.retainingDestroy()

        cancel()
    }

    override fun destroy() {
        super.destroy()

        reset()
    }
}
