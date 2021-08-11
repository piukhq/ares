package com.bink.wallet.scenes.sign_up.continue_with_email.magic_link_result

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.MagicLinkResultFragmentBinding
import com.bink.wallet.scenes.login.LoginFragmentDirections
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.logDebug
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class MagicLinkResultFragment : BaseFragment<MagicLinkResultViewModel, MagicLinkResultFragmentBinding>() {

    override val layoutRes = R.layout.magic_link_result_fragment

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: MagicLinkResultViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            val token = MagicLinkResultFragmentArgs.fromBundle(bundle).token
            viewModel.postMagicLinkToken(requireContext(), token)
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
            findNavController().navigate(LoginFragmentDirections.globalToHome(true))
        }

    }

    private fun showSuccessUi() {
        val subtitlePartOne = getString(R.string.magic_link_success_part_one)
        val subtitlePartTwo = getString(R.string.magic_link_success_part_two)
        val subtitleText = "$subtitlePartOne <b>${viewModel.email.value}</b> $subtitlePartTwo"

        with(binding) {
            title.text = getString(R.string.magic_link_success)
            subtitle.text = Html.fromHtml(subtitleText)

            continueButton.setOnClickListener {
                if (marketingCheckbox.isChecked) {
                    viewModel.postConsent()
                }

                viewModel.getMembershipPlans()
            }

            successLayout.visibility = View.VISIBLE
            animationView.playAnimation()
        }
    }

}