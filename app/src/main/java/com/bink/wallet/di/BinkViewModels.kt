package com.bink.wallet.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.bink.wallet.MainViewModel
import com.bink.wallet.data.*
import com.bink.wallet.di.qualifier.network.NetworkQualifiers
import com.bink.wallet.modal.card_terms_and_conditions.AddPaymentCardRepository
import com.bink.wallet.modal.card_terms_and_conditions.CardTermsAndConditionsViewModel
import com.bink.wallet.modal.generic.BaseModalViewModel
import com.bink.wallet.modal.terms_and_conditions.TermsAndConditionsViewModel
import com.bink.wallet.network.ApiService
import com.bink.wallet.network.ApiSpreedly
import com.bink.wallet.scenes.add.AddViewModel
import com.bink.wallet.scenes.add_auth_enrol.view_models.AddAuthViewModel
import com.bink.wallet.scenes.add_auth_enrol.view_models.AddCardViewModel
import com.bink.wallet.scenes.add_auth_enrol.view_models.GetNewCardViewModel
import com.bink.wallet.scenes.add_auth_enrol.view_models.GhostCardViewModel
import com.bink.wallet.scenes.add_join.AddJoinRequestPaymentCardViewModel
import com.bink.wallet.scenes.add_join.AddJoinViewModel
import com.bink.wallet.scenes.add_payment_card.AddPaymentCardViewModel
import com.bink.wallet.scenes.browse_brands.BrowseBrandsViewModel
import com.bink.wallet.scenes.dynamic_actions.DynamicActionViewModel
import com.bink.wallet.scenes.forgot_password.ForgotPasswordViewModel
import com.bink.wallet.scenes.login.LoginRepository
import com.bink.wallet.scenes.login.LoginViewModel
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsRepository
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardDetailsViewModel
import com.bink.wallet.scenes.loyalty_details.LoyaltyCardRewardsHistoryViewModel
import com.bink.wallet.scenes.loyalty_details.VoucherDetailsViewModel
import com.bink.wallet.scenes.loyalty_wallet.barcode.BarcodeViewModel
import com.bink.wallet.scenes.loyalty_wallet.barcode.MaximisedBarcodeViewModel
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyViewModel
import com.bink.wallet.scenes.loyalty_wallet.wallet.LoyaltyWalletRepository
import com.bink.wallet.scenes.onboarding.OnboardingViewModel
import com.bink.wallet.scenes.payment_card_details.PaymentCardsDetailsViewModel
import com.bink.wallet.scenes.payment_card_wallet.PaymentCardWalletViewModel
import com.bink.wallet.scenes.pll.PaymentWalletRepository
import com.bink.wallet.scenes.pll.PllEmptyViewModel
import com.bink.wallet.scenes.pll.PllViewModel
import com.bink.wallet.scenes.preference.PreferencesViewModel
import com.bink.wallet.scenes.settings.*
import com.bink.wallet.scenes.sign_up.SignUpViewModel
import com.bink.wallet.scenes.sign_up.continue_with_email.ContinueWithEmailViewModel
import com.bink.wallet.scenes.sign_up.continue_with_email.check_inbox.CheckInboxViewModel
import com.bink.wallet.scenes.sign_up.continue_with_email.magic_link_result.MagicLinkResultViewModel
import com.bink.wallet.scenes.splash.SplashViewModel
import com.bink.wallet.scenes.transactions_screen.TransactionViewModel
import com.bink.wallet.scenes.wallets.WalletsViewModel
import com.bink.wallet.scenes.who_we_are.WhoWeAreViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModules = module {

    single { provideLoginRepository(get(NetworkQualifiers.BinkApiInterface), get()) }
    single { provideUserRepository(get(NetworkQualifiers.BinkApiInterface)) }
    single { provideDataStoreSource(get()) }
    single { provideDataStore(get()) }

    viewModel { LoginViewModel(get(), get(), get()) }

    single {
        provideLoyaltyCardRepository(
            get(NetworkQualifiers.BinkApiInterface),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel { LoyaltyViewModel(get(), get()) }

    viewModel { AddAuthViewModel(get(), get()) }
    viewModel { AddCardViewModel(get(), get()) }
    viewModel { GetNewCardViewModel(get(), get()) }
    viewModel { GhostCardViewModel(get(), get()) }

    viewModel { BrowseBrandsViewModel() }

    viewModel { BarcodeViewModel() }

    viewModel { AddViewModel(get(), get()) }

    viewModel { AddJoinViewModel(get()) }

    viewModel { AddJoinRequestPaymentCardViewModel() }

    single {
        provideLoyaltyCardDetailsRepository(
            get(NetworkQualifiers.BinkApiInterface),
            get(),
            get()
        )
    }
    viewModel { LoyaltyCardDetailsViewModel(get()) }

    viewModel { VoucherDetailsViewModel() }

    viewModel { LoyaltyCardRewardsHistoryViewModel() }

    viewModel { PllEmptyViewModel(get()) }

    viewModel { MaximisedBarcodeViewModel() }

    viewModel { TransactionViewModel() }

    viewModel { DebugMenuViewModel(get(), get(), get()) }

    viewModel { DebugColorSwatchesViewModel(get()) }

    viewModel { TermsAndConditionsViewModel() }

    viewModel { AddPaymentCardViewModel(get()) }

    viewModel { PaymentCardWalletViewModel(get(), get()) }

    viewModel { PaymentCardsDetailsViewModel(get(), get()) }

    viewModel { BaseModalViewModel() }

    viewModel { WalletsViewModel(get(), get()) }

    single { providePllRepository(get(NetworkQualifiers.BinkApiInterface), get(), get()) }
    viewModel { PllViewModel(get()) }

    viewModel { SettingsViewModel(get(), get(), get(), get(),get()) }

    viewModel { DeleteAccountViewModel(get()) }

    viewModel { SignUpViewModel(get(), get()) }

    viewModel { WhoWeAreViewModel() }

    viewModel { DynamicActionViewModel(get()) }

    single {
        provideAddPaymentCardRepository(
            get(NetworkQualifiers.BinkApiInterface),
            get(NetworkQualifiers.SpreedlyApiInterface),
            get(),
            get(),
            get()
        )
    }
    viewModel { CardTermsAndConditionsViewModel(get()) }

    viewModel { ForgotPasswordViewModel(get()) }

    viewModel { PreferencesViewModel(get()) }

    viewModel { OnboardingViewModel(get(), get()) }

    viewModel { MainViewModel(get()) }

    viewModel { SplashViewModel(get(), get()) }

    viewModel { ContinueWithEmailViewModel(get()) }

    viewModel { MagicLinkResultViewModel(get(), get(), get(), get()) }

    viewModel { CheckInboxViewModel(get()) }
}

fun provideLoginRepository(
    restApiService: ApiService,
    binkDatabase: BinkDatabase
): LoginRepository =
    LoginRepository(restApiService, binkDatabase)

fun provideLoyaltyCardRepository(
    restApiService: ApiService,
    membershipPlanDao: MembershipPlanDao,
    membershipCardDao: MembershipCardDao,
    bannersDisplayDao: BannersDisplayDao,
    paymentCardDao: PaymentCardDao
): LoyaltyWalletRepository =
    LoyaltyWalletRepository(
        restApiService,
        membershipCardDao,
        membershipPlanDao,
        bannersDisplayDao,
        paymentCardDao
    )

fun provideLoyaltyCardDetailsRepository(
    restApiService: ApiService,
    membershipCardDao: MembershipCardDao,
    paymentCardDao: PaymentCardDao
): LoyaltyCardDetailsRepository =
    LoyaltyCardDetailsRepository(restApiService, membershipCardDao, paymentCardDao)

fun providePllRepository(
    restApiService: ApiService,
    paymentCardDao: PaymentCardDao,
    membershipCardDao: MembershipCardDao
): PaymentWalletRepository =
    PaymentWalletRepository(restApiService, paymentCardDao, membershipCardDao)

fun provideUserRepository(
    restApiService: ApiService
): UserRepository = UserRepository(restApiService)

fun provideAddPaymentCardRepository(
    restApiService: ApiService,
    spreedlyApiService: ApiSpreedly,
    paymentCardDao: PaymentCardDao,
    membershipCardDao: MembershipCardDao,
    membershipPlanDao: MembershipPlanDao
): AddPaymentCardRepository =
    AddPaymentCardRepository(
        restApiService,
        spreedlyApiService,
        paymentCardDao,
        membershipCardDao,
        membershipPlanDao
    )

fun provideDataStore(context: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
        produceFile = {
            context.preferencesDataStoreFile("DS_NAME")
        }
    )

fun provideDataStoreSource(dataStore: DataStore<Preferences>): DataStoreSourceImpl =
    DataStoreSourceImpl(dataStore)