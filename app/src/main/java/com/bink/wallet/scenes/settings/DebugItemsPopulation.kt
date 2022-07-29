package com.bink.wallet.scenes.settings

import android.content.res.Resources
import com.bink.wallet.BuildConfig
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.model.DebugItem
import com.bink.wallet.model.DebugItemType
import com.bink.wallet.utils.LocalStoreUtils

object DebugItemsPopulation {
    fun populateItems(res: Resources): ArrayList<DebugItem> {
        val itemsList = ArrayList<DebugItem>()
        itemsList.add(
            DebugItem(
                res.getString(R.string.debug_current_version_title),
                retrieveVersionName(res),
                DebugItemType.CURRENT_VERSION
            )
        )
        itemsList.add(
            DebugItem(
                res.getString(R.string.debug_environment_base_url_title),
                SharedPreferenceManager.storedApiUrl.toString(),
                DebugItemType.ENVIRONMENT
            )
        )
        itemsList.add(
            DebugItem(
                res.getString(R.string.debug_backend_version),
                SharedPreferenceManager.storedBackendVersion.toString(),
                DebugItemType.BACKEND_VERSION
            )
        )
        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_EMAIL)?.let {
            itemsList.add(
                DebugItem(
                    res.getString(R.string.debug_current_email_address),
                    it,
                    DebugItemType.EMAIL
                )
            )
        }
        itemsList.add(
            DebugItem(
                res.getString(R.string.debug_backend_colour_swatches),
                "",
                DebugItemType.COLOR_SWATCHES
            )
        )
        itemsList.add(
            DebugItem(
                "Force Crash",
                "This will immediately crash the application",
                DebugItemType.FORCE_CRASH
            )
        )

        itemsList.add(
            DebugItem(
                "Card on boarding state",
                "Select how many cards to display",
                DebugItemType.CARD_ON_BOARDING
            )
        )

        itemsList.add(
            DebugItem(
                res.getString(R.string.clear_image_title),
                res.getString(R.string.clear_image_body),
                DebugItemType.RESET_CACHE
            )
        )

        return itemsList
    }

    private fun retrieveVersionName(res: Resources): String =
        res.getString(
            R.string.debug_current_version_content,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )
}