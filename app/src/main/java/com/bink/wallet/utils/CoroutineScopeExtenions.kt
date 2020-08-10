package com.bink.wallet.utils

import android.os.Bundle
import com.bink.wallet.BaseFragment
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.data.PaymentCardDao
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import com.bink.wallet.utils.FirebaseEvents.FAILED_EVENT_NO_DATA
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_STATUS_ACTIVE
import com.bink.wallet.utils.FirebaseEvents.FIREBASE_STATUS_PENDING
import com.bink.wallet.utils.FirebaseEvents.PAYMENT_CARD_STATUS
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import java.util.*

suspend fun CoroutineScope.generateUuidForPaymentCards(
    cards: List<PaymentCard>,
    paymentCardDao: PaymentCardDao
) {
    val oldCards = paymentCardDao.getAllAsync()
    val firebaseAnalytics = Firebase.analytics
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

                if ((cardInDb.status.equals(FIREBASE_STATUS_PENDING) && cardFromApi.status.equals(
                        FIREBASE_STATUS_ACTIVE
                    )) || (cardInDb.status.equals(FIREBASE_STATUS_ACTIVE) && cardFromApi.status.equals(
                        FIREBASE_STATUS_PENDING
                    ))
                ) {
                    coroutineScope {
                        logStatusChange(
                            cardFromApi,
                            firebaseAnalytics
                        )
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
}

suspend fun CoroutineScope.generateUuidFromCardLinkageState(
    card: PaymentCard,
    paymentCardDao: PaymentCardDao
) {
    val oldCards = paymentCardDao.getAllAsync()

    for (cardInDb in oldCards) {
        if (card.id == cardInDb.id) {
            if (cardInDb.uuid == null) {
                card.uuid = UUID.randomUUID().toString()
            } else {
                card.uuid = cardInDb.uuid
            }
        }
    }

}

suspend fun CoroutineScope.generateUuidForMembershipCardPullToRefresh(
    card: MembershipCard,
    membershipCardDao: MembershipCardDao
) {
    val oldMembershipCards = membershipCardDao.getAllAsync()

    for (membershipCardInDb in oldMembershipCards) {
        if (membershipCardInDb.id == card.id) {
            if (membershipCardInDb.uuid == null) {
                card.uuid = UUID.randomUUID().toString()
            } else {
                card.uuid = membershipCardInDb.uuid
            }
        }
    }

}

suspend fun CoroutineScope.generateUuidForMembershipCards(
    cards: List<MembershipCard>,
    membershipCardDao: MembershipCardDao
) {
    val oldMembershipCards = membershipCardDao.getAllAsync()

    cards.forEach { card ->
        for (membershipCardInDb in oldMembershipCards) {
            if (membershipCardInDb.id == card.id) {
                if (membershipCardInDb.uuid == null) {
                    card.uuid = UUID.randomUUID().toString()
                } else {
                    card.uuid = membershipCardInDb.uuid
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

suspend fun CoroutineScope.logFirebaseEvent(
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

suspend fun CoroutineScope.failedEvent(
    eventName: String,
    firebaseAnalytics: FirebaseAnalytics
) {
    val bundle = Bundle()

    bundle.putString(FirebaseEvents.ATTEMPTED_EVENT_KEY, eventName)

    firebaseAnalytics.logEvent(FAILED_EVENT_NO_DATA, bundle)
}

suspend fun CoroutineScope.logStatusChange(
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


