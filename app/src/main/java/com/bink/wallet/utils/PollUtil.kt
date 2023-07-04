package com.bink.wallet.utils

import com.bink.wallet.data.SharedPreferenceManager
import java.util.concurrent.TimeUnit

object PollUtil {

    private const val TWENTY_FOUR_HOURS = 1440

    fun dismissPollFor24h(pollId: String) {
        val previousDismissed = arrayListOf("")
        //Add all previously dismissed
        SharedPreferenceManager.tempDismissedPolls?.split(":")?.let { previousDismissed.addAll(it) }
        //Find if this poll has been dismissed before
        val thisPoll = previousDismissed.firstOrNull { it.contains(pollId) }
        //remove it if it existed previously
        thisPoll?.let {
            previousDismissed.remove(it)
        }
        //Add a new version
        previousDismissed.add("$pollId,${System.currentTimeMillis()}")
        SharedPreferenceManager.tempDismissedPolls = previousDismissed.joinToString()
    }

    fun dismissPollPermanently(pollId: String) {
        val previousDismissed = SharedPreferenceManager.permDismissedPolls ?: ""
        SharedPreferenceManager.permDismissedPolls = "$previousDismissed:$pollId"
    }

    fun canViewPoll(pollId: String): Boolean {
        //Check if its been perm dismissed before
        val previousPermDismissed = SharedPreferenceManager.permDismissedPolls ?: ""
        if (previousPermDismissed.contains(pollId)) {
            return false
        }

        //If its not perm dismissed, check if its been temp dismissed
        val previousTempDismissed = arrayListOf("")
        //Add all previously dismissed
        SharedPreferenceManager.tempDismissedPolls?.split(":")?.let { previousTempDismissed.addAll(it) }

        //Find if this poll has been dismissed before
        previousTempDismissed.firstOrNull { it.contains(pollId) }?.let { thisPoll ->
            //find poll dismiss time by splitting the string
            val pollDismissTime = thisPoll.split(",")[2]

            return try {
                have24HoursElapsed(pollDismissTime.trim().toLong())
            } catch (e: Exception) {
                false
            }
        }

        return true
    }

    private fun have24HoursElapsed(time: Long): Boolean {
        val difference = System.currentTimeMillis() - time
        val minutes = TimeUnit.MILLISECONDS.toMinutes(difference)
        return minutes > TWENTY_FOUR_HOURS
    }

}