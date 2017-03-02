package com.rubengees.ktask.sample

import com.squareup.moshi.Json

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class RepositoryInfo(@Json(name = "items") val repositories: List<Repository>) {

    class Repository(@Json(name = "id") val id: Long, @Json(name = "name") val name: String,
                     @Json(name = "owner") val owner: Owner, @Json(name = "stargazers_count") val stars: Int,
                     @Json(name = "html_url") val url: String) {
        class Owner(@Json(name = "login") val name: String)
    }
}
