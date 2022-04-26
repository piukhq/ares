package com.bink.wallet.scenes.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.BuildConfig
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentSplashBinding
import com.bink.wallet.network.ApiConfig
import com.bink.wallet.network.ApiConstants
import com.bink.wallet.utils.*
import com.bink.wallet.utils.enums.BuildTypes
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.scottyab.rootbeer.RootBeer
import org.koin.androidx.viewmodel.ext.android.viewModel
import zendesk.core.Zendesk
import zendesk.support.Support

class SplashFragment : BaseFragment<SplashViewModel, FragmentSplashBinding>() {
    override val layoutRes: Int
        get() = R.layout.fragment_splash
    override val viewModel: SplashViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder().build()
    }

    companion object {
        init {
            System.loadLibrary("api_keys-lib")
        }
    }

    private external fun spreedlyKey(): String
    private external fun paymentCardHashingDevKey(): String
    private external fun paymentCardHashingStagingKey(): String
    private external fun paymentCardHashingProdKey(): String
    private external fun paymentCardEncryptionPublicKeyDev(): String
    private external fun paymentCardEncryptionPublicKeyStaging(): String
    private external fun paymentCardEncryptionPublicKeyProd(): String

    // Zendesk Keys
    private external fun zendeskSandboxUrl(): String
    private external fun zendeskSandboxAppId(): String
    private external fun zendeskSandboxOAuthId(): String
    private external fun zendeskProdUrl(): String
    private external fun zendeskProdAppId(): String
    private external fun zendeskProdOAuthId(): String

    // Bouncer
    private external fun bouncerDevKey(): String

    private external fun bouncerProdKey(): String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        LocalStoreUtils.setAppSharedPref(
            LocalStoreUtils.KEY_SPREEDLY, spreedlyKey()
        )
        Keys
        persistPaymentCardHashSecret()
        configureZendesk()
        persistBouncerKeys()
        setUpRemoteConfig()
        RequestReviewUtil.recordAppOpen()
        if (findNavController().currentDestination?.id == R.id.splash) {
            if (getDirections() == R.id.global_to_home) {
                goToDashboard()
            } else {
                findNavController().navigateIfAdded(this, getDirections())
            }
        }
    }

    private fun getDirections(): Int {
//        val rootBeer = RootBeer(context)
//        return when (rootBeer.isRooted) {
//            true -> R.id.splash_to_rooted_device
//            else -> return getUnRootedDirections()
//        }
        return getUnRootedDirections()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.postServiceResponse.observeNonNull(this) {
            findNavController().navigateIfAdded(this, getDirections())
        }
        viewModel.postServiceErrorResponse.observeNonNull(this) {
            findNavController().navigateIfAdded(this, getDirections())
        }
    }

    private fun getUnRootedDirections(): Int {

        return when (requireContext().let { LocalStoreUtils.isLoggedIn(LocalStoreUtils.KEY_TOKEN) }) {
            true -> R.id.global_to_home
            else ->
                /**
                 *      Since in the future we might want to redirect the user to
                 * different screens we can do that based on a destination
                 * string in the intent
                 *      If the user isn't logged in then it is sent to onboarding.
                 * Since an 'else' branch can't be merged together with another
                 * option in a when clause, we will have for two clauses with the
                 * same destination for now.
                 **/
                when (requireActivity().intent.getSessionHandlerNavigationDestination()) {
                    SESSION_HANDLER_DESTINATION_ONBOARDING -> R.id.splash_to_onboarding
                    else -> R.id.splash_to_onboarding
                }
        }
    }

    private fun persistPaymentCardHashSecret() {
        when {
            ApiConstants.BASE_URL == ApiConfig.PROD_URL -> {
                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_PAYMENT_HASH_SECRET, paymentCardHashingProdKey()
                )

                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_ENCRYPT_PAYMENT_PUBLIC_KEY,
                    paymentCardEncryptionPublicKeyProd()
                )
            }
            ApiConstants.BASE_URL == ApiConfig.STAGING_URL -> {
                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_PAYMENT_HASH_SECRET, paymentCardHashingStagingKey()
                )

                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_ENCRYPT_PAYMENT_PUBLIC_KEY,
                    paymentCardEncryptionPublicKeyStaging()
                )
            }
            ApiConstants.BASE_URL == ApiConfig.DEV_URL -> {
                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_PAYMENT_HASH_SECRET, paymentCardHashingDevKey()
                )

                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_ENCRYPT_PAYMENT_PUBLIC_KEY,
                    paymentCardEncryptionPublicKeyDev()
                )
            }
        }
    }

    private fun configureZendesk() {
        val isProduction =
            BuildConfig.BUILD_TYPE.lowercase() == BuildTypes.RELEASE.type
        Zendesk.INSTANCE.init(
            requireActivity(),
            if (isProduction) zendeskProdUrl() else zendeskSandboxUrl(),
            if (isProduction) zendeskProdAppId() else zendeskSandboxAppId(),
            if (isProduction) zendeskProdOAuthId() else zendeskSandboxOAuthId()
        )

        Support.INSTANCE.init(Zendesk.INSTANCE)
    }

    private fun persistBouncerKeys() {
        var bouncerKey = bouncerDevKey()

        if (BuildConfig.BUILD_TYPE == BuildTypes.RELEASE.toString().lowercase()) {
            bouncerKey = bouncerProdKey()
        }

        LocalStoreUtils.setAppSharedPref(
            LocalStoreUtils.KEY_BOUNCER_KEY,
            bouncerKey
        )
    }

    private fun goToDashboard() {
        viewModel.getUserResponse.observeNonNull(this) {
            findNavController().navigateIfAdded(this, R.id.global_to_home)
            setAnalyticsUserId(it.uid)
        }
        viewModel.getCurrentUser()
    }

    private fun setUpRemoteConfig() {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
    }
}
