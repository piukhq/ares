package com.bink.wallet.scenes.dynamic_actions

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.DynamicActionFragmentBinding
import com.bink.wallet.model.DynamicActionEventBodyCTAHandler
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class DynamicActionFragment : BaseFragment<DynamicActionViewModel, DynamicActionFragmentBinding>() {

    override val viewModel: DynamicActionViewModel by viewModel()

    override val layoutRes = R.layout.dynamic_action_fragment

    private var snowList: ArrayList<Snow> = ArrayList()
    private lateinit var runnable: Runnable

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.ic_close)
        arguments?.let { bundle ->
            DynamicActionFragmentArgs.fromBundle(bundle).apply {
                dynamicActionEvent.body?.let { body ->
                    binding.title.text = body.title
                    binding.description.text = body.description

                    body.cta?.let { cta ->
                        binding.firstButton.text = cta.title
                        binding.firstButton.visibility = View.VISIBLE

                        cta.action?.let { action ->
                            binding.firstButton.setOnClickListener {
                                launchDynamicActionEventCta(action)
                            }
                        }
                    }


                }
            }
        }

    }

    override fun onResume() {
        setUpSnowEffect()
        super.onResume()
    }

    override fun onPause() {
        var container: ViewGroup = activity?.window?.decorView as ViewGroup
        if (snowList.isNotEmpty()) {
            for (snowflake in snowList) {
                container.removeView(snowflake.snowflake)
            }
        }

        snowList.clear()
        super.onPause()
    }

    private fun setUpSnowEffect() {
        val point = Point()
        activity?.windowManager?.defaultDisplay?.getRealSize(point)

        val container: ViewGroup = activity?.window?.decorView as ViewGroup
        val bgHandler = Handler()

        for (i in 0 until 200) {
            snowList.add(Snow(requireContext(), point.x.toFloat(), point.y.toFloat(), container))
        }

        runnable = Runnable {
            for (snow: Snow in snowList)
                snow.update()
            bgHandler.postDelayed(runnable, 10)
        }
        bgHandler.post(runnable)
    }

    private fun launchDynamicActionEventCta(action: DynamicActionEventBodyCTAHandler) {
        when (action) {
            DynamicActionEventBodyCTAHandler.ZENDESK_CONTACT_US -> {
                contactSupport()
            }
        }
    }

}

class Snow(
    context: Context,
    private val screenW: Float,
    private val screenH: Float,
    parent: ViewGroup
) {
    var snowflake: ImageView = ImageView(context)
    private var distance = Random().nextFloat() * 0.5f + 0.5f
    private val fallingSpeed = 6
    private val windSpeed = 4

    init {
        snowflake.setBackgroundResource(R.mipmap.ic_snowflake_foreground)
        parent.addView(snowflake)

        val dimens = (Random().nextFloat() * screenH * 0.015 / distance).toInt()
        snowflake.layoutParams.height = dimens
        snowflake.layoutParams.width = dimens
        snowflake.alpha = (1.0f - distance * 0.7).toFloat()
        snowflake.translationX = Random().nextFloat() * screenW
        snowflake.translationY = Random().nextFloat() * screenH
        snowflake.rotation = Random().nextFloat() * 360
    }

    fun update() {
        snowflake.translationY =
            (snowflake.translationY + fallingSpeed * (1 - distance * 0.8)).toFloat()
        snowflake.translationX =
            (snowflake.translationX + windSpeed * (1 - distance * 0.7)).toFloat()

        if (snowflake.translationY > screenH)
            snowflake.translationY = snowflake.translationY - screenH
        if (snowflake.translationX > screenW)
            snowflake.translationX = snowflake.translationX - screenW

        snowflake.rotation = snowflake.rotation + (Random().nextFloat() * 0.6f - 0.3f) * 5
    }
}