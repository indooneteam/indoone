package com.indoone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.indoone.accounts.AccountItem
import com.indoone.accounts.AccountRecord
import com.indoone.accounts.AccountsScreen
import com.indoone.accounts.AccountsViewModel
import com.indoone.accounts.addaccount.AddAccountScreen
import com.indoone.accounts.addaccount.AddAccountViewModel
import com.indoone.accounts.addaccount.entersetupkey.EnterSetupKeyScreen
import com.indoone.accounts.addaccount.entersetupkey.EnterSetupKeyViewModel
import com.indoone.accounts.addaccount.scanqr.QrManualPrefill
import com.indoone.accounts.addaccount.scanqr.QrScanResultHandler
import com.indoone.accounts.addaccount.scanqr.ScanQrScreen
import com.indoone.accounts.addaccount.scanqr.ScanQrViewModel
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountDetailsScreen
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountDetailsViewModel
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountSaveCoordinator
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountSaveRequest
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountRecordMapper
import com.indoone.accounts.storage.AccountRepositoryProvider
import kotlinx.coroutines.launch

private enum class AppRoute {
    ACCOUNTS,
    ADD_ACCOUNT,
    SCAN_QR,
    QR_ACCOUNT_DETAILS,
    ENTER_SETUP_KEY,
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val repository = remember {
                    AccountRepositoryProvider(applicationContext)
                }
                val coroutineScope = rememberCoroutineScope()
                val accountsViewModel: AccountsViewModel = viewModel()
                val addAccountViewModel: AddAccountViewModel = viewModel()
                val scanQrViewModel: ScanQrViewModel = viewModel()
                val enterSetupKeyViewModel: EnterSetupKeyViewModel = viewModel()

                var route by remember { mutableStateOf(AppRoute.ACCOUNTS) }
                var qrAccountDetailsViewModel by remember {
                    mutableStateOf<AccountDetailsViewModel?>(null)
                }

                fun loadAccounts() {
                    coroutineScope.launch {
                        val records = repository.getAll()
                        accountsViewModel.setAccounts(records.toUiAccounts())
                    }
                }

                LaunchedEffect(Unit) {
                    loadAccounts()
                }

