package com.bink.wallet.model


import android.content.Context
import com.bink.wallet.R
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class LocationOpeningHours(
    @SerializedName("Fri")
    val fri: List<List<String>>,
    @SerializedName("Mon")
    val mon: List<List<String>>,
    @SerializedName("Sat")
    val sat: List<List<String>>,
    @SerializedName("Sun")
    val sun: List<List<String>>,
    @SerializedName("Thu")
    val thu: List<List<String>>,
    @SerializedName("Tue")
    val tue: List<List<String>>,
    @SerializedName("Wed")
    val wed: List<List<String>>,
)

fun LocationOpeningHours.asArray(): ArrayList<Triple<String, String?, String?>> {
    val monday = Triple("mon", mon.firstOrNull()?.get(0), mon.firstOrNull()?.get(1))
    val tuesday = Triple("tue", tue.firstOrNull()?.get(0), tue.firstOrNull()?.get(1))
    val wednesday = Triple("wed", wed.firstOrNull()?.get(0), wed.firstOrNull()?.get(1))
    val thursday = Triple("thu", thu.firstOrNull()?.get(0), thu.firstOrNull()?.get(1))
    val friday = Triple("fri", fri.firstOrNull()?.get(0), fri.firstOrNull()?.get(1))
    val saturday = Triple("sat", sat.firstOrNull()?.get(0), sat.firstOrNull()?.get(1))
    val sunday = Triple("sun", sun.firstOrNull()?.get(0), sun.firstOrNull()?.get(1))
    return arrayListOf(monday, tuesday, wednesday, thursday, friday, saturday, sunday)
}

fun Context.getCurrentOpeningTimes(locationOpeningHours: LocationOpeningHours): String {
    val tomorrowCalender = Calendar.getInstance()
    tomorrowCalender.add(Calendar.DAY_OF_YEAR, 1)

    val calender = Calendar.getInstance()
    val today = SimpleDateFormat("EE", Locale.ENGLISH).format(calender.time)

    val currentTime = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(calender.time)

    val openingHours = locationOpeningHours.asArray()
    repeat(7) { // Repeating this up to 7 times for each day of the week.
        val currentDay = SimpleDateFormat("EE", Locale.ENGLISH).format(calender.time)
        val tomorrow = SimpleDateFormat("EE", Locale.ENGLISH).format(tomorrowCalender.time)
        val fullDayFormat = SimpleDateFormat("EEEE", Locale.ENGLISH).format(calender.time)
        val dayOpeningHours = findDay(currentDay, openingHours)
        try {

            //Check to see if we've had to iterate to the next day, if so the shop is closed.
            if (currentDay != today && currentDay != tomorrow) {
                return buildString {
                    append(getString(R.string.location_closed_title))
                    append("${dayOpeningHours?.second} on $fullDayFormat")
                }
            } else if (currentDay != today) {
                return buildString {
                    append(getString(R.string.location_closed_title))
                    append(dayOpeningHours?.second)
                }
            }

            val openTime = dayOpeningHours?.second?.replace(":", "")?.toInt()!!
            val closeTime = dayOpeningHours.third?.replace(":", "")?.toInt()!!
            val formattedCurrentTime = currentTime.replace(":", "").toInt()

            if (formattedCurrentTime < openTime) {
                return buildString {
                    append(getString(R.string.location_closed_title))
                    append(dayOpeningHours.second)
                }
            } else if (formattedCurrentTime in (openTime + 1) until closeTime) {
                return if ((closeTime - formattedCurrentTime) < 100) { // Check to see if theres less than a hour left
                    buildString {
                        append(getString(R.string.location_closes_soon_title))
                        append(dayOpeningHours.third)
                    }
                } else {
                    buildString {
                        append(getString(R.string.location_open_title))
                        append(dayOpeningHours.third)
                    }
                }
            }

        } catch (e: Exception) {
            //Iterate
        }

        calender.add(Calendar.DAY_OF_YEAR, 1)
    }

    return ""
}

private fun findDay(day: String, dayArray: ArrayList<Triple<String, String?, String?>>): Triple<String, String?, String?>? {
    return dayArray.find { it.first == day.lowercase() }
}