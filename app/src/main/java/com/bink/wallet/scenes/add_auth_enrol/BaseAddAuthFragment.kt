package com.bink.wallet.scenes.add_auth_enrol

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.BaseAddAuthFragmentBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.utils.hideKeyboard
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

open class BaseAddAuthFragment : BaseFragment<AddAuthViewModel, BaseAddAuthFragmentBinding>() {

    override val layoutRes: Int
        get() = R.layout.base_add_auth_fragment
    override val viewModel: AddAuthViewModel by viewModel()

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .build()
    }

    private val args: BaseAddAuthFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.membershipPlan = args.membershipPlan
        binding.viewModel = viewModel
        binding.footerSimple.viewModel = viewModel
        binding.footerComposed.viewModel = viewModel

        SharedPreferenceManager.isLoyaltySelected = true

        binding.toolbar.setNavigationOnClickListener {
            handleToolbarAction()
            findNavController().navigateUp()
        }

        binding.buttonCancel.setOnClickListener {
            handleToolbarAction()
            findNavController().navigate(AddAuthFragmentDirections.globalToHome())
        }

        binding.addJoinReward.setOnClickListener {
          //  navigateToBrandHeader()
        }
    }

    //TODO move this in children
    private fun navigateToBrandHeader() {
        binding.membershipPlan?.let { plan ->
            if (plan.account?.plan_description != null) {
                findNavController().navigateIfAdded(
                    this,
                    BaseAddAuthFragmentDirections.baseAddAuthToBrandHeader(
                        GenericModalParameters(
                            R.drawable.ic_close,
                            true,
                            plan.account.plan_name
                                ?: getString(R.string.plan_description),
                            plan.account.plan_description
                        )
                    )
                )
            } else if (plan.account?.plan_name_card != null) {
                plan.account.plan_name?.let { planName ->
                    findNavController().navigateIfAdded(
                        this,
                        BaseAddAuthFragmentDirections.baseAddAuthToBrandHeader(
                            GenericModalParameters(
                                R.drawable.ic_close,
                                true,
                                planName
                            )
                        )
                    )
                }
            }
        }
    }

    private fun handleToolbarAction() {
        view?.hideKeyboard()
        windowFullscreenHandler.toNormalScreen()
    }
}