package com.bink.wallet.scenes.rooted_device_screen

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bink.wallet.R
import com.bink.wallet.databinding.RootedDeviceFragmentBinding

class RootedDeviceFragment : Fragment() {

    private var _binding: RootedDeviceFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = RootedDeviceFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.binkLogo.setImageResource(R.drawable.ic_logo_dark)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.binkLogo.setImageResource(R.drawable.ic_logo_light)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
