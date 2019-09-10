package com.bink.wallet.scenes.add

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddFragmentBinding
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.toolbar.FragmentToolbar
import kotlinx.android.synthetic.main.add_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddFragment : BaseFragment<AddViewModel, AddFragmentBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    companion object {
        fun newInstance() = AddFragment()
    }

    override val layoutRes: Int
        get() = R.layout.add_fragment

    override val viewModel: AddViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cancel_button.setOnClickListener { findNavController().navigateIfAdded(this, R.id.add_to_loyalty) }
        browse_brands_container.setOnClickListener {
            arguments?.let {
                val plans = AddFragmentArgs.fromBundle(it).membershipPlans
                val directions = AddFragmentDirections.addToBrowse(plans)
                findNavController().navigateIfAdded(this, directions)
            }
        }
    }

}
