# KTask

Painless tasks for the JVM and Android, written in Kotlin.

### Including in your project

Gradle is recommended for using this library.<br>
Add this to your project-level `build.gradle`:

```groovy
repositories {
    maven { url "https://jitpack.io" }
}
```

And this to your module-level `build.gradle`:

```groovy
dependencies {
    compile "com.github.rubengees.ktask:ktask:0.4.0"
}
```

### Usage

KTask allows you to run tasks in a ordered and reactive way.<br>
This is what a task looks like for calculating all prime numbers up to 1000:

```kotlin
fun main(args: Array<String>) {
    TaskBuilder.task(PrimeTask())
            .onSuccess { println("These are the primes up to 1000: $it") }
            .build()
            .execute(1000)
}

class PrimeTask : WorkerTask<Int, List<Int>>() {

    /**
     * Does the actual work. [input] specifies the number to calculate primes to (exclusive).
     */
    override fun work(input: Int): List<Int> {
        val result = arrayListOf<Int>()

        for (i in 3 until input step 2) {
            val isPrime = (2 until i).none { i % it == 0 }

            if (isPrime) {
                result.add(i)
            }
        }

        return result
    }
}
```

The `TaskBuilder` allows you to construct complex tasks, with fine grained control over each component.<br>
The following operations are supported:

| Name | Description |
| -- | -- |
| `cache` | Caches the last result (or error) of the task. |
| `inputEcho` | Returns a `Pair` of the input and the result. Usefull when you futher need the input in your task chain. |
| `map` | Transforms the result to a new type. |
| `parallelWith` | Runs two tasks in parallel, returning the result of the `zipFunction`, specified by you. |
| `then` | Runs two tasks in series, returning the result of the second task. The result of the first task acts as input for the second task. |
| `validateBefore` | Convenience task for simpliy checking the input before running the actual task. If the input is not valid, you are expected to throw an `Exception`. |
| `async` | Runs the task asynchronous in its own thread. |

Furthermore the following callbacks can be set on each task:

| Name | Description |
| -- | -- |
| `onStart` | Called when the task starts. |
| `onSuccess` | Called when the task finishes successfully. |
| `onError` | Called when the task finishes with an error. |
| `onFinish` | Called when the task finishes either successfully or with an error. This is always called after  `onSuccess` and `onError`. |
| `onInnerStart` | Convenience function for assigning an `onStart` callback to the innermost task which does the actual work. This is usefull when you only want to know if actual work is going to be done and not if for example the cache is queried. |

Tasks have a lifecycle which you can control by these functions:

| Name | Description |
| -- | -- |
| `execute` | Simply executes the task, if not running already. |
| `forceExecute` | Executes the task, even if it running already (cancelling the ongoing execution). |
| `freshExecute` | Like `forceExecute` but also clears temporary data like a cache. |
| `cancel` | Simply cancels the task if it is running. |
| `reset` | Like `cancel` but also deletes all temporary data like a cache. |
| `destroy` | Like `reset` but also removes all callbacks and references to avoid memory-leaks. |
| `retainingDestroy` | Removes all callbacks and refrerences, but does not cancel the task. |
