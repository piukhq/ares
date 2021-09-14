package com.bink.wallet.utils

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bink.wallet.R

class ProgressButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val progressBar: ProgressBar
    private val buttonTextView: TextView

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.progress_button, this, true)
        buttonTextView = root.findViewById(R.id.tv_button_text)
        progressBar = root.findViewById(R.id.pg_button_progress_bar)
        loadAttrs(attrs, defStyleAttr)
    }

    private fun loadAttrs(attrs: AttributeSet?, defStyleAttr: Int) {
        val arr = context.obtainStyledAttributes(
            attrs,
            R.styleable.ProgressButton,
            defStyleAttr,
            0
        )
        val buttonText = arr.getString(R.styleable.ProgressButton_text)
        val loading = arr.getBoolean(R.styleable.ProgressButton_loading, false)
        val enabled = arr.getBoolean(R.styleable.ProgressButton_enabled, false)

        isEnabled = enabled
        setText(buttonText)
        setLoading(loading)

        arr.recycle()
    }

    fun setLoading(loading: Boolean) {
        isClickable = !loading //Disable clickable when loading
        if (loading) {
            buttonTextView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        } else {
            buttonTextView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }


    fun setText(text: String?) {
        buttonTextView.text = text
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        buttonTextView.isEnabled = enabled
    }
}