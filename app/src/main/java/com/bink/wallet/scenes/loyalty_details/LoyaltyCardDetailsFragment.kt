package com.bink.wallet.scenes.loyalty_details

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.DialogSecurityBinding
import com.bink.wallet.databinding.FragmentLoyaltyCardDetailsBinding
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoyaltyCardDetailsFragment: BaseFragment<LoyaltyCardDetailsViewModel, FragmentLoyaltyCardDetailsBinding>() {

    override val viewModel: LoyaltyCardDetailsViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.fragment_loyalty_card_details
    var membershipCard: MembershipCard? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.ic_close)
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        arguments?.let {
            viewModel.membershipPlan.value = LoyaltyCardDetailsFragmentArgs.fromBundle(it).membershipPlan
            membershipCard = LoyaltyCardDetailsFragmentArgs.fromBundle(it).membershipCard
        }

        binding.offerTiles.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.offerTiles.adapter = viewModel.tiles.value?.let { LoyaltyDetailsTilesAdapter(it) }

        binding.footerAbout.setOnClickListener {
            viewModel.membershipPlan.value?.account?.plan_description?.let { it1 ->
                context?.displayModalPopup(
                    "",
                    it1
                )
            }
        }



        binding.footerSecurity.setOnClickListener {
            val stringToSpan = resources.getString(R.string.security_modal_body_3)
            val spannableString = SpannableStringBuilder(stringToSpan)
            val url = "https://bink.com/terms-and-conditions/#privacy-policy"
            val hyperlinkText = resources.getString(R.string.hyperlink_text)
            spannableString.setSpan(
                URLSpan(url),
                stringToSpan.indexOf(hyperlinkText),
                stringToSpan.indexOf(hyperlinkText) + hyperlinkText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val dialog = Dialog(requireContext())
            val dialogBinding = DataBindingUtil.inflate<DialogSecurityBinding>(
                layoutInflater,
                R.layout.dialog_security,
                null,
                true
            )

            dialog.setContentView(dialogBinding.root)
            dialog.setTitle(R.string.security_modal_title)
            dialogBinding.preBody.text = getString(
                R.string.security_modal_body,
                getString(R.string.security_modal_body_1),
                getString(R.string.security_modal_body_2)
            )
            dialogBinding.body.text = spannableString
            dialogBinding.body.movementMethod = LinkMovementMethod.getInstance()
            dialogBinding.ok.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()

        }


        binding.footerDelete.setOnClickListener { footerView ->
            val builder = AlertDialog.Builder(context)
            var dialog: AlertDialog? = null
            builder.setMessage(getString(R.string.delete_card_modal_body))
            builder.setNeutralButton(getString(R.string.no_text)) { _, _ -> }
            builder.setPositiveButton(getString(R.string.yes_text)) { _, _ ->
                runBlocking {
                    viewModel.deleteCard(membershipCard?.id)
                }
                viewModel.deleteError.observeNonNull(this@LoyaltyCardDetailsFragment) { error ->
                    Snackbar.make(footerView, error, Snackbar.LENGTH_SHORT)
                    dialog?.dismiss()
                }
                viewModel.deletedCard.observeNonNull(this@LoyaltyCardDetailsFragment) {
                    dialog?.dismiss()
                    findNavController().navigateIfAdded(this, R.id.detail_to_home)
                }
            }
            dialog = builder.create()
            dialog.show()
        }
    }
}