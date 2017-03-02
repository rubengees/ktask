package com.rubengees.ktask.sample

import java.text.SimpleDateFormat
import java.util.*

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
object Utils {
    fun query(): String {
        return "created:>${sevenDaysAgo()} language:kotlin"
    }

    private fun sevenDaysAgo(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.time)
    }
}
