package com.bink.wallet.utils

import android.os.Bundle
import com.bink.wallet.BaseFragment
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.data.PaymentCardDao
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.FirebaseEvents.FAILED_EVENT_NO_DATA
import com.bink.wallet.utils.FirebaseEvents.LOYALTY_CARD_STATUS
import com.bink.wallet.utils.FirebaseEvents.PAYMENT_CARD_STATUS
import com.bink.wallet.utils.FirebaseEvents.PLL_ACTIVE
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.coroutineScope
import java.util.*

val firebaseAnalytics = Firebase.analytics

suspend fun generateUuidForPaymentCards(
    cards: List<PaymentCard>,
    paymentCardDao: PaymentCardDao,
    membershipCardDao: MembershipCardDao
) {
    val oldCards = paymentCardDao.getAllAsync()
    //Loop through each card we get from Api
    cards.forEach { cardFromApi ->
        for (cardInDb in oldCards) {
            //Check if the card from Api has the same Id as any card in the database
            if (cardFromApi.id == cardInDb.id) {
                //When a match is found,we check if the uuid of the card in the database is null
                //If uuid for card in database is null, we generate a new uuid and assign to card from api
                //Else if the card in the database has a uuid already,we assign that uuid to the card from api
                if (cardInDb.uuid == null) cardFromApi.uuid =
                    UUID.randomUUID().toString() else cardFromApi.uuid = cardInDb.uuid

                if (!cardInDb.status.equals(cardFromApi.status)) {
                    coroutineScope {
                        logStatusChange(
                            cardFromApi,
                            firebaseAnalytics
                        )
                        logPllActivePaymentCard(cardInDb, cardFromApi, membershipCardDao)
                    }
                }
            }
        }
    }
    //To cover all other cases in which Uuid is still null
    val cardsWithoutUuid = cards.filter { it.uuid == null }
    cardsWithoutUuid.forEach { card ->
        card.uuid = UUID.randomUUID().toString()
    }
}

suspend fun generateUuidFromCardLinkageState(
    card: PaymentCard,
    paymentCardDao: PaymentCardDao
) {
    val oldCards = paymentCardDao.getAllAsync()

    for (cardInDb in oldCards) {
        if (card.id == cardInDb.id) {
            if (cardInDb.uuid == null) card.uuid = UUID.randomUUID().toString() else card.uuid =
                cardInDb.uuid
        }
    }

}

suspend fun generateUuidForMembershipCardPullToRefresh(
    membershipCardFromApi: MembershipCard,
    membershipCardDao: MembershipCardDao,
    paymentCardDao: PaymentCardDao
) {
    val oldMembershipCards = membershipCardDao.getAllAsync()

    for (membershipCardInDb in oldMembershipCards) {
        if (membershipCardInDb.id == membershipCardFromApi.id) {
            if (membershipCardInDb.uuid == null) membershipCardFromApi.uuid = UUID.randomUUID()
                .toString() else membershipCardFromApi.uuid = membershipCardInDb.uuid

            if (!membershipCardInDb.status?.state.equals(membershipCardFromApi.status?.state)) {
                coroutineScope {
                    logLoyaltyCardStatusChange(
                        membershipCardFromApi,
                        firebaseAnalytics
                    )
                    logPllActive(membershipCardInDb, membershipCardFromApi, paymentCardDao)

                }
            }
        }
    }
}

suspend fun generateUuidForMembershipCards(
    cards: List<MembershipCard>,
    membershipCardDao: MembershipCardDao,
    paymentCardDao: PaymentCardDao
) {
    val oldMembershipCards = membershipCardDao.getAllAsync()

    cards.forEach { membershipCardFromApi ->
        for (membershipCardInDb in oldMembershipCards) {
            if (membershipCardInDb.id == membershipCardFromApi.id) {
                if (membershipCardInDb.uuid == null) membershipCardFromApi.uuid =
                    UUID.randomUUID().toString() else membershipCardFromApi.uuid =
                    membershipCardInDb.uuid

                if (!membershipCardInDb.status?.state.equals(membershipCardFromApi.status?.state)) {
                    coroutineScope {
                        logLoyaltyCardStatusChange(
                            membershipCardFromApi,
                            firebaseAnalytics
                        )
                        logPllActive(membershipCardInDb, membershipCardFromApi, paymentCardDao)

                    }
                }
            }
        }

    }
    //To cover all other cases in which Uuid is still null
    val cardsWithoutUuid = cards.filter { it.uuid == null }
    cardsWithoutUuid.forEach { card ->
        card.uuid = UUID.randomUUID().toString()
    }

}

