package com.bink.wallet.utils.LocalPointScraping

import android.util.Log
import com.bink.wallet.model.request.membership_card.MembershipCardRequest
import com.bink.wallet.utils.LocalStoreUtils
import org.koin.core.KoinComponent
import org.koin.core.inject

object WebScrapableManager : KoinComponent {

    private val webScrapeViewModel: WebScrapeViewModel by inject()

    private val scrapableAgents = arrayListOf(TescoScrapableAgent())

    private const val BASE_ENCRYPTED_KEY_SHARED_PREFERENCES =
        "com.bink.wallet.utils.LocalPointScraping.credentials.cardId_%s.%s"

    enum class CredentialType {
        USERNAME,
        PASSWORD
    }

    fun storeCredentialsFromRequest(membershipCardRequest: MembershipCardRequest) {

        for (scrapableAgent in scrapableAgents) {
            membershipCardRequest.membership_plan?.toIntOrNull()?.let { membershipPlanId ->
                if (scrapableAgent.membershipPlanId == membershipPlanId) {
                    membershipCardRequest.account?.authorise_fields?.let { authorizeFields ->

                        val username = authorizeFields.firstOrNull {
                            (it.column ?: "").equals(scrapableAgent.usernameFieldTitle)
                        }?.value
                        val password = authorizeFields.firstOrNull {
                            (it.column ?: "").equals(scrapableAgent.passwordFieldTitle)
                        }?.value

                        val webScrapeCredentials =
                            WebScrapeCredentials(username, password)
                        webScrapeViewModel.storeWebScrapeCredentials(webScrapeCredentials)

                        Log.d("TescoLPS", "$username, $password")

                    }
                }
            }

        }

    }

    private fun storeCredentials(webScrapeCredentials: WebScrapeCredentials, cardId: String) {

        //Storing the username/email here
        webScrapeCredentials.email?.let {
            LocalStoreUtils.setAppSharedPref(
                encryptedKeyForCardId(cardId, CredentialType.USERNAME),
                it
            )
        }

        //Storing password here
        webScrapeCredentials.password?.let {
            LocalStoreUtils.setAppSharedPref(
                encryptedKeyForCardId(cardId, CredentialType.PASSWORD),
                it
            )
        }
    }

    private fun retrieveCredentials(cardId: String): WebScrapeCredentials {
        val username =
            LocalStoreUtils.getAppSharedPref(encryptedKeyForCardId(cardId, CredentialType.USERNAME))
        val password =
            LocalStoreUtils.getAppSharedPref(encryptedKeyForCardId(cardId, CredentialType.PASSWORD))

        return WebScrapeCredentials(username,password)
    }

    private fun removeCredentials(cardId: String){
        LocalStoreUtils.removeKey(encryptedKeyForCardId(cardId, CredentialType.USERNAME))
        LocalStoreUtils.removeKey(encryptedKeyForCardId(cardId, CredentialType.PASSWORD))
    }
    private fun encryptedKeyForCardId(cardId: String, credentialType: CredentialType): String {

        return String.format(BASE_ENCRYPTED_KEY_SHARED_PREFERENCES, cardId, credentialType.name)
    }
    /** To test if it stores, uncomment this and call somewhere.
    fun logCreds() {
    webScrapeViewModel.getWebScrapeCredentials{
    Log.d("TescoLPS", "Stored creds $it")
    }
    }
     **/

}