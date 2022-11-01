package com.bink.wallet.scenes.preference

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.PreferencesFragmentBinding
import com.bink.wallet.model.request.Preference
import com.bink.wallet.utils.*
import com.bink.wallet.utils.FirebaseEvents.PREFERENCES_VIEW
import com.bink.wallet.utils.UtilFunctions.isNetworkAvailable
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
                        promptPreferenceClear(preference.slug!!, isChecked)
                        if (preference.slug == ALWAYS_SHOW_BARCODE_KEY) {
                            setMixpanelProperty(
                                MixpanelEvents.FORCE_BARCODE,
                                isChecked.toString()
                            )
                        }
                        val state = if (isChecked) 1 else 0
                        viewModel.savePreference(
                            requestBody = JSONObject().put(
                                preference.slug,
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

    private fun promptPreferenceClear(preferenceSlug: String, isChecked: Boolean) {
        if ((preferenceSlug == REMEMBER_DETAILS_KEY || preferenceSlug == CLEAR_PREF_KEY) && !isChecked) {
            lateinit var dialog: AlertDialog
            val builder = context?.let { AlertDialog.Builder(it) }
            if (builder != null) {
                builder.setTitle(getString(R.string.clear_stored_cred_title))
                builder.setMessage(getString(R.string.clear_stored_cred_message))
                val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            REMEMBERABLE_FIELD_NAMES.forEach { fieldName ->
                                LocalStoreUtils.removeKey(fieldName)
                            }

                            if (isNetworkAvailable(requireContext(), true)) {
                                viewModel.getPreferences()
                            }
                        }
                        DialogInterface.BUTTON_NEUTRAL -> {
                            dialog.cancel()
                        }
                    }
                }
                builder.setPositiveButton(getString(R.string.ok), dialogClickListener)
                builder.setNeutralButton(getString(R.string.cancel_text_upper), dialogClickListener)
                dialog = builder.create()
                dialog.show()
            }
        }
    }

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }
}
