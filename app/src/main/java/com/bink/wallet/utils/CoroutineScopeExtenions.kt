package com.bink.wallet.utils

import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.data.PaymentCardDao
import com.bink.wallet.model.response.membership_card.MembershipCard
import com.bink.wallet.model.response.payment_card.PaymentCard
import kotlinx.coroutines.CoroutineScope
import java.util.*

suspend fun CoroutineScope.generateUuidForPaymentCards(
    cards: List<PaymentCard>,
    paymentCardDao: PaymentCardDao
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


            }
        }
    }
    //To cover all other cases in which Uuid is still null
    val cardsWithoutUuid = cards.filter { it.uuid == null }
    cardsWithoutUuid.forEach { card ->
        card.uuid = UUID.randomUUID().toString()
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
                card.uuid == UUID.randomUUID().toString()
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