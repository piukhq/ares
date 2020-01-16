package com.bink.wallet.scenes.add

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddFragmentBinding
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toDipFromPixel
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.constraintlayout.widget.ConstraintSet



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

    private fun setCardMarginRelativeToButton() {
        val lastCardHeight = binding.paymentCardContainer.height
        var marginInDp = requireContext().toDipFromPixel(((75 * lastCardHeight) / 100).toFloat())

        val constraintSet = ConstraintSet()
//        constraintSet.clone(binding.root)
//        constraintSet.connect(R.id.imageView,ConstraintSet.TOP,R.id.check_answer1,ConstraintSet.TOP,0)
//        constraintSet.applyTo(binding.root)

    }
}