                when (route) {
                    AppRoute.ACCOUNTS -> {
                        val state by accountsViewModel.state.collectAsState()

                        AccountsScreen(
                            state = state,
                            onSearchChanged = accountsViewModel::updateSearchQuery,
                            onClearSearch = accountsViewModel::clearSearch,
                            onSort = accountsViewModel::toggleSort,
                            onToggleFavorite = accountsViewModel::toggleFavorite,
                            onAddAccount = {
                                route = AppRoute.ADD_ACCOUNT
                            },
                        )
                    }

                    AppRoute.ADD_ACCOUNT -> {
                        val state by addAccountViewModel.state.collectAsState()

                        AddAccountScreen(
                            state = state,
                            onOtpUriChanged = addAccountViewModel::updateOtpUri,
                            onBack = {
                                route = AppRoute.ACCOUNTS
                            },
                            onScanQr = {
                                route = AppRoute.SCAN_QR
                            },
                            onEnterSetupKey = {
                                route = AppRoute.ENTER_SETUP_KEY
                            },
                            onImportOtpUri = {
                                // Import URI flow is implemented separately.
                            },
                        )
                    }

                    AppRoute.SCAN_QR -> {
                        val state by scanQrViewModel.state.collectAsState()

                        ScanQrScreen(
                            state = state,
                            onCameraPermissionChanged = scanQrViewModel::onCameraPermissionChanged,
                            onCameraStarting = scanQrViewModel::onCameraStarting,
                            onCameraReady = scanQrViewModel::onCameraReady,
                            onCameraError = scanQrViewModel::onCameraError,
                            onQrDetected = { rawValue ->
                                val handler = QrScanResultHandler(
                                    onAccountDetailsReady = { prefill ->
                                        qrAccountDetailsViewModel = AccountDetailsViewModel(
                                            prefill.toQrAccountResult(),
                                        )
                                        scanQrViewModel.onQrDetected()
                                        route = AppRoute.QR_ACCOUNT_DETAILS
                                    },
                                    onInvalidQr = scanQrViewModel::onCameraError,
                                )

                                handler.handle(rawValue)
                            },
                            onCancelScan = {
                                scanQrViewModel.onScanCancelled()
                                route = AppRoute.ADD_ACCOUNT
                            },
                        )
                    }

                    AppRoute.QR_ACCOUNT_DETAILS -> {
                        val detailsViewModel = qrAccountDetailsViewModel

                        if (detailsViewModel == null) {
                            route = AppRoute.ADD_ACCOUNT
                        } else {
                            val state by detailsViewModel.state.collectAsState()

                            AccountDetailsScreen(
                                state = state,
                                onNameChanged = detailsViewModel::onNameChanged,
                                onEmailChanged = detailsViewModel::onEmailChanged,
                                onSecretChanged = detailsViewModel::onSecretChanged,
                                onDigitsChanged = detailsViewModel::onDigitsChanged,
                                onPeriodChanged = detailsViewModel::onPeriodChanged,
                                onAlgorithmChanged = detailsViewModel::onAlgorithmChanged,
                                onBack = {
                                    route = AppRoute.SCAN_QR
                                },
                                onSave = {
                                    val validation = detailsViewModel.prepareSave()

                                    validation.onFailure { error ->
                                        detailsViewModel.onSaveFailed(
                                            error.message ?: "Invalid account details.",
                                        )
                                    }.onSuccess {
                                        detailsViewModel.onSaveStarted()

                                        coroutineScope.launch {
                                            val result = AccountSaveCoordinator(repository)
                                                .save(state)

                                            result.onSuccess {
                                                detailsViewModel.onSaveCompleted()
                                                loadAccounts()
                                                route = AppRoute.ACCOUNTS
                                            }.onFailure { error ->
                                                detailsViewModel.onSaveFailed(
                                                    error.message ?: "Could not save account.",
                                                )
                                            }
                                        }
                                    }
                                },
                            )
                        }
                    }

                    AppRoute.ENTER_SETUP_KEY -> {
                        val state by enterSetupKeyViewModel.state.collectAsState()

                        EnterSetupKeyScreen(
                            state = state,
                            onNameChanged = enterSetupKeyViewModel::onNameChanged,
                            onEmailChanged = enterSetupKeyViewModel::onEmailChanged,
                            onSecretChanged = enterSetupKeyViewModel::onSecretChanged,
                            onDigitsChanged = enterSetupKeyViewModel::onDigitsChanged,
                            onPeriodChanged = enterSetupKeyViewModel::onPeriodChanged,
                            onAlgorithmChanged = enterSetupKeyViewModel::onAlgorithmChanged,
                            onBack = {
                                route = AppRoute.ADD_ACCOUNT
                            },
                            onSave = {
                                val validation = enterSetupKeyViewModel.prepareSave()

                                validation.onFailure { error ->
                                    enterSetupKeyViewModel.onSaveFailed(
                                        error.message ?: "Invalid account details.",
                                    )
                                }.onSuccess { request ->
                                    enterSetupKeyViewModel.onSaveStarted()

                                    coroutineScope.launch {
                                        val result = saveManualAccount(
                                            repository = repository,
                                            request = request,
                                        )

                                        result.onSuccess {
                                            enterSetupKeyViewModel.onSaveCompleted()
                                            loadAccounts()
                                            route = AppRoute.ACCOUNTS
                                        }.onFailure { error ->
                                            enterSetupKeyViewModel.onSaveFailed(
                                                error.message ?: "Could not save account.",
                                            )
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

private suspend fun saveManualAccount(
    repository: com.indoone.accounts.AccountRepository,
    request: AccountSaveRequest,
): Result<AccountRecord> {
    return runCatching {
        val record = AccountRecordMapper.from(request)
        repository.save(record)
        record
    }
}

private fun QrManualPrefill.toQrAccountResult(): com.indoone.accounts.addaccount.scanqr.QrAccountResult {
    return com.indoone.accounts.addaccount.scanqr.QrAccountResult(
        name = name,
        email = email,
        secret = secret,
        algorithm = algorithm,
        digits = digits,
        period = period,
        provider = provider,
        service = service,
    )
}

private fun List<AccountRecord>.toUiAccounts(): List<AccountItem> {
    return map { record ->
        AccountItem(
            id = record.id,
            name = record.name,
            email = record.email,
            code = "------",
            secondsRemaining = record.period,
            periodSeconds = record.period,
            favorite = record.favorite,
        )
    }
}
