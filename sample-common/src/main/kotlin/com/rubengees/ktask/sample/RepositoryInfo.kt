package com.rubengees.ktask.sample

import com.squareup.moshi.Json

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class RepositoryInfo(@field:Json(name = "items") val repositories: List<Repository>) {
    class Repository(
            @field:Json(name = "id") val id: Long,
            @field:Json(name = "name") val name: String,
            @field:Json(name = "owner") val owner: Owner,
            @field:Json(name = "stargazers_count") val stars: Int,
            @field:Json(name = "html_url") val url: String
    )

    class Owner(@field:Json(name = "login") val name: String)
}
