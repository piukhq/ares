package com.bink.wallet.model

data class DynamicAction(val name: String, val type: String, val startDate: Int, val endDate: Int, val arrayList: ArrayList<DynamicActionLocation>, val event: DynamicActionEvent)

data class DynamicActionLocation(val icon: String, val screen: String, val area: String, val action: String)

data class DynamicActionEvent(val type: String, val body: DynamicActionEventBody)

data class DynamicActionEventBody(val title: String, val description: String, val cta: DynamicActionEventBodyCta)

data class DynamicActionEventBodyCta(val title: String, val action: String)