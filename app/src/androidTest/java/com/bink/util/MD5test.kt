package com.bink.util

import com.bink.wallet.model.response.payment_card.BankCard
import junit.framework.Assert
import org.junit.Test

class MD5test {

    @Test
    fun checkMD5() {
        Assert.assertEquals(
            "f620f241baf3ed5ceb33184716e0b5a0",
            BankCard.fingerprintGenerator("4242424242424242", "21", "01")
        )
    }

}