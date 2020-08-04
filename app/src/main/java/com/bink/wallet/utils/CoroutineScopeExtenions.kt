package com.bink.wallet.utils

import com.bink.wallet.data.PaymentCardDao
import com.bink.wallet.model.response.payment_card.PaymentCard
import kotlinx.coroutines.CoroutineScope
import java.util.*

suspend fun CoroutineScope.generateUuid(cards: List<PaymentCard>, paymentCardDao:PaymentCardDao){
    val oldCards = paymentCardDao.getAllAsync()
    //Loop through each card we get from Api
    cards.forEach { cardFromApi ->
        for (cardInDb in oldCards) {
            //Check if the card from Api has the same Id as any card in the database
            if (cardFromApi.id == cardInDb.id) {
                //When a match is found,we check if the uuid of the card in the database is null
                if (cardInDb.uuid == null) {
                    //If uuid for card in database is null, we generate a new uuid and assign to card from api
                    cardFromApi.uuid = UUID.randomUUID().toString()
                } else {
                    //Else if the card in the database has a uuid already,we assign that uuid to the card from api
                    cardFromApi.uuid = cardInDb.uuid
                }
            }
        }
    }
    //To cover all other cases in which Uuid is still be null
    val cardsWithoutUuid = cards.filter { it.uuid == null }
    cardsWithoutUuid.forEach { card ->
        card.uuid = UUID.randomUUID().toString()
    }
}