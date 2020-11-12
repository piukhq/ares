package com.bink.wallet.scenes.add_loyalty_card

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.databinding.AddLoyaltyCardFragmentBinding
import com.bink.wallet.model.response.membership_plan.Account
import com.bink.wallet.model.response.membership_plan.MembershipPlan
import com.bink.wallet.utils.ADD_AUTH_BARCODE
import com.bink.wallet.utils.FirebaseEvents.ADD_LOYALTY_CARD_VIEW
import com.bink.wallet.utils.UtilFunctions
import com.bink.wallet.utils.enums.SignUpFieldTypes
import com.bink.wallet.utils.navigateIfAdded
import com.bink.wallet.utils.setVisible
import com.bink.wallet.utils.toolbar.FragmentToolbar
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddLoyaltyCardFragment :
    BaseFragment<AddLoyaltyCardViewModel, AddLoyaltyCardFragmentBinding>(),
    ZXingScannerView.ResultHandler {
    private val args by navArgs<AddLoyaltyCardFragmentArgs>()
    private lateinit var brands: Array<MembershipPlan>
    private var validators = HashMap<String, String?>()
    var resumeTimerFromMillis: Long = -1
    var isPaused = false
    var hapticTimerWithPause = true
    var cancelHaptic = false
    private var isFromAddAuth = false
    private var account: Account? = null

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar).shouldDisplayBack(requireActivity())
            .build()
    }

    override val layoutRes: Int
        get() = R.layout.add_loyalty_card_fragment

    override val viewModel: AddLoyaltyCardViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cancelHaptic = false
        setBottomLayout()
        getValidators()
        context?.resources?.getInteger(R.integer.add_loyalty_haptic_delay)?.toLong()
            ?.let { scheduleHapticWithPause(it) }
        isFromAddAuth = args.isFromAddAuth
        account = args.account
    }

    override fun onResume() {
        super.onResume()
        isPaused = false
        logScreenView(ADD_LOYALTY_CARD_VIEW)
        startScanning()
        if (resumeTimerFromMillis > 0) {
            scheduleHapticWithPause(resumeTimerFromMillis)
        }
    }

    override fun onPause() {
        isPaused = true
        super.onPause()
        stopScanning()
    }

    private fun stopScanning() {
        binding.scannerView.stopCameraPreview()
    }

    private fun getValidators() {
        args.membershipPlans?.let {
            brands = it
            for (plan in brands) {
                var foundInAddFields = false
                plan.account?.add_fields?.map { field ->
                    if (field.common_name == SignUpFieldTypes.BARCODE.common_name && !field.validation.isNullOrEmpty()) {
                        validators[plan.id] = field.validation
                        foundInAddFields = true
                    }
                }
                if (!foundInAddFields) {
                    plan.account?.authorise_fields?.map { field ->
                        if (field.common_name == SignUpFieldTypes.BARCODE.common_name) {
                            validators[plan.id] = field.validation
                            foundInAddFields = true
                        }
                    }
                }
            }
        }
    }

    override fun handleResult(rawResult: Result?) {
        cancelHaptic = true
        val savedInstanceState = findNavController().previousBackStackEntry?.savedStateHandle
        savedInstanceState?.remove<String>(ADD_AUTH_BARCODE)
        SharedPreferenceManager.scannedLoyaltyBarCode = null

        if (isFromAddAuth && account != null && rawResult != null) {
            if (isValidRegex(rawResult.text)) {
                savedInstanceState?.set(ADD_AUTH_BARCODE, rawResult.text)
                if (rawResult.text != null) {
                    findNavController().popBackStack()

                }
            } else {
                showUnsupportedBarcodePopup(account!!.company_name!!)

            }

        } else {
            val membershipPlan: MembershipPlan? = findMembershipPlan(rawResult)

            membershipPlan?.also {

                val membershipCardId = ""
                val action = AddLoyaltyCardFragmentDirections.addLoyaltyToAddCardFragment(
                    membershipPlan = it,
                    membershipCardId = membershipCardId,
                    barcode = rawResult.toString()
                )
                findNavController().navigateIfAdded(this, action)

            } ?: run {
                showUnsupportedBarcodePopup()
            }
        }

    }

    private fun setBottomLayout() {
        binding.scannerView.setViewFinderMeasureCallback {
            val topPadding = binding.scannerView.totalPreviewHeight
            binding.bottomView.root.post {
                binding.bottomView.root.setPadding(0, topPadding, 0, 0)
                binding.bottomView.root.alpha = 0f
                binding.bottomView.root.setVisible(true)
                binding.bottomView.root.animate().alpha(1f)

            }
        }
        binding.bottomView.enterManuallyContainer.setOnClickListener()
        {
            cancelHaptic = true
            if (isFromAddAuth) {
                findNavController().popBackStack()
            } else {
                goToBrowseBrands()

            }
        }
    }

    private fun findMembershipPlan(rawResult: Result?): MembershipPlan? {
        var foundPlan: MembershipPlan? = null

        for ((planId, validation) in validators) {
            if (!validation.isNullOrEmpty() && UtilFunctions.isValidField(
                    validation,
                    rawResult?.text
                )
            ) {
                foundPlan = brands.find { plan -> plan.id == (planId) }
                break
            }
        }
        return foundPlan
    }

    private fun goToBrowseBrands() {
        val directions = args.membershipPlans?.let { plans ->
            args.membershipCards?.let { cards ->
                AddLoyaltyCardFragmentDirections.addLoyaltyToBrowse(
                    plans,
                    cards
                )
            }
        }
        if (directions != null) {
            findNavController().navigateIfAdded(this, directions)
        }
    }

    private fun showUnsupportedBarcodePopup(companyName: String = "") {
        if (isFromAddAuth) {
            showTryAgainGenericError(
                requireActivity(), getString(R.string.scan_failure_body, companyName)
            )
        }
        performHaptic(
            getString(R.string.unrecognized_barcode_title),
            getString(R.string.unrecognized_barcode_body)
        )
    }


    private fun performHaptic(
        title: String = getString(R.string.enter_manually),
        text: String = getString(R.string.enter_manually_cant_scan)
    ) {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    VIBRATION_DURATION,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator?.vibrate(VIBRATION_DURATION)
        }

        binding.bottomView.enterText.text = title
        binding.bottomView.enterSubtext.text = text
        val colorResource = resources.getColor(R.color.red_attention, null)
        binding.bottomView.enterText.setTextColor(colorResource)

        binding.bottomView.icon.setImageResource(R.drawable.scan_message_icons_error)

        val bounceAnimation = AnimationUtils.loadAnimation(context, R.anim.bounce)
        binding.bottomView.enterManuallyContainer.startAnimation(bounceAnimation)
    }


    private fun timer(millisUntilTrigger: Long, countDownInterval: Long): CountDownTimer {
        return object : CountDownTimer(millisUntilTrigger, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {

                if (cancelHaptic) {
                    resumeTimerFromMillis = -1
                    cancel()
                } else
                    if (isPaused && hapticTimerWithPause) {
                        resumeTimerFromMillis = millisUntilFinished
                        cancel()
                    }
            }

            override fun onFinish() {
                resumeTimerFromMillis = -1
                if (!cancelHaptic) {
                    if (isAdded){
                        performHaptic()
                    }
                }
            }
        }

    }

    /**If [hapticTimerWithPause] is set to true, the timer will pause when the fragment goes to the background, and resume when it gets back to the foreground.
     * If it is set to false, the timer will continue and [performHaptic] will be triggered even if the fragment is in the background(Note: in this case the vibration will not work)*/
    private fun scheduleHapticWithPause(millisUntilTrigger: Long, countDownInterval: Long = 100) {
        timer(millisUntilTrigger, countDownInterval).start()
    }

    private fun startScanning() {
        binding.scannerView.setResultHandler(this)
        binding.scannerView.startCamera()
    }

    private fun isValidRegex(barcode: String): Boolean {
        var validationPattern: String? = ""

        account?.let {
            it.add_fields?.forEach { planField ->
                if (planField.common_name.equals(BARCODE)) {
                    validationPattern = planField.validation
                }
            }
        }

        if (UtilFunctions.isValidField(validationPattern, barcode)) {
            return true
        }
        return false

    }

    private fun showTryAgainGenericError(
        activity: Activity,
        message: String

    ) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.getString(R.string.payment_card_scanning_scan_failed_title))
        builder.setMessage(message)
        builder.setPositiveButton(activity.getString(android.R.string.ok)) { dialogInterface, _ ->
            dialogInterface.cancel()
            startScanning()
        }

        builder.create().show()
    }

    companion object {
        private const val VIBRATION_DURATION = 600L
        private const val BARCODE = "barcode"

    }
}