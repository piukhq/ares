package com.bink.wallet.scenes.sign_up.continue_with_email.magic_link_result

import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Toolbar
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.MagicLinkResultFragmentBinding
import com.bink.wallet.scenes.login.LoginFragmentDirections
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.MixpanelEvents
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MagicLinkResultFragment :
    BaseFragment<MagicLinkResultViewModel, MagicLinkResultFragmentBinding>() {

    override val layoutRes = R.layout.magic_link_result_fragment

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(Toolbar(requireContext()))
            .build()
    }

    override val viewModel: MagicLinkResultViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            val token = MagicLinkResultFragmentArgs.fromBundle(bundle).token
            val isLogoutNeeded = MagicLinkResultFragmentArgs.fromBundle(bundle).isLogoutNeeded
            viewModel.token = token

            if (isLogoutNeeded) {
                viewModel.logOut()
            } else {
                viewModel.postMagicLinkToken(requireContext(), token)
            }
        }

        viewModel.user.observeNonNull(this) {
            setAnalyticsUserId(it.uid)
            showSuccessUi()
        }

        viewModel.email.observeNonNull(this) {
            LocalStoreUtils.setAppSharedPref(
                LocalStoreUtils.KEY_EMAIL,
                it
            )
        }

        viewModel.isLoading.observeNonNull(this) {
            with(binding) {
                progressSpinner.visibility = when (it) {
                    true -> {
                        View.VISIBLE
                    }
                    else -> {
                        View.GONE
                    }
                }
            }
        }

        viewModel.membershipPlans.observeNonNull(this) {
            logMixpanelEvent(MixpanelEvents.LOGIN, JSONObject().put(MixpanelEvents.METHOD, MixpanelEvents.LOGIN_ML))
            findNavController().navigate(LoginFragmentDirections.globalToHome(true))
        }

        viewModel.hasLoggedOut.observeNonNull(this) { hasLoggedOut ->
            if (hasLoggedOut) {
                viewModel.token?.let {
                    viewModel.postMagicLinkToken(requireContext(), it)
                }
            }
        }

        viewModel.hasErrorWithExpiry.observeNonNull(this) {
            showErrorUi(it)
        }

    }

    private fun showSuccessUi() {
        val subtitlePartOne = getString(R.string.magic_link_success_part_one)
        val subtitlePartTwo = getString(R.string.magic_link_success_part_two)
        val subtitleText = "$subtitlePartOne<b>${viewModel.email.value}</b>$subtitlePartTwo"

        with(binding) {
            title.text = getString(R.string.magic_link_success)
            subtitle.text = Html.fromHtml(subtitleText)

            continueButton.setOnClickListener {
                viewModel.postConsent()
                viewModel.putMarketingPref(marketingCheckbox.isChecked)
                viewModel.getMembershipPlans()
            }

            successLayout.visibility = View.VISIBLE
            animationView.playAnimation()
        }
    }

    private fun showErrorUi(isExpired: Boolean) {
        LocalStoreUtils.clearPreferences(requireContext())

        with(binding) {
            errorTitle.text =
                if (isExpired) getString(R.string.magic_link_expiry_title) else getString(R.string.magic_link_error_title)
            errorSubtitle.text =
                if (isExpired) getString(R.string.magic_link_expiry_subtitle) else getString(R.string.magic_link_error_subtitle)

            errorRetry.setOnClickListener {
                if (isExpired) {
                    findNavController().navigateIfAdded(
                        this@MagicLinkResultFragment,
                        MagicLinkResultFragmentDirections.magicLinkResultToCheckInbox(
                            viewModel.email.value ?: "", true
                        )
                    )
                } else {
                    errorLayout.visibility = View.GONE
                    viewModel.postMagicLinkToken(requireContext(), viewModel.token ?: "")
                }
            }

            errorCancel.setOnClickListener {
                findNavController().navigateIfAdded(
                    this@MagicLinkResultFragment,
                    MagicLinkResultFragmentDirections.globalToOnboarding(),
                    R.id.magic_link_result_fragment
                )
            }

            errorLayout.visibility = View.VISIBLE
        }
    }

}