package com.rubengees.ktask.sample

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
interface GitHubApi {

    @GET("search/repositories?sort=stars&order=desc")
    fun mostStarredRepositories(@Query("q") query: String): Call<RepositoryInfo>
}
