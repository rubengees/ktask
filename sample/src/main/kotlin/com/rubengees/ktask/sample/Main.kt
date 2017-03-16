@file:JvmName("Main")

package com.rubengees.ktask.sample

import com.rubengees.ktask.retrofit.retrofitTask
import com.rubengees.ktask.util.TaskBuilder
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * The main function. It loads the trending Kotlin repositories asynchronously and prints them on the console.
 */
fun main(args: Array<String>) {
    val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

    val api = retrofit.create(GitHubApi::class.java)

    TaskBuilder.retrofitTask<RepositoryInfo>()
            .onStart {
                println("Asynchronous loading started!\n")
            }
            .onSuccess {
                println(it.repositories.map {
                    "${it.name} by ${it.owner.name} with ${it.stars} ${if (it.stars == 1) "star" else "stars"}."
                }.joinToString(separator = "\n", prefix = "These are the trending Kotlin repositories of the week:\n\n"))
            }
            .onError {
                println("Error retrieving: ${it.message}")
            }
            .onFinish {
                print("\nAsynchronous loading finished!")

                System.exit(0)
            }
            .build()
            .execute(api.mostStarredRepositories(Utils.query()))
}
