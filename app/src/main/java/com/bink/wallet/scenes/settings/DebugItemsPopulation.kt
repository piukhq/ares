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
        LocalStoreUtils.getAppSharedPref(LocalStoreUtils.KEY_EMAIL)?.let {
            itemsList.add(
                DebugItem(
                    res.getString(R.string.debug_current_email_address),
                    it,
                    DebugItemType.EMAIL
                )
            )
            itemsList.add(
                DebugItem(
                    res.getString(R.string.debug_environment_open_zendesk_title),
                    "",
                    DebugItemType.ZENDESK
                )
            )
        }
        return itemsList
    }

    private fun retrieveVersionName(res: Resources): String =
        res.getString(
            R.string.debug_current_version_content,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )
}