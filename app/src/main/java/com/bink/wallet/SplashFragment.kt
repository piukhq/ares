package com.bink.wallet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bink.sdk.BinkCore
import com.bink.wallet.data.SharedPreferenceManager
import com.bink.wallet.utils.EMPTY_STRING
import com.bink.wallet.utils.LocalStoreUtils
import com.bink.wallet.utils.SESSION_HANDLER_DESTINATION_ONBOARDING
import com.bink.wallet.utils.enums.BuildTypes
import com.bink.wallet.utils.getSessionHandlerNavigationDestination
import com.bink.wallet.utils.navigateIfAdded
import com.scottyab.rootbeer.RootBeer
import java.util.*

class SplashFragment : Fragment() {

    companion object {
        init {
            System.loadLibrary("spreedly-lib")
        }
    }

    external fun spreedlyKey(): String
    //todo move this to where we need it
    external fun paymentCardHashingDevKey(): String
    external fun paymentCardHashingStagingKey(): String
    external fun paymentCardHashingProdKey(): String
    external fun paymentCardEncryptionPublicKeyDev(): String
    external fun paymentCardEncryptionPublicKeyStaging(): String
    external fun paymentCardEncryptionPublicKeyProd(): String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        LocalStoreUtils.setAppSharedPref(
            LocalStoreUtils.KEY_SPREEDLY, spreedlyKey()
        )
        persistPaymentCardHashSecret()
        findNavController().navigateIfAdded(this, getDirections())
    }

    private fun getDirections(): Int {
        val rootBeer = RootBeer(context)
        return when (rootBeer.isRooted) {
            true -> R.id.splash_to_rooted_device
            else -> getUnRootedDirections()
        }
    }

    private fun getUnRootedDirections(): Int {
        if (!requireContext().let { LocalStoreUtils.isLoggedIn(LocalStoreUtils.KEY_TOKEN) }) {
            val binkCore = BinkCore(requireContext())
            val key = binkCore.sessionConfig.apiKey
            val email = binkCore.sessionConfig.userEmail
            if (!key.isNullOrEmpty()) {
                SharedPreferenceManager.isUserLoggedIn = true
                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_TOKEN,
                    getString(R.string.token_api_v1, key)
                )

                LocalStoreUtils.setAppSharedPref(
                    LocalStoreUtils.KEY_EMAIL,
                    email ?: EMPTY_STRING
                )
            }
        }

        return when (requireContext().let { LocalStoreUtils.isLoggedIn(LocalStoreUtils.KEY_TOKEN) }) {
            true -> R.id.global_to_home
            else ->
                /**
                 *      Since in the future we might want to redirect the user to
                 * different screens we can do that based on a destination
                 * string in the intent
                 *      If the user isn't logged in then it is sent to onboarding.
                 * Since an 'else' branch can't be merged together with another
                 * option in a when clause, we will have for two clauses with the
                 * same destination for now.
                 **/
                when (requireActivity().intent.getSessionHandlerNavigationDestination()) {
                    SESSION_HANDLER_DESTINATION_ONBOARDING -> R.id.splash_to_onboarding
                    else -> R.id.splash_to_onboarding
                }
        }
    }

    private fun persistPaymentCardHashSecret() {
        /*
        first encrypt


        U0mvFBrId6oMkJaCC2d31Z6ODPNl2Tf4kiin4cmD19M=
2020-03-25 17:43:30.380 5448-5448/com.bink.wallet.debug E/ConnorDebug: hashSecret first six: U0mvFBrIffBzgHaGuta4lVPTf08puw==
2020-03-25 17:43:30.380 5448-5448/com.bink.wallet.debug E/ConnorDebug: hashSecret last four: U0mvFAkfEvqys2LyMkbVY9J8MfI=
2020-03-25 17:43:30.380 5448-5448/com.bink.wallet.debug E/ConnorDebug: hashSecret month: Ve3IBo6EmjQB2ROHFtBLtSM=
2020-03-25 17:43:30.381 5448-5448/com.bink.wallet.debug E/ConnorDebug: hashSecret year: VUuoFqLq6P6vlIYCH/Az0WVOhvY=


second encrypt
 U0mvFBrId6oMkJaCC2d31Z6ODPNl2Tf4kiin4cmD19M=
2020-03-25 17:54:27.198 9452-9452/com.bink.wallet.debug E/ConnorDebug: hashSecret first six: U0mvFBrIffBzgHaGuta4lVPTf08puw==
2020-03-25 17:54:27.199 9452-9452/com.bink.wallet.debug E/ConnorDebug: hashSecret last four: U0mvFAkfEvqys2LyMkbVY9J8MfI=
2020-03-25 17:54:27.199 9452-9452/com.bink.wallet.debug E/ConnorDebug: hashSecret month: Ve3IBo6EmjQB2ROHFtBLtSM=
2020-03-25 17:54:27.200 9452-9452/com.bink.wallet.debug E/ConnorDebug: hashSecret year: VUuoFqLq6P6vlIYCH/Az0WVOhvY=

third encrypt

 hashSecret pan: Wmt7gbE/49JeqseReBhlzaGqxl2ukU5lPd+XJclT8sVEuKEuqn5C67o3RTCfySaR3ZyJE7z3Xh2iz/6pcybgD9FwGR0heqiBXqpIW1n5ZTTH6JZOBkNm9b3Jj6OhhPeYordaeDL6FsaIVK2MGJuKNfRh5rOqD053rJMfYGH3nSeUtYINOZBESFjWWUrOf8/gRQu/n9LXsBgBPuU05F8LbczKqRu9MmLzmWq1fuKYWIzwkCgJyIFkcp65YTg1xcJv4ZRyXyKbJto7Oyl5B1mteOqR2x+6DVvJ3JKvrCDf+WuQrrDr0XiDfuvh4P4t8iYfv326p9sdUl9ejNffi0onWnn/hrAtPPaAXXAb3oy9myJ+ls7+heF8OlH5qcfCIIz7uZciSSHESeUrjkwXR1L1u2c1VSFAAcnfpM90U3sVVLu4hD1AqbkbTtd5K+jG3ZQ5GzsheNe5fP2mm3zuYTAX2jxbLW3dKsoBMcy/HiIvpF2ch81EHw5U367BYPpC6HH0huUv51yQhI7GsLKtkCT17nymHCRuDTqvBKTJSakNjulwvQJB/Z4y4INyEzTWJfbeefWc+DRLrmoBNPEkfNkH8qrFSs0/PRYrw3vJWp63Rvu8WZLSJpwoZS5mo9FIvXM2Pe+orJmvLynA7igdZ5M+kWsNwZiIIkglUQZVZMe9iqc=
2020-03-25 18:48:18.485 20580-20580/com.bink.wallet.debug E/ConnorDebug: hashSecret first six: elrbC8lMvT/NXx1vC0zZAfG7MyjI+iEs8GDzzsT9PrePfwIc7G6ZInuxd+6MSOXnM3xGFxYhtoxCiHpDpYrZBukGrACKCWWv8WYwOeWPU7yQQeipnYdUHAJYZz/atW1iFkBMOleIaxClg+54W/y0GRcPfI06C2drGDbyLy+39OROyC2W8ZESmJmc3rkd3W6m/IiVbhNvD/k5aXtCo5KdtZRMdq3IdqVZIpWo3rtHhbeJgrpXyyiwNSqBxBNN31zlgJOUeHiex6tmV0nyyty0Z0Np2zl42MIL2SzKd/Aijhcm8CIoQ2uWyUzc7Yquh8zKasbxtbvgqj1I4shagDOLzo0HqlpuUFWpMLkr7nk6M8v6OcaOWMmCcej9MUNnkxirwplmG+32HWHv/AydNXzbs2xWl/DEOF8iJBoecdAFwA0urYMzvMrL5iQWlUIar0BF3qhb4sdymXTTLe1dcnvEHUj3vbjVRdwobVw9JXTVNu9T1YCZ47L1wsP6+tJO9rOuV3OBEVy77irlR6R1DXOWDTtfn37/In4uqU2cX5hOfFGoBcnNfsfp+j5T6Ax2eLIO074PqqxJmFI5box7PSMqjpHrvErvsYIuwASWtATyyonUP2tOUdl260+ZCOL+xFVFPzEp1/Bgod++B4on9MDJvMVkNOlH/+pgApaPwILeb/g=
2020-03-25 18:48:18.566 20580-20580/com.bink.wallet.debug E/ConnorDebug: hashSecret last four: BXrcD7nUCHWl/I6Rt5xtXgCImqzl4DLCSAVL6CM62BG8gCn5jUgueFtcJZQILQc7r7Qh3Qck1EqwyDZk9EjiscAYK/h9pos7dM1fGWmCh9lzv3qd52YYAfnPDmkL86KXL7xvwCDzF0WnyThc1FEe3qYwrUJn2wkWSwwScyTnoelwb+eYWHW+KpEjJNvKTb1k8DbMQ8JtjGZNuaj9cClXqCrtYf5tmVPKJL7awK8zWKQSmX+kXlHqAQAbd91sspRZp7ZQpQw8vQxu8zZsq/BrIoG+jXZkbEca27ey8yAT0iQfCIn9+0oMdz5yJQ2vTf3PVi92ZrskOHYD8SWZxycNxmYWQybPFnZcGtCeU2Mq11CjBDM1QEuhGzeXHorRvltBLjmv0IWzQs+4NNPgjCzWgZzOdFglMKFqhnghbtFup9g22FeVs/jAyffN6zaHVwW/WYW9meJ8gG1IiFyt412EIUu5eLVNNXnBsv269Ar2myez/1UIGlniFM6kczw1d2MCSEfShzlhh4CM4HluHsO6Z5OxoofeCgqdABlkmL6C4M99FB3Y24LjXXsNBVkSQRmrIBB6z2vHjuY/1RUrEcxG3d8qpj7OoG7lAA8vgkFlytWfwMCy5HOQS0AekMo6f135vR79tsAVMJT2SMLhhWjIGbewokEBPz/7FMU8HlAPYoM=
2020-03-25 18:48:18.637 20580-20580/com.bink.wallet.debug E/ConnorDebug: hashSecret month: DuAi0J5V26j/09j5aFbpP4GLtPeCrt2dbYS5C6CqCvuRT5Mpn184KpKpIw/VQmx4IJnZLj2+tQYKVUwUlm0XCnj4wDP49XjwfnOHRnnlwkxPkTlGE2a+fdRX1FX8kP8harDsEKDY4eupD9SAR4u0PGbmMwXkgRxer5GwEOBkhDquVpzaA9Zoh219WIQPK/DtPnKRZhWhhdCSV+f96aWhi9pV9JbXhh/EBpC+di2pAKjujejDVzrQ94b6eWk/xSgpChDYcNhtc05F3Cz71xAQLGVz6hs1gtbqYWvTIknWUSyAcUbFaErKq9F/+zDJd4TFqNyXKL0QjZ6Il0ka6akkDuDkofZIUHJbnyyLff7M7Wb7OwRv+LKe07h8P19AEweNSe1XGpqQBbKIT6nU+G0ZW1m3hJzDHBMOliF4xi7UNgtzYtzNF+8hQtMqYGH3eG8kMJeDYGhAH32BiKp9F5bg8WcIgAj/6qgQcs106tRfEY/UN6Er8/Cs8wpsGbdleZ3FOiZkXOnzX8h8Oo1uW6BrS1i6zx1TBpnfAMWukjRawvhLdKCLJKy8vDE/H/xngnoc62S1VSziKlIqV8AKb0nip20ZdBPObl5u24VmqtrH+vzZnKE3XgZfY1+GTzI/ai8DKgs1kjH/JhxYcPJMkg31iTxB66AgPz3rs1K7vMP07R4=
2020-03-25 18:48:18.706 20580-20580/com.bink.wallet.debug E/ConnorDebug: hashSecret year: EUpu1BgJJIeC+2UTUJwbddb4Naf7lzD9rSSdgwuBQ/gLZVf7Aa8jYfP8MPcqV6X7UiV0doy8Tnwij1e4Q0crBmfrBUhbVFJom+7RYvnWGnIrg9gdI37hwJO9gHjXmtdJht/X1WW3Naf6JlWkbeOWoBRMJeBG8DCxJhih7SKANrnY2Y5ZoR6RsQYGPhay/mEJ1XQoE4kNoj4jn3zqwjCVdrkR6PHBl9oAyr3aVe7HHq2NRdDVjUhNe/VGyhFqz+6Cpa47MDFL+RJZMfSvRp9TOeyH8j5dDM+oryOfTnP9oYYKW9esiO2IXkTBkx/C8MbhV3O2NMC6Zzuz2pD6xyK6WRw0iAia7I3yXhN8m37h00iVSQhAn5Fh2q6JVkP7jcrHVHze2522Qm4FcX3eSequJFRJ1B5ZrbVpy0vN68NowxnT+MOhmJdsmRVD+U5BpjU9RxLpPqB+yU65EUGDwhAtXX9OsQSLFIzzPwefFPKVRxzaHprgxLeIq28uPwwZ/F6mFCj5q30BqFhMOj3k26kP8kDTMAchXvCO4g5jUn0BLm8SGUlvtHTA3BbQm7z1sqD+xzXNbi4SoUvJcsqR2PrSG2fFic5hMZlmD8KYIbimFowCZtBSjX5fPRJXhY25pR9woaYEIWApiFF6t749HQBGmt84DYYsO4yFB7vnKQoxyWc=

         */
//        Log.e("ConnorDebug", "hashSecret pan: " + BinkCore(requireContext()).sessionConfig.encryptSomething(requireContext(), "cc8c1d29664a15f5363cba5a0eb67c38e9319ef003ec25ae6f36f33ce744b00091d417e7d0de73eec95741c42c82f8249a4a15774a13df3702751332b89d6271"))
//        Log.e("ConnorDebug", "hashSecret pan: " + BinkCore(requireContext()).sessionConfig.encryptSomething(requireContext(), "4242424242424242"))
//        Log.e("ConnorDebug", "hashSecret first six: " + BinkCore(requireContext()).sessionConfig.encryptSomething(requireContext(), "424242"))
//        Log.e("ConnorDebug", "hashSecret last four: " + BinkCore(requireContext()).sessionConfig.encryptSomething(requireContext(), "4242"))
//        Log.e("ConnorDebug", "hashSecret month: " + BinkCore(requireContext()).sessionConfig.encryptSomething(requireContext(), "2"))
//        Log.e("ConnorDebug", "hashSecret year: " + BinkCore(requireContext()).sessionConfig.encryptSomething(requireContext(), "2030"))

        //todo these should be encoded base64
        if (BuildConfig.BUILD_TYPE == BuildTypes.RELEASE.toString().toLowerCase(Locale.ENGLISH)) {
            LocalStoreUtils.setAppSharedPref(
                LocalStoreUtils.KEY_PAYMENT_HASH_SECRET, paymentCardHashingProdKey()
            )

            LocalStoreUtils.setAppSharedPref(
                LocalStoreUtils.KEY_ENCRYPT_PAYMENT_PUBLIC_KEY, paymentCardEncryptionPublicKeyProd()
            )
        } else if (BuildConfig.BUILD_TYPE == BuildTypes.BETA.toString().toLowerCase(Locale.ENGLISH)) {
            LocalStoreUtils.setAppSharedPref(
                LocalStoreUtils.KEY_PAYMENT_HASH_SECRET, paymentCardHashingStagingKey()
            )

            LocalStoreUtils.setAppSharedPref(
                LocalStoreUtils.KEY_ENCRYPT_PAYMENT_PUBLIC_KEY, paymentCardEncryptionPublicKeyStaging()
            )
        } else if (BuildConfig.BUILD_TYPE == BuildTypes.DEBUG.toString().toLowerCase(Locale.ENGLISH)) {
            LocalStoreUtils.setAppSharedPref(
                LocalStoreUtils.KEY_PAYMENT_HASH_SECRET, paymentCardHashingDevKey()
            )

            LocalStoreUtils.setAppSharedPref(
                LocalStoreUtils.KEY_ENCRYPT_PAYMENT_PUBLIC_KEY, paymentCardEncryptionPublicKeyDev()
            )
        }
    }
}
