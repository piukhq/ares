package com.bink.wallet.scenes.settings

import android.content.res.Resources
import com.bink.wallet.BuildConfig
import com.bink.wallet.R
import com.bink.wallet.model.SettingsItem
import com.bink.wallet.model.SettingsItemType
import com.bink.wallet.network.ApiConstants
import com.bink.wallet.utils.ENVIRONMENTS_TO_DEBUG

object SettingsItemsPopulation {
    fun populateItems(res: Resources): ArrayList<SettingsItem> {
        val buildTypes: List<String> = ENVIRONMENTS_TO_DEBUG

        val itemsList = ArrayList<SettingsItem>()
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_account),
                null,
                SettingsItemType.HEADER
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_log_out),
                null,
                SettingsItemType.LOGOUT
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_support_and_feedback),
                null,
                SettingsItemType.HEADER
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_faqs),
                res.getString(R.string.settings_menu_faqs_subtitle),
                SettingsItemType.FAQS
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_contact_us),
                res.getString(R.string.settings_menu_contact_us_subtitle),
                SettingsItemType.CONTACT_US
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_rate_this_app),
                null,
                SettingsItemType.RATE_APP
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_about),
                null,
                SettingsItemType.HEADER
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_security_and_privacy),
                res.getString(R.string.settings_menu_security_and_privacy_subtitle),
                SettingsItemType.SECURITY_AND_PRIVACY
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_how_it_works),
                res.getString(R.string.settings_menu_how_it_works_subtitle),
                SettingsItemType.HOW_IT_WORKS
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_legal),
                null,
                SettingsItemType.HEADER
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_privacy_policy),
                null,
                SettingsItemType.PRIVACY_POLICY
            )
        )
        itemsList.add(
            SettingsItem(
                res.getString(R.string.settings_menu_terms_and_conditions),
                null,
                SettingsItemType.TERMS_AND_CONDITIONS
            )
        )

        if (buildTypes.contains(BuildConfig.BUILD_TYPE)) {
            itemsList.add(
                SettingsItem(
                    res.getString(R.string.settings_menu_debug),
                    null,
                    SettingsItemType.HEADER
                )
            )
            itemsList.add(
                SettingsItem(
                    res.getString(R.string.current_version),
                    versionName(res),
                    SettingsItemType.VERSION_NUMBER
                )
            )
            itemsList.add(
                SettingsItem(
                    res.getString(R.string.environment_base_url),
                    ApiConstants.BASE_URL,
                    SettingsItemType.BASE_URL
                )
            )
            itemsList.add(
                SettingsItem(
                    res.getString(R.string.current_email_address),
                    null,
                    SettingsItemType.EMAIL_ADDRESS
                )
            )
        }
        return itemsList
    }

    private fun versionName(res: Resources): String =
        res.getString(R.string.version_name_format, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
}