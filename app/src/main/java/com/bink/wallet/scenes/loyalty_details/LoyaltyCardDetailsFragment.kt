package com.bink.wallet.scenes.loyalty_details

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.FragmentLoyaltyCardDetailsBinding
import com.bink.wallet.modal.generic.GenericModalParameters
import com.bink.wallet.model.response.membership_card.CardBalance
import com.bink.wallet.utils.enums.LinkStatus
import com.bink.wallet.utils.enums.LoginStatus
import com.bink.wallet.utils.enums.SignUpFormType
import com.bink.wallet.utils.getElapsedTime
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.observeNonNull
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.bink.wallet.utils.verifyAvailableNetwork
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class LoyaltyCardDetailsFragment :
    BaseFragment<LoyaltyCardDetailsViewModel, FragmentLoyaltyCardDetailsBinding>() {
    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(FragmentToolbar.NO_TOOLBAR)
            .build()
    }

    override val viewModel: LoyaltyCardDetailsViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.fragment_loyalty_card_details

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.ic_close)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateIfAdded(this, R.id.detail_to_home)
        }

        setLoadingState(true)

        runBlocking {
            viewModel.fetchPaymentCards()
        }

        viewModel.paymentCards.observeNonNull(this) {
            viewModel.setLinkStatus()
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
            runBlocking { viewModel.updateMembershipCard() }
            binding.viewModel = viewModel
            viewModel.setAccountStatus()
        }

        viewModel.updatedMembershipCard.observeNonNull(this) {
            viewModel.membershipCard.value = it
            binding.swipeLayout.isRefreshing = false
            viewModel.setAccountStatus()
            viewModel.setLinkStatus()
        }

        viewModel.membershipCard.observeNonNull(this) {
            binding.swipeLayout.isRefreshing = false
        }

        binding.offerTiles.layoutManager = LinearLayoutManager(context)

        binding.offerTiles.adapter = viewModel.tiles.value?.let { LoyaltyDetailsTilesAdapter(it) }

        binding.footerAbout.setOnClickListener {
            val directions =
                viewModel.membershipPlan.value?.account?.plan_description?.let { description ->
                    GenericModalParameters(
                        R.drawable.ic_close,
                        getString(R.string.about_membership_plan, viewModel.membershipPlan.value?.account!!.plan_name),
                        description, getString(R.string.ok)
                    )
                }?.let { arguments ->
                    LoyaltyCardDetailsFragmentDirections.detailToAbout(
                        arguments
                    )
                }
            directions?.let { _ -> findNavController().navigateIfAdded(this, directions) }
        }

        if (!viewModel.membershipCard.value?.card?.barcode.isNullOrEmpty()) {
            binding.cardHeader.setOnClickListener {
                val directions = viewModel.membershipCard.value?.card?.barcode_type.let { type ->
                    viewModel.membershipPlan.value?.let { plan ->
                        type?.let {
                            LoyaltyCardDetailsFragmentDirections.detailToBarcode(
                                plan, viewModel.membershipCard.value!!
                            )
                        }
                    }
                }

                directions?.let { findNavController().navigateIfAdded(this, directions) }
            }
        }
        binding.pointsWrapper.setOnClickListener {
            val genericModalParameters: GenericModalParameters?
            when (viewModel.accountStatus.value) {
                LoginStatus.STATUS_LOGGED_IN_HISTORY_AVAILABLE -> {
                    val action =
                        LoyaltyCardDetailsFragmentDirections.detailToTransactions(
                            viewModel.membershipCard.value!!,
                            viewModel.membershipPlan.value!!
                        )
                    findNavController().navigateIfAdded(this, action)
                }
                LoginStatus.STATUS_NOT_LOGGED_IN_HISTORY_UNAVAILABLE -> {
                    genericModalParameters = GenericModalParameters(
                        R.drawable.ic_close,
                        getString(R.string.title_1_2),
                        getString(
                            R.string.description_1_2,
                            viewModel.membershipPlan.value?.account?.plan_name
                        )
                    )
                    val action =
                        genericModalParameters.let { params ->
                            LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                                params
                            )
                        }
                    action.let { findNavController().navigateIfAdded(this, action) }
                }
                LoginStatus.STATUS_LOGIN_PENDING -> {
                    genericModalParameters = GenericModalParameters(
                        R.drawable.ic_close,
                        getString(R.string.title_1_7),
                        getString(R.string.description_1_7)
                    )
                    val action =
                        genericModalParameters.let { params ->
                            LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                                params
                            )
                        }
                    action.let { findNavController().navigateIfAdded(this, action) }
                }
                LoginStatus.STATUS_SIGN_UP_PENDING -> {
                    genericModalParameters = GenericModalParameters(
                        R.drawable.ic_close,
                        getString(R.string.title_1_9),
                        getString(R.string.description_1_9)
                    )
                    val action =
                        genericModalParameters.let { params ->
                            LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                                params
                            )
                        }
                    action.let { findNavController().navigateIfAdded(this, action) }
                }
                LoginStatus.STATUS_REGISTER_GHOST_CARD_PENDING -> {
                    genericModalParameters = GenericModalParameters(
                        R.drawable.ic_close,
                        getString(R.string.title_1_11),
                        getString(R.string.description_1_11)
                    )
                    val action =
                        genericModalParameters.let { params ->
                            LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                                params
                            )
                        }
                    action.let { findNavController().navigateIfAdded(this, action) }
                }
                LoginStatus.STATUS_NOT_LOGGED_IN_HISTORY_AVAILABLE,
                LoginStatus.STATUS_LOGIN_FAILED -> {
                    val action =
                        LoyaltyCardDetailsFragmentDirections.detailToAuth(
                            SignUpFormType.ADD_AUTH,
                            viewModel.membershipPlan.value!!,
                            viewModel.membershipCard.value!!
                        )
                    findNavController().navigateIfAdded(this, action)
                }
                else -> {
                }
            }
        }

        binding.footerSecurity.setOnClickListener {
            val directions = LoyaltyCardDetailsFragmentDirections.detailToSecurity(
                GenericModalParameters(
                    R.drawable.ic_close,
                    getString(R.string.security_modal_title),
                    getString(
                        R.string.security_modal_body,
                        getString(R.string.security_modal_body_1),
                        getString(R.string.security_modal_body_2)
                                + getString(R.string.security_modal_body_3)
                    ),
                    getString(R.string.ok)
                )
            )
            findNavController().navigateIfAdded(this, directions)
        }

        viewModel.linkStatus.observeNonNull(this) { status ->
            if (viewModel.accountStatus.value != null && viewModel.paymentCards.value != null) {
                setLoadingState(false)
            } else {
                runBlocking {
                    viewModel.fetchPaymentCards()
                }
            }
            when(status){
                LinkStatus.STATUS_LINKED_TO_SOME_OR_ALL -> {
                    binding.activeLinked.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_active_linked
                        )
                    )
                    binding.linkStatusText.text = getString(R.string.link_status_linked)
                    var linkedCards = 0
                    viewModel.membershipCard.value?.payment_cards?.forEach {
                        if (it.active_link == true)
                            linkedCards++
                    }
                    binding.linkDescription.text = getString(
                        R.string.description_linked,
                        linkedCards,
                        viewModel.paymentCards.value?.size
                    )
                    binding.linkedWrapper.setOnClickListener {
                        val directions =
                            viewModel.membershipCard.value?.let { membershipCard ->
                                viewModel.membershipPlan.value?.let { membershipPlan ->
                                    LoyaltyCardDetailsFragmentDirections.detailToPll(
                                        membershipCard, membershipPlan, false
                                    )
                                }

                            }
                        directions?.let { _ -> findNavController().navigateIfAdded(this, directions) }
                    }
                }
                LinkStatus.STATUS_LINKABLE_NO_PAYMENT_CARDS -> {
                    binding.activeLinked.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_link_error
                        )
                    )
                    binding.linkDescription.text =
                        getString(R.string.description_no_cards)
                    binding.linkStatusText.text =
                        getString(R.string.link_status_linkable_no_cards)
                    val directions = viewModel.membershipPlan.value?.let { membershipPlan ->
                        viewModel.membershipCard.value?.let { membershipCard ->
                            LoyaltyCardDetailsFragmentDirections.detailToPllEmpty(
                                membershipPlan, membershipCard
                            )
                        }
                    }
                    binding.linkedWrapper.setOnClickListener {
                        if (directions != null) {
                            findNavController().navigateIfAdded(this, directions)
                        }
                    }
                }
                LinkStatus.STATUS_LINKABLE_NO_PAYMENT_CARDS_LINKED -> {
                    binding.activeLinked.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_link_error
                        )
                    )
                    binding.linkDescription.text =
                        getString(R.string.description_no_cards)
                    binding.linkStatusText.text =
                        getString(R.string.link_status_linkable_no_cards)

                    binding.linkedWrapper.setOnClickListener {
                        val directions =
                            viewModel.membershipCard.value?.let { membershipCard ->
                                viewModel.membershipPlan.value?.let { membershipPlan ->
                                    LoyaltyCardDetailsFragmentDirections.detailToPll(
                                        membershipCard, membershipPlan, false
                                    )
                                }

                            }
                        directions?.let { _ -> findNavController().navigateIfAdded(this, directions) }
                    }

                }
                LinkStatus.STATUS_LINKABLE_GENERIC_ERROR -> {
                    binding.activeLinked.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_link_error
                        )
                    )
                    binding.linkStatusText.text =
                        getString(R.string.link_status_link_error)
                    binding.linkDescription.text = getString(R.string.description_error)
                    binding.linkedWrapper.setOnClickListener {
                        val directions =
                            LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                                GenericModalParameters(
                                    R.drawable.ic_close,
                                    getString(R.string.title_2_4),
                                    getString(R.string.description_2_4)
                                )
                            )
                        findNavController().navigateIfAdded(this, directions)
                    }
                }
                LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH -> {
                    binding.activeLinked.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_login
                        )
                    )
                    binding.linkStatusText.text =
                        getString(R.string.link_status_requires_auth)
                    binding.linkDescription.text =
                        getString(R.string.description_requires_auth)
                }

                LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH_PENDING -> {
                    binding.activeLinked.setImageDrawable(
                        getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_pending
                        )
                    )
                    binding.linkStatusText.text =
                        getString(R.string.link_status_requires_auth_pending)
                    binding.linkDescription.text =
                        getString(R.string.description_requires_auth_pending)
                    binding.linkedWrapper.setOnClickListener {
                        val directions =
                            LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                                GenericModalParameters(
                                    R.drawable.ic_close,
                                    getString(R.string.title_1_7),
                                    getString(R.string.description_1_7)
                                )
                            )
                        findNavController().navigateIfAdded(this, directions)
                    }
                }

                LinkStatus.STATUS_LINKABLE_REQUIRES_AUTH_PENDING_FAILED -> {
                    binding.activeLinked.setImageDrawable(
                        getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_login
                        )
                    )
                    binding.linkStatusText.text =
                        getString(R.string.link_status_auth_failed)
                    binding.linkDescription.text =
                        getString(R.string.description_auth_failed)
                }

                LinkStatus.STATUS_UNLINKABLE -> {
                    binding.activeLinked.setImageDrawable(
                        getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_link_inactive
                        )
                    )
                    binding.linkStatusText.text =
                        getString(R.string.link_status_unlinkable)
                    binding.linkDescription.text = getString(R.string.description_unlinkable)
                    binding.linkedWrapper.setOnClickListener {
                        val directions =
                            LoyaltyCardDetailsFragmentDirections.detailToErrorModal(
                                GenericModalParameters(
                                    R.drawable.ic_close,
                                    getString(R.string.title_2_8),
                                    getString(R.string.description_2_8)
                                )
                            )
                        findNavController().navigateIfAdded(this, directions)
                    }
                }
            }
        }

        binding.swipeLayout.setOnRefreshListener {
            runBlocking {
                viewModel.updateMembershipCard()
            }
        }

        viewModel.accountStatus.observeNonNull(this) { status ->
            when (status) {
                LoginStatus.STATUS_LOGGED_IN_HISTORY_AVAILABLE -> {
                    binding.pointsImage.setImageDrawable(
                        getDrawable(
                            requireContext(),
                            R.drawable.ic_active
                        )
                    )
                    binding.pointsDescription.text = getText(R.string.view_history)
                    val balance = viewModel.membershipCard.value?.balances?.first()
                    setBalanceText(balance)
                }
                LoginStatus.STATUS_LOGGED_IN_HISTORY_UNAVAILABLE -> {
                    binding.pointsImage.setImageDrawable(
                        getDrawable(
                            requireContext(),
                            R.drawable.ic_active
                        )
                    )
                    val balance = viewModel.membershipCard.value?.balances?.first()
                    setBalanceText(balance)

                    val updateTime = balance?.updated_at
                    val currentTime = Calendar.getInstance().timeInMillis / 1000
                    updateTime?.let {
                        val timeSinceUpdate = currentTime - it
                        binding.pointsDescription.text =
                            timeSinceUpdate.getElapsedTime(requireContext())
                    }
                }
                LoginStatus.STATUS_NOT_LOGGED_IN_HISTORY_AVAILABLE,
                LoginStatus.STATUS_NOT_LOGGED_IN_HISTORY_UNAVAILABLE -> {
                    binding.pointsText.text = getString(R.string.points_login)
                    binding.pointsDescription.text =
                        getString(R.string.description_see_history)
                    binding.pointsImage.setImageDrawable(
                        getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_login
                        )
                    )
                }
                LoginStatus.STATUS_LOGIN_UNAVAILABLE -> {
                    binding.pointsText.text = getString(R.string.history_text)
                    binding.pointsDescription.text =
                        getString(R.string.description_not_available)
                    binding.pointsImage.setImageDrawable(
                        getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_inactive
                        )
                    )
                }

                LoginStatus.STATUS_LOGIN_FAILED -> {
                    binding.pointsImage.setImageDrawable(
                        getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_login
                        )
                    )
                    binding.pointsText.text = getString(R.string.points_retry_login)
                    binding.pointsDescription.text =
                        getString(R.string.description_see_history)
                }

                LoginStatus.STATUS_LOGIN_PENDING -> {
                    binding.pointsImage.setImageDrawable(
                        getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_pending
                        )
                    )
                    binding.pointsText.text = getString(R.string.points_logging_in)
                    binding.pointsDescription.text =
                        getString(R.string.description_please_wait)
                }

                LoginStatus.STATUS_SIGN_UP_FAILED -> {
                    binding.pointsImage.setImageDrawable(
                        getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_login
                        )
                    )
                    binding.pointsText.text = getString(R.string.points_sign_up_failed)
                    binding.pointsDescription.text =
                        getString(R.string.description_please_try_again)
                }

                LoginStatus.STATUS_SIGN_UP_PENDING -> {
                    binding.pointsImage.setImageDrawable(
                        getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_pending
                        )
                    )
                    binding.pointsText.text = getString(R.string.points_signing_up)
                    binding.pointsDescription.text =
                        getString(R.string.description_please_wait)
                }

                LoginStatus.STATUS_REGISTER_GHOST_CARD_FAILED -> {
                    binding.pointsImage.setImageDrawable(
                        getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_login
                        )
                    )
                    binding.pointsText.text =
                        getString(R.string.points_registration_failed)
                    binding.pointsDescription.text =
                        getString(R.string.description_please_try_again)
                }
                LoginStatus.STATUS_REGISTER_GHOST_CARD_PENDING -> {
                    binding.pointsImage.setImageDrawable(
                        getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_pending
                        )
                    )
                    binding.pointsText.text = getString(R.string.points_registering_card)
                    binding.pointsDescription.text =
                        getString(R.string.description_please_wait)
                }

                LoginStatus.STATUS_CARD_ALREADY_EXISTS -> {
                    binding.pointsImage.setImageDrawable(
                        getDrawable(
                            requireContext(),
                            R.drawable.ic_lcd_module_icons_points_login
                        )
                    )
                    binding.pointsText.text = getString(R.string.points_login)
                    binding.pointsDescription.text =
                        getString(R.string.description_see_history)
                }
            }
        }

        binding.footerDelete.setOnClickListener { footerView ->
            val builder = AlertDialog.Builder(context)
            var dialog: AlertDialog? = null
            builder.setMessage(getString(R.string.delete_card_modal_body))
            builder.setNeutralButton(getString(R.string.no_text)) { _, _ -> }
            builder.setPositiveButton(getString(R.string.yes_text)) { _, _ ->
                if (verifyAvailableNetwork(requireActivity())) {
                    runBlocking {
                        viewModel.deleteCard(viewModel.membershipCard.value?.id)
                    }
                    viewModel.deleteError.observeNonNull(this@LoyaltyCardDetailsFragment) { error ->
                        Snackbar.make(footerView, error, Snackbar.LENGTH_SHORT).show()
                        dialog?.dismiss()
                    }
                    viewModel.deletedCard.observeNonNull(this@LoyaltyCardDetailsFragment) {
                        dialog?.dismiss()
                        findNavController().navigateIfAdded(this, R.id.detail_to_home)
                    }
                } else {
                    showNoInternetConnectionDialog()
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
                    getString(R.string.points_prefix_or_suffix, prefix, balance.value)
            } else {
                binding.pointsText.text = getString(
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
                    getString(R.string.points_prefix_or_suffix, balance.value, suffix)
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingIndicator.visibility = View.VISIBLE
            binding.linkedWrapper.visibility = View.INVISIBLE
            binding.pointsWrapper.visibility = View.INVISIBLE
        } else {
            binding.loadingIndicator.visibility = View.GONE
            binding.linkedWrapper.visibility = View.VISIBLE
            binding.pointsWrapper.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.paymentCards.value = null
    }
}