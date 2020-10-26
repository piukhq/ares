package com.bink.wallet

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.SwitchCompat


class GradientSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwitchCompat(context, attrs) {

    fun displayCustomSwitch(isEnabled: Boolean) {
        if (isEnabled) displaySwitchEnabled()
        else displaySwitchDisabled()
    }

    private fun displaySwitchEnabled() {
        setThumbResource(R.drawable.gradient_switch_thumb)
        setTrackResource(R.drawable.gradient_switch_track)
    }

    private fun displaySwitchDisabled() {
        setThumbResource(R.drawable.gradient_switch_thumb_off)
        setTrackResource(R.drawable.gradient_switch_track_off)
    }
}