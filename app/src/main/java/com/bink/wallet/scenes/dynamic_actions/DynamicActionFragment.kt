package com.bink.wallet.scenes.dynamic_actions

import android.os.Bundle
import android.view.View
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.DynamicActionFragmentBinding
import com.bink.wallet.model.DynamicActionEventBodyCTAHandler
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class DynamicActionFragment : BaseFragment<DynamicActionViewModel, DynamicActionFragmentBinding>() {

    override val viewModel: DynamicActionViewModel by viewModel()

    override val layoutRes = R.layout.dynamic_action_fragment

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        arguments?.let { bundle ->
            DynamicActionFragmentArgs.fromBundle(bundle).apply {
                dynamicActionEvent.body?.let { body ->
                    binding.title.text = body.title
                    binding.description.text = body.description

                    body.cta?.let { cta ->
                        binding.firstButton.text = cta.title
                        binding.firstButton.visibility = View.VISIBLE

                        cta.action?.let { action ->
                            binding.firstButton.setOnClickListener {
                                launchDynamicActionEventCta(action)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun launchDynamicActionEventCta(action: DynamicActionEventBodyCTAHandler){
        when(action) {
            DynamicActionEventBodyCTAHandler.ZENDESK_CONTACT_US -> {
                viewModel.launchZendesk(this){}
            }
        }
    }

}