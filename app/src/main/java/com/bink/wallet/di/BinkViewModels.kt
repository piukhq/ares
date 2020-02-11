package com.bink.wallet.di

import com.bink.wallet.data.*
import com.bink.wallet.modal.card_terms_and_conditions.AddPaymentCardRepository
import com.bink.wallet.modal.card_terms_and_conditions.CardTermsAndConditionsViewModel
import com.bink.wallet.modal.generic.BaseModalViewModel
import com.bink.wallet.modal.terms_and_conditions.TermsAndConditionsRepository
import com.bink.wallet.modal.terms_and_conditions.TermsAndConditionsViewModel
import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.add.AddViewModel
import com.bink.wallet.scenes.add_auth_enrol.AddAuthViewModel
import com.bink.wallet.scenes.add_join.AddJoinViewModel
import com.bink.wallet.scenes.add_payment_card.AddPaymentCardViewModel
import com.bink.wallet.scenes.browse_brands.BrowseBrandsViewModel
import com.bink.wallet.scenes.forgot_password.ForgotPasswordViewModel
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.login.LoginViewModel
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsRepository
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsViewModel
import com.bink.wallet.scenes.loyalty_wallet.BarcodeViewModel
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyViewModel
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.loyalty_wallet.MaximisedBarcodeViewModel
import com.bink.wallet.scenes.onboarding.OnboardingViewModel
import com.bink.wallet.scenes.payment_card_details.PaymentCardsDetailsViewModel
import com.bink.wallet.scenes.payment_card_wallet.PaymentCardWalletViewModel
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import com.bink.wallet.scenes.pll.PllEmptyViewModel
import com.bink.wallet.scenes.pll.PllViewModel
import com.bink.wallet.scenes.preference.PreferencesViewModel
import com.bink.wallet.scenes.registration.AcceptTCViewModel
import com.bink.wallet.scenes.registration.AddEmailViewModel
import com.bink.wallet.scenes.settings.SettingsViewModel
import com.bink.wallet.scenes.sign_up.SignUpViewModel
import com.bink.wallet.scenes.transactions_screen.TransactionViewModel
import com.bink.wallet.scenes.wallets.WalletsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModules = module {

    single { provideLoginRepository(get(), get()) }
    viewModel { LoginViewModel(get()) }

    single { provideLoyaltyCardRepository(get(), get(), get(), get()) }
    viewModel { LoyaltyViewModel(get()) }

    viewModel { AddAuthViewModel(get()) }

    viewModel { BrowseBrandsViewModel() }

    viewModel { BarcodeViewModel() }

    viewModel { AddViewModel() }

    viewModel { AddJoinViewModel() }

    single { provideLoyaltyCardDetailsRepository(get(), get(), get()) }
    viewModel { LoyaltyCardDetailsViewModel(get()) }

    viewModel { PllEmptyViewModel() }

    viewModel { MaximisedBarcodeViewModel() }

    viewModel { TransactionViewModel() }

    single { provideTermsAndConditionsRepository(get()) }
    viewModel { TermsAndConditionsViewModel(get()) }

    viewModel { AddPaymentCardViewModel(get()) }

    viewModel { PaymentCardWalletViewModel(get(), get()) }

    viewModel { PaymentCardsDetailsViewModel(get(), get()) }

    viewModel { BaseModalViewModel() }

    viewModel { WalletsViewModel(get(), get()) }

    single { providePllRepository(get(), get()) }
    viewModel { PllViewModel(get()) }

    viewModel { SettingsViewModel(get(), get(), get()) }

    viewModel { SignUpViewModel(get()) }

    single { provideCardTermsAndConditionsRepository(get(), get(), get(), get()) }
    viewModel { CardTermsAndConditionsViewModel(get()) }

    viewModel { AcceptTCViewModel(get()) }

    viewModel { AddEmailViewModel() }

    viewModel { ForgotPasswordViewModel(get()) }

    viewModel { PreferencesViewModel(get()) }

    viewModel { OnboardingViewModel(get(), get()) }
}

fun provideLoginRepository(
    restApiService: ApiService,
    loginDataDao: LoginDataDao
): LoginRepository =
    LoginRepository(restApiService, loginDataDao)

fun provideLoyaltyCardRepository(
    restApiService: ApiService,
    membershipPlanDao: MembershipPlanDao,
    membershipCardDao: MembershipCardDao,
    bannersDisplayDao: BannersDisplayDao
): LoyaltyWalletRepository =
    LoyaltyWalletRepository(restApiService, membershipCardDao, membershipPlanDao, bannersDisplayDao)

fun provideLoyaltyCardDetailsRepository(
    restApiService: ApiService,
    membershipCardDao: MembershipCardDao,
    paymentCardDao: PaymentCardDao
): LoyaltyCardDetailsRepository =
    LoyaltyCardDetailsRepository(restApiService, membershipCardDao, paymentCardDao)

fun provideTermsAndConditionsRepository(restApiService: ApiService): TermsAndConditionsRepository =
    TermsAndConditionsRepository(restApiService)

fun providePllRepository(
    restApiService: ApiService,
    paymentCardDao: PaymentCardDao
): PaymentWalletRepository = PaymentWalletRepository(restApiService, paymentCardDao)

fun provideCardTermsAndConditionsRepository(
    restApiService: ApiService,
    paymentCardDao: PaymentCardDao,
    membershipCardDao: MembershipCardDao,
    membershipPlanDao: MembershipPlanDao
): AddPaymentCardRepository =
    AddPaymentCardRepository(
        restApiService,
        paymentCardDao,
        membershipCardDao,
        membershipPlanDao
    )