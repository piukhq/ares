package com.bink.wallet.scenes.registration

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AcceptTcFragmentBinding
import com.bink.wallet.model.auth.FacebookAuthRequest
import com.bink.wallet.model.request.MarketingOption
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.TERMS_AND_CONDITIONS_VIEW
import com.bink.wallet.utils.FirebaseEvents.getFirebaseIdentifier
import com.bink.wallet.utils.enums.MarketingOptions.MARKETING_OPTION_NO
import com.bink.wallet.utils.enums.MarketingOptions.MARKETING_OPTION_YES
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import io.fabric.sdk.android.services.common.CommonUtils.hideKeyboard
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class AcceptTCFragment : BaseFragment<AcceptTCViewModel, AcceptTcFragmentBinding>() {
    override val layoutRes: Int
        get() = R.layout.accept_tc_fragment

    override val viewModel: AcceptTCViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .build()
    }

    private lateinit var termsAndConditionsHyperlink: String
    private lateinit var privacyPolicyHyperlink: String
    private lateinit var boldedTexts: Array<String>
    private var userEmail: String? = null
    private var accessToken: AccessToken? = null

    override fun onResume() {
        super.onResume()
        logScreenView(TERMS_AND_CONDITIONS_VIEW)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        termsAndConditionsHyperlink = getString(R.string.terms_conditions_text)
        privacyPolicyHyperlink = getString(R.string.privacy_policy_text)
        boldedTexts = resources.getStringArray(R.array.terms_bold_text_array)

        arguments?.let {
            with(AcceptTCFragmentArgs.fromBundle(it)) {
                userEmail = email
                this@AcceptTCFragment.accessToken = accessToken
            }
        }

        binding.acceptTc.movementMethod = LinkMovementMethod.getInstance()

        viewModel.facebookAuthError.observeNonNull(this) {
            binding.accept.isClickable = false
            val timer = Timer()
            context?.resources?.getInteger(R.integer.button_disabled_delay)?.toLong()
                ?.let { delay ->
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            binding.accept.isClickable = true
                        }
                    }, delay)
                }
            if (UtilFunctions.isNetworkAvailable(requireContext(), true)) {
                requireContext().displayModalPopup(getString(R.string.facebook_failed), null)
            }
        }

        viewModel.facebookAuthResult.observeNonNull(this) {
            runBlocking {
                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_TOKEN,
                    getString(R.string.token_api_v1, it.api_key)
                )
            }
            if (binding.acceptMarketing.isChecked) {
                viewModel.handleMarketingPreferences(
                    MarketingOption(MARKETING_OPTION_YES.selected)
                )
            } else {
                viewModel.handleMarketingPreferences(
                    MarketingOption(
                        (MARKETING_OPTION_NO.selected)
                    )
                )
            }
            viewModel.getMembershipPlans()
        }

        binding.accept.setOnClickListener {
            accessToken?.token?.let { token ->
                accessToken?.userId?.let { userId ->
                    userEmail?.let { email ->
                        viewModel.authWithFacebook(
                            FacebookAuthRequest(
                                token,
                                email,
                                userId
                            )
                        )
                    }
                }
            }

            logEvent(
                getFirebaseIdentifier(
                    TERMS_AND_CONDITIONS_VIEW,
                    binding.accept.text.toString()
                )
            )
        }

        binding.back.setOnClickListener {
            LoginManager.getInstance().logOut()
            hideKeyboard(requireContext(), binding.root)
            findNavController().navigateIfAdded(this, R.id.accept_to_onboarding)
        }

        initMembershipPlansObserver()
    }

    private fun initMembershipPlansObserver() {
        viewModel.membershipPlanDatabaseLiveData.observeNonNull(this@AcceptTCFragment) {
            finishLogInProcess()
        }

        viewModel.membershipPlanErrorLiveData.observeNonNull(this@AcceptTCFragment) {
            finishLogInProcess()
        }
    }

    private fun finishLogInProcess() {
        findNavController().navigateIfAdded(this, R.id.accept_to_lcd)
    }
}