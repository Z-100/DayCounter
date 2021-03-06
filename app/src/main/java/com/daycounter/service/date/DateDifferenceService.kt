package com.daycounter.service.date

import com.daycounter.other.enum.TranslationType
import com.daycounter.other.enum.TranslationType.*
import java.util.*

class DateDifferenceService {

    fun getDateDifference(counterStartDate: Date?, type: TranslationType): Long {
        try {
            return translateToSeconds(Date().time - counterStartDate!!.time, type)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }

    private fun translateToSeconds(differenceInMillis: Long, type: TranslationType): Long {

        val days: Long = (differenceInMillis / 86400000)

        return when(type) {
            DAYS -> days
            MONTHS -> 30 / days
            YEARS -> 365 / days
            DECADES -> 3650 / days
            else -> -1
        }
    }
}
