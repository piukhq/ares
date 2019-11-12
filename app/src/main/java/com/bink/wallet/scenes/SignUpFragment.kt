package com.bink.wallet.scenes

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.SignUpFragmentBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel


class SignUpFragment : BaseFragment<SignUpViewModel, SignUpFragmentBinding>() {

    override val layoutRes = R.layout.sign_up_fragment

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: SignUpViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sb =
            SpannableStringBuilder(getString(R.string.sign_up_footer_text))

        val bss = StyleSpan(Typeface.BOLD)
        sb.setSpan(bss, 50, 78, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        binding.signUpFooterMessage.text = sb

    }

}
