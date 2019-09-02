package com.bink.wallet.scenes.loyalty_details

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.DialogSecurityBinding
import com.bink.wallet.databinding.FragmentLoyaltyCardDetailsBinding
import com.bink.wallet.model.response.membership_card.CardBalance
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsViewModel.Companion.STATUS_LOGGED_IN_HISTORY_AVAILABLE
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsViewModel.Companion.STATUS_LOGGED_IN_HISTORY_UNAVAILABLE
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsViewModel.Companion.STATUS_LOGIN_FAILED
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsViewModel.Companion.STATUS_LOGIN_PENDING
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsViewModel.Companion.STATUS_LOGIN_UNAVAILABLE
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsViewModel.Companion.STATUS_NOT_LOGGED_IN_HISTORY_AVAILABLE
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsViewModel.Companion.STATUS_NOT_LOGGED_IN_HISTORY_UNAVAILABLE
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsViewModel.Companion.STATUS_REGISTER_GHOST_CARD_FAILED
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsViewModel.Companion.STATUS_REGISTER_GHOST_CARD_PENDING
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsViewModel.Companion.STATUS_SIGN_UP_FAILED
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsViewModel.Companion.STATUS_SIGN_UP_PENDING
import com.bink.wallet.utils.displayModalPopup
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class LoyaltyCardDetailsFragment: BaseFragment<LoyaltyCardDetailsViewModel, FragmentLoyaltyCardDetailsBinding>() {

    override val viewModel: LoyaltyCardDetailsViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.fragment_loyalty_card_details

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.ic_close)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateIfAdded(this, R.id.detail_to_home)
        }

        arguments?.let {
            viewModel.membershipPlan.value =
                LoyaltyCardDetailsFragmentArgs.fromBundle(it).membershipPlan
            val tiles = arrayListOf<String>()
            viewModel.membershipPlan.value?.images?.filter { image -> image.type == 2 }
                ?.forEach { image -> tiles.add(image.url.toString()) }
            viewModel.tiles.value = tiles
            viewModel.membershipCard.value =
                LoyaltyCardDetailsFragmentArgs.fromBundle(it).membershipCard
            binding.viewModel = viewModel
            viewModel.setAccountStatus()
        }

        binding.offerTiles.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.offerTiles.adapter = viewModel.tiles.value?.let { LoyaltyDetailsTilesAdapter(it) }

        binding.footerAbout.setOnClickListener {
            viewModel.membershipPlan.value?.account?.plan_description?.let { it1 ->
                context?.displayModalPopup(
                    "",
                    it1
                )
            }
        }

        if (!viewModel.membershipCard.value?.card?.barcode.isNullOrEmpty()) {
            binding.cardHeader.setOnClickListener {
                val directions = viewModel.membershipCard.value?.card?.barcode_type.let { type ->
                    viewModel.membershipPlan.value?.let { plan ->
                        type?.let { it1 ->
                            LoyaltyCardDetailsFragmentDirections.detailToBarcode(
                                plan, viewModel.membershipCard.value?.card?.barcode,
                                it1
                            )
                        }
                    }
                }

                directions?.let { findNavController().navigateIfAdded(this, it) }
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

        viewModel.accountStatus.observeNonNull(this) { status ->
            when (status) {
                STATUS_LOGGED_IN_HISTORY_AVAILABLE -> {
                    binding.pointsImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_active
                        )
                    )
                    binding.pointsDescription.text = resources.getText(R.string.view_history)
                    val balance = viewModel.membershipCard.value?.balances?.first()
                    setBalanceText(balance)
                }
                STATUS_LOGGED_IN_HISTORY_UNAVAILABLE -> {
                    binding.pointsImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_active
                        )
                    )
                    val balance = viewModel.membershipCard.value?.balances?.first()
                    setBalanceText(balance)

                    val time = balance?.updated_at?.toLong()?.let { Date(it) }
                    binding.pointsDescription.text =
                        SimpleDateFormat("ddMMyyyy", Locale.getDefault()).toLocalizedPattern()
                            .format(time)
                }
                STATUS_NOT_LOGGED_IN_HISTORY_AVAILABLE,
                STATUS_NOT_LOGGED_IN_HISTORY_UNAVAILABLE -> {
                    binding.pointsText.text = resources.getString(R.string.points_login)
                    binding.pointsDescription.text =
                        resources.getString(R.string.description_see_history)
                    binding.pointsImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_login
                        )
                    )
                }
                STATUS_LOGIN_UNAVAILABLE -> {
                    binding.pointsText.text = resources.getString(R.string.points_history)
                    binding.pointsDescription.text =
                        resources.getString(R.string.description_not_available)
                    binding.pointsImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_inactive
                        )
                    )
                }

                STATUS_LOGIN_FAILED -> {
                    binding.pointsImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_login
                        )
                    )
                    binding.pointsText.text = resources.getString(R.string.points_retry_login)
                    binding.pointsDescription.text =
                        resources.getString(R.string.description_see_history)
                }

                STATUS_LOGIN_PENDING -> {
                    binding.pointsImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_pending
                        )
                    )
                    binding.pointsText.text = resources.getString(R.string.points_logging_in)
                    binding.pointsDescription.text =
                        resources.getString(R.string.description_please_wait)
                }

                STATUS_SIGN_UP_FAILED -> {
                    binding.pointsImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_login
                        )
                    )
                    binding.pointsText.text = resources.getString(R.string.points_sign_up_failed)
                    binding.pointsDescription.text =
                        resources.getString(R.string.description_please_try_again)
                }

                STATUS_SIGN_UP_PENDING -> {
                    binding.pointsImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_pending
                        )
                    )
                    binding.pointsText.text = resources.getString(R.string.points_signing_up)
                    binding.pointsDescription.text =
                        resources.getString(R.string.description_please_wait)
                }

                STATUS_REGISTER_GHOST_CARD_FAILED -> {
                    binding.pointsImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_login
                        )
                    )
                    binding.pointsText.text =
                        resources.getString(R.string.points_registration_failed)
                    binding.pointsDescription.text =
                        resources.getString(R.string.description_please_try_again)
                }
                STATUS_REGISTER_GHOST_CARD_PENDING -> {
                    binding.pointsImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_pending
                        )
                    )
                    binding.pointsText.text = resources.getString(R.string.points_registering_card)
                    binding.pointsDescription.text =
                        resources.getString(R.string.description_please_wait)
                }
            }
        }


        binding.footerDelete.setOnClickListener { footerView ->
            val builder = AlertDialog.Builder(context)
            var dialog: AlertDialog? = null
            builder.setMessage(getString(R.string.delete_card_modal_body))
            builder.setNeutralButton(getString(R.string.no_text)) { _, _ -> }
            builder.setPositiveButton(getString(R.string.yes_text)) { _, _ ->
                runBlocking {
                    viewModel.deleteCard(viewModel.membershipCard.value?.id)
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

    private fun setBalanceText(balance: CardBalance?) {
        balance?.prefix?.let { prefix ->
            if (balance.suffix.isNullOrEmpty()) {
                binding.pointsText.text =
                    resources.getString(R.string.points_prefix_or_suffix, prefix, balance.value)
            } else {
                binding.pointsText.text = resources.getString(
                    R.string.points_prefix_and_suffix,
                    prefix,
                    balance.value,
                    balance.suffix
                )
            }
        }
        balance?.suffix?.let { suffix ->
            if (balance.prefix.isNullOrEmpty()) {
                binding.pointsText.text =
                    resources.getString(R.string.points_prefix_or_suffix, balance.value, suffix)
            }
        }
    }
}