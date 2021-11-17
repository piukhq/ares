package com.bink.wallet.scenes.settings

import android.content.res.Resources
import com.bink.wallet.BuildConfig
import com.bink.wallet.R
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.model.SettingsItemType
import com.bink.wallet.utils.enums.BuildTypes

object SettingsItemsPopulation {
    fun populateItems(res: Resources): ArrayList<SettingsItem> {
        val itemsList = ArrayList<SettingsItem>()
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_account),
                null,
                SettingsItemType.HEADER,
                null
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.preferences_text),
                null,
                SettingsItemType.PREFERENCES,
                null
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_log_out),
                null,
                SettingsItemType.LOGOUT,
                null
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_support_and_feedback),
                null,
                SettingsItemType.HEADER,
                null
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_faqs),
                res.getString(R.string.settings_menu_faqs_subtitle),
                SettingsItemType.FAQS,
                res.getString(R.string.faq_url)
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_contact_us),
                res.getString(R.string.settings_menu_contact_us_subtitle),
                SettingsItemType.CONTACT_US,
                null
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_rate_this_app),
                null,
                SettingsItemType.RATE_APP,
                null
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_about),
                null,
                SettingsItemType.HEADER,
                null
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_security_and_privacy),
                res.getString(R.string.settings_menu_security_and_privacy_subtitle),
                SettingsItemType.SECURITY_AND_PRIVACY,
                null
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_how_it_works),
                res.getString(R.string.settings_menu_how_it_works_subtitle),
                SettingsItemType.HOW_IT_WORKS,
                null
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_who_we_are),
                null,
                SettingsItemType.WHO_WE_ARE,
                null
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_legal),
                null,
                SettingsItemType.HEADER,
                null
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_privacy_policy),
                null,
                SettingsItemType.PRIVACY_POLICY,
                res.getString(R.string.privacy_policy_url)
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_terms_and_conditions),
                null,
                SettingsItemType.TERMS_AND_CONDITIONS,
                res.getString(R.string.ts_and_cs_url)
            )
        )

        if (BuildConfig.BUILD_TYPE.lowercase() != BuildTypes.RELEASE.type) {
            itemsList.add(
                SettingsItem(
                    res.getString(R.string.settings_menu_debug),
                    null,
                    SettingsItemType.HEADER,
                    null
                )
            )
            itemsList.add(
                SettingsItem(
                    "Debug",
                    "Only accessible on debug builds",
                    SettingsItemType.DEBUG_MENU,
                    null
                )
            )
        }

        itemsList.add(
            SettingsItem(
                null,
                null,
                SettingsItemType.FOOTER,
                null
            )
        )
        return itemsList
    }
}