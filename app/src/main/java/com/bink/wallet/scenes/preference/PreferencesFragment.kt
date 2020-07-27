package com.bink.wallet.scenes.preference

import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.PreferencesFragmentBinding
import com.bink.wallet.model.request.Preference
import com.bink.wallet.utils.FirebaseEvents.PREFERENCES_VIEW
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.observeErrorNonNull
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PreferencesFragment : BaseFragment<PreferencesViewModel, PreferencesFragmentBinding>() {

    override val viewModel: PreferencesViewModel by viewModel()

    override val layoutRes = R.layout.preferences_fragment

    override fun onResume() {
        super.onResume()
        logScreenView(PREFERENCES_VIEW)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.progressSpinner.visibility = View.VISIBLE

        binding.preferenceDescription.text = HtmlCompat.fromHtml(
            getString(R.string.preference_description),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        viewModel.savePreferenceError.observeErrorNonNull(requireContext(), true, this)

        viewModel.preferences.observeNonNull(this) { preferences ->
            binding.preferenceError.visibility = View.GONE
            binding.progressSpinner.visibility = View.GONE
            binding.preferencesRecycler.apply {
                adapter = PreferenceAdapter(
                    preferences,
                    onClickListener = { preference: Preference, isChecked: Boolean, _ ->
                        val state = if (isChecked) 1 else 0
                        viewModel.savePreference(
                           requestBody = JSONObject().put(
                               preference.slug!!,
                               state
                           ).toString()
                        )
                    })
                layoutManager = GridLayoutManager(requireContext(), 1)
            }
        }

        viewModel.preferenceErrorResponse.observeNonNull(this) {
            if (isNetworkAvailable(requireContext(), true)) {
                binding.preferenceError.visibility = View.VISIBLE
            }
            binding.progressSpinner.visibility = View.GONE
        }

        if (isNetworkAvailable(requireContext(), true)) {
            viewModel.getPreferences()
        } else {
            binding.progressSpinner.visibility = View.GONE
        }
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }
}
