package com.bink.wallet.scenes.sign_up.continue_with_email.check_inbox

import android.content.Intent
import android.content.pm.LabeledIntent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.view.View
import com.bink.wallet.BaseFragment
import com.bink.wallet.R
import com.bink.wallet.databinding.CheckInboxFragmentBinding
import com.bink.wallet.utils.toolbar.FragmentToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class CheckInboxFragment : BaseFragment<CheckInboxViewModel, CheckInboxFragmentBinding>() {

    override val layoutRes = R.layout.check_inbox_fragment

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .with(binding.toolbar)
            .shouldDisplayBack(requireActivity())
            .build()
    }

    override val viewModel: CheckInboxViewModel by viewModel()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        arguments?.let { bundle ->
            val email = CheckInboxFragmentArgs.fromBundle(bundle).userEmail
            val isRepost = CheckInboxFragmentArgs.fromBundle(bundle).isRepost
            setSubtitle(email)

            if (isRepost) viewModel.rePostMagicLink(email)
        }

        binding.goToInbox.setOnClickListener {
            val emailClients = showEmailClients()

            if (emailClients != null) {
                startActivity(emailClients)
            }
        }

        if (showEmailClients() == null) {
            binding.goToInbox.visibility = View.GONE
        }

        startButtonFadeInCountdown()
    }

    private fun startButtonFadeInCountdown() {
        val countdown = object : CountDownTimer(3000, 1000) {
            override fun onFinish() {
                binding.goToInbox.animate().alpha(1.0f)
            }

            override fun onTick(millisUntilFinished: Long) {
            }
        }

        countdown.start()
    }

    private fun setSubtitle(email: String) {
        val subtitlePartOne = getString(R.string.check_inbox_subtitle_part_1)
        val subtitlePartTwo = getString(R.string.check_inbox_subtitle_part_2)
        val subtitle = "$subtitlePartOne <b>$email</b>. $subtitlePartTwo"
        binding.subtitle.text = Html.fromHtml(subtitle)
    }

    private fun showEmailClients(): Intent? {
        val emailIntent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"))
        val packageManager = context?.packageManager

        val activitiesHandlingEmails = packageManager?.queryIntentActivities(emailIntent, 0)
        if (activitiesHandlingEmails != null) {
            if (activitiesHandlingEmails.isNotEmpty()) {
                try {
                    val firstEmailPackageName = activitiesHandlingEmails.first().activityInfo.packageName
                    val firstEmailInboxIntent = packageManager.getLaunchIntentForPackage(firstEmailPackageName)
                    val emailAppChooserIntent = Intent.createChooser(firstEmailInboxIntent, getString(R.string.choose_email_client))

                    val emailInboxIntents = mutableListOf<LabeledIntent>()
                    for (i in 1 until activitiesHandlingEmails.size) {
                        val activityHandlingEmail = activitiesHandlingEmails[i]
                        val packageName = activityHandlingEmail.activityInfo.packageName
                        val intent = packageManager.getLaunchIntentForPackage(packageName)
                        emailInboxIntents.add(
                            LabeledIntent(
                                intent,
                                packageName,
                                activityHandlingEmail.loadLabel(packageManager),
                                activityHandlingEmail.icon
                            )
                        )
                    }
                    val extraEmailInboxIntents = emailInboxIntents.toTypedArray()
                    return emailAppChooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraEmailInboxIntents)
                } catch (e: Exception) {
                    return null
                }
            } else {
                return null
            }
        }

        return null
    }

}