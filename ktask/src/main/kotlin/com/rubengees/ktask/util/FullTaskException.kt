package com.rubengees.ktask.util

/**
 * Exception which holds the two causes for a [com.rubengees.ktask.base.MultiBranchTask] to fail.
 *
 * @param cause The first cause for the task to fail.
 *
 * @property secondCause The second cause for the task to fail.
 *
 * @author Ruben Gees
 */
class FullTaskException(cause: Throwable, val secondCause: Throwable) : Exception(cause)
