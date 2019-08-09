package com.bink.wallet.scenes.add

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddFragmentBinding
import kotlinx.android.synthetic.main.add_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddFragment : BaseFragment<AddViewModel, AddFragmentBinding>() {

    companion object {
        fun newInstance() = AddFragment()
    }

    override val layoutRes: Int
        get() = R.layout.add_fragment

    override val viewModel: AddViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        cancel_button.setOnClickListener { findNavController().navigate(R.id.add_to_loyalty) }
        browse_brands_container.setOnClickListener { findNavController().navigate(R.id.add_to_browse) }
    }

}
