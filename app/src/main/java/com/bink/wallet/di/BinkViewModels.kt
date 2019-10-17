package com.bink.wallet.di

import com.bink.wallet.data.LoginDataDao
import com.bink.wallet.data.MembershipCardDao
import com.bink.wallet.data.MembershipPlanDao
import com.bink.wallet.data.PaymentCardDao
import com.bink.wallet.modal.terms_and_conditions.TermsAndConditionsRepository
import com.bink.wallet.modal.terms_and_conditions.TermsAndConditionsViewModel
import com.bink.wallet.modal.generic.BaseModalViewModel
import com.bink.wallet.network.ApiService
import com.bink.wallet.scenes.add.AddViewModel
import com.bink.wallet.scenes.add_auth_enrol.SignUpViewModel
import com.bink.wallet.scenes.add_join.AddJoinViewModel
import com.bink.wallet.scenes.add_payment_card.PaymentCardRepository
import com.bink.wallet.scenes.add_payment_card.AddPaymentCardViewModel
import com.bink.wallet.scenes.browse_brands.BrowseBrandsViewModel
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.login.LoginViewModel
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsRepository
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsViewModel
import com.bink.wallet.scenes.loyalty_wallet.BarcodeViewModel
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyViewModel
import com.bink.wallet.scenes.loyalty_wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.loyalty_wallet.MaximisedBarcodeViewModel
import com.bink.wallet.scenes.payment_card_details.PaymentCardsDetailsViewModel
import com.bink.wallet.scenes.payment_card_wallet.PaymentCardWalletViewModel
import com.bink.wallet.scenes.pll.PllEmptyViewModel
import com.bink.wallet.scenes.pll.PllRepository
import com.bink.wallet.scenes.pll.PllViewModel
import com.bink.wallet.scenes.settings.SettingsViewModel
import com.bink.wallet.scenes.transactions_screen.TransactionViewModel
import com.bink.wallet.scenes.wallets.WalletsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModules = module {

    single { provideLoginRepository(get(), get()) }
    viewModel { LoginViewModel(get()) }

    single { provideLoyaltyCardRepository(get(), get(), get()) }
    viewModel { LoyaltyViewModel(get()) }

    viewModel { SignUpViewModel(get()) }

    viewModel { BrowseBrandsViewModel() }

    viewModel { BarcodeViewModel() }

    viewModel { AddViewModel() }

    viewModel { AddJoinViewModel() }

    single { provideLoyaltyCardDetailsRepository(get(), get()) }
    viewModel { LoyaltyCardDetailsViewModel(get()) }

    viewModel { PllEmptyViewModel() }

    viewModel { MaximisedBarcodeViewModel() }

    viewModel { TransactionViewModel() }

    single { provideTermsAndConditionsRepository(get()) }
    viewModel { TermsAndConditionsViewModel(get()) }

    single { providePaymentCardRepository(get(), get()) }
    viewModel { AddPaymentCardViewModel(get()) }

    viewModel { PaymentCardWalletViewModel(get(), get()) }

    viewModel { PaymentCardsDetailsViewModel(get()) }

    viewModel { BaseModalViewModel() }

    viewModel { WalletsViewModel(get()) }

    single { providePllRepository(get(), get()) }
    viewModel { PllViewModel(get()) }

    viewModel { SettingsViewModel(get()) }
}

fun provideLoginRepository(
    restApiService: ApiService,
    loginDataDao: LoginDataDao
): LoginRepository =
    LoginRepository(restApiService, loginDataDao)

fun provideLoyaltyCardRepository(
    restApiService: ApiService,
    membershipPlanDao: MembershipPlanDao,
    membershipCardDao: MembershipCardDao
): LoyaltyWalletRepository =
    LoyaltyWalletRepository(restApiService, membershipCardDao, membershipPlanDao)

fun provideLoyaltyCardDetailsRepository(
    restApiService: ApiService,
    membershipCardDao: MembershipCardDao
): LoyaltyCardDetailsRepository =
    LoyaltyCardDetailsRepository(restApiService, membershipCardDao)

fun provideTermsAndConditionsRepository(restApiService: ApiService): TermsAndConditionsRepository =
    TermsAndConditionsRepository(restApiService)

fun providePllRepository(
    restApiService: ApiService,
    paymentCardDao: PaymentCardDao
): PllRepository = PllRepository(restApiService, paymentCardDao)

fun providePaymentCardRepository(
    restApiService: ApiService,
    paymentCardDao: PaymentCardDao
): PaymentCardRepository = PaymentCardRepository(restApiService, paymentCardDao)