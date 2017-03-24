package com.rubengees.ktask.util

/**
 * Exception which holds the two causes for a [com.rubengees.ktask.base.MultiBranchTask] to fail.
 *
 * @param firstInnerError The first error, causing the task to fail.
 * @property secondInnerError The second error, causing the task to fail.
 *
 * @author Ruben Gees
 */
class FullTaskException(val firstInnerError: Throwable, val secondInnerError: Throwable) : Exception()
