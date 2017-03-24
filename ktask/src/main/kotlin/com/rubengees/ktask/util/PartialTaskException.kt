package com.rubengees.ktask.util

/**
 * Exception which holds the cause of failure of a child of a [com.rubengees.ktask.base.MultiBranchTask] and the partial
 * result of the other child.
 *
 * @param innerError The error, causing the task to fail.
 * @property partialResult The result of the other task, which completed successfully.
 *
 * @author Ruben Gees
 */
class PartialTaskException(val innerError: Throwable, val partialResult: Any?) : Exception()
