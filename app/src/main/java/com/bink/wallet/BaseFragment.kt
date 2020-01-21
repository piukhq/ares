package com.bink.wallet

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.WindowFullscreenHandler
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.hideKeyboard
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.bink.wallet.utils.toolbar.ToolbarManager
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.net.HttpURLConnection

abstract class BaseFragment<VM : BaseViewModel, DB : ViewDataBinding> : Fragment() {

    @get:LayoutRes
    abstract val layoutRes: Int

    abstract val viewModel: VM

    open lateinit var binding: DB

    open val windowFullscreenHandler: WindowFullscreenHandler by inject {
        parametersOf(
            requireActivity()
        )
    }

    open fun init(inflater: LayoutInflater, container: ViewGroup) {
        binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)
    }

    open fun init() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        container?.let {
            init(inflater, container)
        }
        init()
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        try {
            viewModel.errorCode.observe(viewLifecycleOwner, Observer {
                if (it == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    LocalStoreUtils.clearPreferences()

                    Navigation.findNavController(binding.root).navigate(R.id.global_to_onboarding)
                    viewModel.errorCode.removeObservers(this)
                }
            })
        } catch (e: Exception) {
            Log.e(BaseFragment::class.simpleName, e.toString())
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (findNavController().currentDestination?.label != getString(R.string.root)) {
                        view?.hideKeyboard()
                        windowFullscreenHandler.toNormalScreen()
                        findNavController().popBackStack()
                    } else {
                        requireActivity().finish()
                    }
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolbarManager(builder()).prepareToolbar()
    }

    protected abstract fun builder(): FragmentToolbar
}