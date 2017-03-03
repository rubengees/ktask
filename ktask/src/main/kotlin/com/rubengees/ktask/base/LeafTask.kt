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
abstract class LeafTask<I, O> : BaseTask<I, O>() {

    override fun onInnerStart(callback: (() -> Unit)?) = this.apply { startCallback = callback }
}
