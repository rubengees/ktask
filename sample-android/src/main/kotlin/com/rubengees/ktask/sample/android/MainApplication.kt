package com.rubengees.ktask.sample.android

import android.app.Application
import com.rubengees.ktask.sample.GitHubApi
import com.squareup.leakcanary.LeakCanary
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class MainApplication : Application() {

    companion object {
        lateinit var api: GitHubApi
    }

    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }

        LeakCanary.install(this)

        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

        api = retrofit.create(GitHubApi::class.java)
    }
}
