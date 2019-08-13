package com.bink.wallet.scenes.loyalty_wallet

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.BarcodeFragmentBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class BarcodeFragment : BaseFragment<BarcodeViewModel, BarcodeFragmentBinding>() {
    override val viewModel: BarcodeViewModel by viewModel()
    override val layoutRes: Int
        get() = R.layout.barcode_fragment

    private var initialConstraints = ConstraintSet()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let {
           BarcodeFragmentArgs.fromBundle(it).apply {
               viewModel.membershipPlan.value = currentMembershipPlan
               viewModel.barcode.value = barcode
               viewModel.isBarcodeAvailable.value = !barcode.isNullOrEmpty()
           }
        }

        binding.barcodeImage.setOnClickListener {
            if(viewModel.isMaximized.value == true){
                unMaximizeBarcode()
            } else {
                maximizeBarcode()
            }
        }

        binding.buttonMaximize.setOnClickListener {
            maximizeBarcode()
        }

        binding.screen.setOnClickListener {
            unMaximizeBarcode()
        }

        binding.viewModel=  viewModel
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            if(viewModel.isMaximized.value == true){
                unMaximizeBarcode()
            } else {
                activity?.onBackPressed()
            }
        }
    }

    private fun maximizeBarcode(){
        initialConstraints.clone(binding.screen)

        binding.buttonMaximize.visibility = View.GONE
        binding.barcodeAddedDate.visibility = View.GONE
        binding.barcodeDescription.visibility = View.GONE
        binding.cardNumber.visibility = View.GONE
        binding.cardNumberLabel.visibility = View.GONE
        viewModel.isMaximized.value = true

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.screen)
        constraintSet.connect(binding.barcodeImage.id, ConstraintSet.TOP, binding.screen.id, ConstraintSet.TOP, 0)
        constraintSet.connect(binding.barcodeImage.id, ConstraintSet.BOTTOM, binding.screen.id, ConstraintSet.BOTTOM, 0)
        constraintSet.connect(binding.barcodeImage.id, ConstraintSet.START, binding.screen.id, ConstraintSet.START, 0)
        constraintSet.connect(binding.barcodeImage.id, ConstraintSet.END, binding.screen.id, ConstraintSet.END, 0)
        constraintSet.applyTo(binding.screen)
    }

    private fun unMaximizeBarcode(){
        binding.buttonMaximize.visibility = View.VISIBLE
        binding.barcodeAddedDate.visibility = View.VISIBLE
        binding.barcodeDescription.visibility = View.VISIBLE
        binding.cardNumber.visibility = View.VISIBLE
        binding.cardNumberLabel.visibility = View.VISIBLE
        viewModel.isMaximized.value = false
        initialConstraints.applyTo(binding.screen)
    }

}