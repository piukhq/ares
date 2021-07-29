package com.bink.wallet.scenes.sign_up.continue_with_email.check_inbox

import android.content.Intent
import android.os.Bundle
import android.text.Html
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.CheckInboxFragmentBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel


class CheckInboxFragment : BaseFragment<CheckInboxViewModel, CheckInboxFragmentBinding>() {

    override val layoutRes = R.layout.check_inbox_fragment

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: CheckInboxViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            val email = CheckInboxFragmentArgs.fromBundle(bundle).userEmail
            setSubtitle(email)
        }

        binding.goToInbox.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_EMAIL)
            startActivity(intent)
        }

    }

    private fun setSubtitle(email: String) {
        val subtitlePartOne = getString(R.string.check_inbox_subtitle_part_1)
        val subtitlePartTwo = getString(R.string.check_inbox_subtitle_part_2)
        val subtitle = "$subtitlePartOne <b>$email.</b> $subtitlePartTwo"
        binding.subtitle.text = Html.fromHtml(subtitle)
    }


}