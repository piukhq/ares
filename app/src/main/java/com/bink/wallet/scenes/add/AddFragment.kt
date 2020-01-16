package com.bink.wallet.scenes.add

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddFragmentBinding
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel


class AddFragment : BaseFragment<AddViewModel, AddFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.add_fragment

    override val viewModel: AddViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.cancelButton.setOnClickListener {
            findNavController().navigateIfAdded(
                this,
                R.id.global_to_home
            )
        }
        binding.browseBrandsContainer.setOnClickListener {
            arguments?.let {
                val plans = AddFragmentArgs.fromBundle(it).membershipPlans
                val directions = AddFragmentDirections.addToBrowse(plans)
                findNavController().navigateIfAdded(this, directions)
            }
        }
        binding.paymentCardContainer.setOnClickListener {
            findNavController().navigateIfAdded(this, R.id.add_to_pcd)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.paymentCardContainer.viewTreeObserver.addOnGlobalLayoutListener {
           // binding.paymentCardContainer.viewTreeObserver.removeOnGlobalLayoutListener()
            setCardMarginRelativeToButton()
        }
    }

    private fun setCardMarginRelativeToButton() {
        val lastCardHeight = binding.paymentCardContainer.height

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.root)
        constraintSet.connect(
            R.id.payment_card_container,
            ConstraintSet.BOTTOM,
            R.id.cancel_button,
            ConstraintSet.TOP,
            75 * lastCardHeight / 100
        )
        constraintSet.applyTo(binding.root)

    }
}
