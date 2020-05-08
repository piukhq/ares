package com.bink.wallet.scenes.add_loyalty_card

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.animation.AnimationUtils
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.AddLoyaltyCardFragmentBinding
import com.bink.wallet.model.response.membership_plan.MembershipPlan
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
        setupPermissions()
        getValidators()
        requireContext().resources?.getInteger(R.integer.add_loyalty_haptic_delay)?.toLong()
            ?.let { scheduleHapticWithPause(it) }

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
        brands = args.membershipPlans
        for (plan in brands) {
            var foundInAddFields = false
            plan.account?.add_fields?.map { field ->
                if (field.common_name == SignUpFieldTypes.BARCODE.common_name && !field.validation.isNullOrEmpty()) {
                    validators[plan.id] = field.validation
                    foundInAddFields = true
                }
            }
            //TODO find out if searching for a validation regex in authorize_fields is needed if we didn't find one in add_fields
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


    private fun setupPermissions() {
        val permission = activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.CAMERA
            )
        }

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeCameraRequest()
        }
    }

    private fun makeCameraRequest() {
        requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == CAMERA_REQUEST_CODE) {

            with(binding) {
                if (permissions[0] == Manifest.permission.CAMERA
                    && grantResults[0] != PackageManager.PERMISSION_GRANTED
                ) {
                    bottomView.cameraPreviewSubtitle.text =
                        getString(R.string.camera_no_permission)
                    setCameraPreviewListener()
                } else {
                    bottomView.cameraPreviewSubtitle.text =
                        getString(R.string.camera_preview_subtitle)
                    scannerView.setOnClickListener(null)
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun handleResult(rawResult: Result?) {

        cancelHaptic = true

        val membershipPlan: MembershipPlan? = findMembershipPlan(rawResult)

        membershipPlan?.also {

            val membershipCardId = ""

            val action = AddLoyaltyCardFragmentDirections.addLoyaltyToAddCardFragment(
                membershipPlan = it,
                membershipCardId = membershipCardId
            )

            findNavController().navigateIfAdded(this, action)

        } ?: run {
            showUnsupportedBarcodePopup()
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
            goToBrowseBrands()
        }
    }

    private fun setCameraPreviewListener() {
        binding.scannerView.setOnClickListener {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                makeCameraRequest()
            } else {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val uri: Uri = Uri.fromParts("package", activity?.packageName, null)
                intent.data = uri
                startActivity(intent)
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
        val directions = AddLoyaltyCardFragmentDirections.addLoyaltyToBrowse(
            args.membershipPlans,
            args.membershipCards
        )
        findNavController().navigateIfAdded(this, directions)
    }

    private fun showUnsupportedBarcodePopup() {
        performHaptic(R.string.unrecognized_barcode_title, R.string.unrecognized_barcode_body)
    }


    private fun performHaptic(
        @StringRes titleId: Int = R.string.enter_manually,
        @StringRes textId: Int = R.string.enter_manually_cant_scan
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

        binding.bottomView.enterText.text = getString(titleId)
        binding.bottomView.enterSubtext.text = getString(textId)
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
                    performHaptic()
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

    companion object {
        private const val CAMERA_REQUEST_CODE = 101
        private const val VIBRATION_DURATION = 600L
    }
}