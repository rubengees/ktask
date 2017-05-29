package com.rubengees.ktask.sample.android

import android.app.Application
import com.rubengees.ktask.sample.GitHubApi
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
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
            private set

        lateinit var refWatcher: RefWatcher
            private set
    }

    override fun onCreate() {
        super.onCreate()

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }

        refWatcher = LeakCanary.install(this)

        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()))
                .build()

        api = retrofit.create(GitHubApi::class.java)
    }
}
