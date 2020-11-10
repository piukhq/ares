package com.bink.wallet.utils

import androidx.fragment.app.Fragment
import com.bink.wallet.BuildConfig
import com.bink.wallet.utils.enums.BuildTypes
import zendesk.support.guide.ViewArticleActivity
import zendesk.support.requestlist.RequestListActivity
import java.util.*


fun Fragment.goToContactUsForm(){
    RequestListActivity.builder()
        .show(requireActivity())
}

fun Fragment.goToPendingFaqArticle(){
    val articleId =
        if (BuildConfig.BUILD_TYPE.toLowerCase(Locale.ENGLISH) == BuildTypes.RELEASE.type) PROD_ARTICLE_ID else SANDBOX_ARTICLE_ID
    ViewArticleActivity.builder(articleId)
        .withContactUsButtonVisible(false)
        .show(requireContext())
}