fun logFirebaseEvent(
    name: String,
    parameters: Map<String, Any>,
    firebaseAnalytics: FirebaseAnalytics
) {
    val bundle = Bundle()

    for (entry: Map.Entry<String, Any> in parameters) {
        if (entry.value is Int) {
            bundle.putInt(entry.key, entry.value as Int)
        } else {
            bundle.putString(entry.key, entry.value.toString())
        }

    }

    firebaseAnalytics.logEvent(name, bundle)
}

fun failedEvent(
    eventName: String,
    firebaseAnalytics: FirebaseAnalytics
) {
    val bundle = Bundle()

    bundle.putString(FirebaseEvents.ATTEMPTED_EVENT_KEY, eventName)

    firebaseAnalytics.logEvent(FAILED_EVENT_NO_DATA, bundle)
}

suspend fun logStatusChange(
    cardFromApi: PaymentCard,
    firebaseAnalytics: FirebaseAnalytics
) {
    val provider = cardFromApi.card?.provider
    val uuid = cardFromApi.uuid
    val status = cardFromApi.status
    if (provider == null || uuid == null || status == null) {
        coroutineScope {
            failedEvent(
                PAYMENT_CARD_STATUS,
                firebaseAnalytics
            )
        }
    } else {
        coroutineScope {
            logFirebaseEvent(
                PAYMENT_CARD_STATUS,
                BaseFragment.getPaymentCardStatusMap(provider, uuid, status),
                firebaseAnalytics
            )
        }
    }
}

suspend fun logLoyaltyCardStatusChange(
    card: MembershipCard,
    firebaseAnalytics: FirebaseAnalytics
) {

    val uuid = card.uuid
    val status = card.status?.state
    val membershipPlan = card.membership_plan

    if (uuid == null || status == null || membershipPlan == null) {
        coroutineScope { failedEvent(LOYALTY_CARD_STATUS, firebaseAnalytics) }
    } else {
        coroutineScope {
            logFirebaseEvent(
                LOYALTY_CARD_STATUS,
                BaseFragment.getLoyaltyCardStatusMap(uuid, status, membershipPlan),
                firebaseAnalytics
            )
        }
    }
}

suspend fun logPllActive(
    membershipCardInDb: MembershipCard,
    membershipFromApi: MembershipCard,
    paymentCardDao: PaymentCardDao
) {

    //Loop through the payment cards from api
    membershipFromApi.payment_cards?.forEach { pCard ->
        //Get it's id
        val paymentCardId = pCard.id
        //Use the id to get that payment card's uuid
        val paymentCardUuid = paymentCardDao.getAllAsync()
            .firstOrNull { card -> card.id.toString() == paymentCardId }?.uuid
        val loyaltyUuid = membershipCardInDb.uuid

        if (paymentCardUuid == null || loyaltyUuid == null) {
            BaseFragment.logFailedEvent(PLL_ACTIVE)
        } else {
            BaseFragment.logPllEvent(
                PLL_ACTIVE,
                BaseFragment.getPllActiveMap(paymentCardUuid, loyaltyUuid)
            )
        }


    }
}

suspend fun logPllActivePaymentCard(
    paymentCardInDb: PaymentCard,
    paymentCardFromApi: PaymentCard,
    membershipCardDao: MembershipCardDao
) {

    //Loop through the payment cards from api
    paymentCardFromApi.membership_cards.forEach { loyaltyCard ->
        //Get it's id
        val loyaltyCardId = loyaltyCard.id
        //Use the id to get that payment card's uuid
        val loyaltyCardUuid = membershipCardDao.getAllAsync()
            .firstOrNull { card -> card.id == loyaltyCardId }?.uuid
        val paymentCardUuid = paymentCardInDb.uuid

        if (paymentCardUuid == null || loyaltyCardUuid == null) {
            BaseFragment.logFailedEvent(PLL_ACTIVE)
        } else {
            BaseFragment.logPllEvent(
                PLL_ACTIVE,
                BaseFragment.getPllActiveMap(paymentCardUuid, loyaltyCardUuid)
            )
        }


    }
}

