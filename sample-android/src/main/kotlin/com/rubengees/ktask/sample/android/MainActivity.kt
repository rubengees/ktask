package com.rubengees.ktask.sample.android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.root, MainFragment.newInstance())
                    .commitNow()
        }
    }
}
