package com.bink.wallet.scenes.preference

import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.PreferencesFragmentBinding
import com.bink.wallet.model.request.Preference
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PreferencesFragment : BaseFragment<PreferencesViewModel, PreferencesFragmentBinding>() {

    override val viewModel: PreferencesViewModel by viewModel()

    override val layoutRes = R.layout.preferences_fragment

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.preferenceDescription.text = HtmlCompat.fromHtml(
            getString(R.string.preference_description),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        viewModel.savePreferenceError.observeNonNull(this) {
            requireContext().displayModalPopup(
                EMPTY_STRING,
                getString(R.string.preference_update_error)
            )
        }

        viewModel.preferences.observeNonNull(this) { preferences ->
            binding.progressSpinner.visibility = View.GONE
            binding.preferencesRecycler.apply {
                adapter = PreferenceAdapter(
                    preferences,
                    onClickListener = { preference: Preference, state: Int, checkBox ->
                        if (isNetworkAvailable(requireActivity(), true)) {
                            viewModel.savePreference(
                                json = JSONObject().put(
                                    preference.slug!!,
                                    state
                                ).toString()
                            )
                        } else {
                            checkBox.isChecked = !checkBox.isChecked
                            requireContext().displayModalPopup(
                                EMPTY_STRING,
                                getString(R.string.no_internet_connection_dialog_message)
                            )
                        }
                    })
                layoutManager = GridLayoutManager(requireContext(), 1)
            }
        }

        viewModel.preferenceErrorResponse.observeNonNull(this) {
            if (isNetworkAvailable(requireContext(), true)) {
                requireContext().displayModalPopup(
                    EMPTY_STRING,
                    getString(
                        R.string.preferences_error
                    )
                )
            }
            binding.progressSpinner.visibility = View.GONE
        }

        viewModel.getPreferences()
        binding.progressSpinner.visibility = View.VISIBLE
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }
}
