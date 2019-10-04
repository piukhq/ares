package com.bink.wallet.scenes.add_join

import android.os.Bundle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddJoinFragmentBinding
import com.bink.wallet.utils.enums.SignUpFormType
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.utils.enums.CardType
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddJoinFragment : BaseFragment<AddJoinViewModel, AddJoinFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.add_join_fragment

    private val args: AddJoinFragmentArgs by navArgs()

    override val viewModel: AddJoinViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val currentMembershipPlan = args.currentMembershipPlan
        binding.item = currentMembershipPlan

        when (currentMembershipPlan.feature_set?.card_type) {
            CardType.STORE.type -> {
                binding.addJoinViewImage.setImageDrawable(context?.getDrawable(R.drawable.ic_icons_svl_view_inactive))
                binding.addJoinViewDescription.text =
                    getString(R.string.add_join_inactive_view_description)
            }
            CardType.PLL.type -> {
                binding.addJoinLinkImage.setImageDrawable(context?.getDrawable(R.drawable.ic_icons_svl_link_inactive))
                binding.addJoinLinkDescription.text =
                    getString(R.string.add_join_inactive_link_description)
            }
        }

        binding.addCardButton.setOnClickListener {
            val action = AddJoinFragmentDirections.addJoinToGhost(
                SignUpFormType.ADD_AUTH,
                currentMembershipPlan,
                null
            )
            findNavController().navigateIfAdded(this, action)
        }

        binding.getCardButton.setOnClickListener {
            val action: NavDirections
            if (currentMembershipPlan.account?.enrol_fields!!.isEmpty()) {
                val genericModalParameters = GenericModalParameters(
                    R.drawable.ic_back,
                    getString(R.string.native_join_unavailable_title),
                    getString(R.string.native_join_unavailable_text)
                )
                if (currentMembershipPlan.account.plan_url!!.isNotEmpty()) {
                    genericModalParameters.firstButtonText =
                        getString(R.string.native_join_unavailable_button_text)
                    genericModalParameters.joinUnavailableLink =
                        currentMembershipPlan.account.plan_url
                }
                action = AddJoinFragmentDirections.addJoinToJoinUnavailable(genericModalParameters)
            } else {
                action = AddJoinFragmentDirections.addJoinToGhost(
                    SignUpFormType.ENROL,
                    currentMembershipPlan,
                    null)
            }
            findNavController().navigateIfAdded(this, action)
        }
    }
}
