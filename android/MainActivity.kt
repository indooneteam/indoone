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
import com.indoone.accounts.accounts.AccountDetailActions
import com.indoone.accounts.accounts.AccountRemovalService
import com.indoone.accounts.accounts.edit.EditAccountScreen
import com.indoone.accounts.accounts.edit.EditAccountViewModel
import com.indoone.accounts.accounts.list.AccountDetailsScreen
import com.indoone.accounts.accounts.list.AccountTotpGenerator
import com.indoone.accounts.addaccount.AddAccountScreen
import com.indoone.accounts.addaccount.AddAccountViewModel
import com.indoone.accounts.addaccount.entersetupkey.EnterSetupKeyScreen
import com.indoone.accounts.addaccount.entersetupkey.EnterSetupKeyViewModel
import com.indoone.accounts.addaccount.importuri.ImportOtpUriScreen
import com.indoone.accounts.addaccount.importuri.ImportOtpUriViewModel
import com.indoone.accounts.addaccount.scanqr.QrManualPrefill
import com.indoone.accounts.addaccount.scanqr.QrScanResultHandler
import com.indoone.accounts.addaccount.scanqr.ScanQrScreen
import com.indoone.accounts.addaccount.scanqr.ScanQrViewModel
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountDetailsScreen
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountDetailsViewModel
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountSaveCoordinator
import com.indoone.accounts.search.SearchScreen
import com.indoone.accounts.search.SearchViewModel
import com.indoone.accounts.storage.AccountRepositoryProvider
import com.indoone.lobby.LobbyScreen
import com.indoone.lobby.LobbyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class AppRoute {
    ACCOUNTS,
    SEARCH,
    LOBBY,
    ADD_ACCOUNT,
    SCAN_QR,
    QR_ACCOUNT_DETAILS,
    ENTER_SETUP_KEY,
    IMPORT_OTP_URI,
    IMPORT_ACCOUNT_DETAILS,
    ACCOUNT_DETAILS,
    EDIT_ACCOUNT,
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val repository = remember { AccountRepositoryProvider(applicationContext) }
                val detailActions = remember { AccountDetailActions(repository, applicationContext) }
                val removalService = remember { AccountRemovalService(repository) }
                val coroutineScope = rememberCoroutineScope()
                val accountsViewModel: AccountsViewModel = viewModel()
                val addAccountViewModel: AddAccountViewModel = viewModel()
                val scanQrViewModel: ScanQrViewModel = viewModel()
                val enterSetupKeyViewModel: EnterSetupKeyViewModel = viewModel()
                val importOtpUriViewModel: ImportOtpUriViewModel = viewModel()
                val searchViewModel: SearchViewModel = viewModel()
                val lobbyViewModel: LobbyViewModel = viewModel()

                var route by remember { mutableStateOf(AppRoute.ACCOUNTS) }
                var selectedAccount by remember { mutableStateOf<AccountRecord?>(null) }
                var qrAccountDetailsViewModel by remember { mutableStateOf<AccountDetailsViewModel?>(null) }
                var importAccountDetailsViewModel by remember { mutableStateOf<AccountDetailsViewModel?>(null) }
                var editAccountViewModel by remember { mutableStateOf<EditAccountViewModel?>(null) }

                fun loadAccounts() {
                    coroutineScope.launch {
                        val records = repository.getAll()
                        val uiAccounts = records.toUiAccounts()
                        accountsViewModel.setAccounts(uiAccounts)
                        searchViewModel.setAccounts(uiAccounts)
                    }
                }

                LaunchedEffect(Unit) { loadAccounts() }

                LaunchedEffect(repository) {
                    while (true) {
                        val uiAccounts = repository.getAll().toUiAccounts(System.currentTimeMillis())
                        accountsViewModel.setAccounts(uiAccounts)
                        searchViewModel.setAccounts(uiAccounts)
                        delay(1_000L)
                    }
                }

                when (route) {
                    AppRoute.ACCOUNTS -> {
                        val state by accountsViewModel.state.collectAsState()
                        AccountsScreen(
                            state = state,
                            onSearchChanged = accountsViewModel::updateSearchQuery,
                            onClearSearch = accountsViewModel::clearSearch,
                            onSort = accountsViewModel::toggleSort,
                            onToggleFavorite = { id ->
                                coroutineScope.launch {
                                    val account = repository.getAll().firstOrNull { it.id == id }
                                    if (account != null) {
                                        detailActions.setFavorite(account, !account.favorite)
                                        loadAccounts()
                                    }
                                }
                            },
                            onAccountClick = { item ->
                                coroutineScope.launch {
                                    selectedAccount = repository.getAll().firstOrNull { it.id == item.id }
                                    if (selectedAccount != null) route = AppRoute.ACCOUNT_DETAILS
                                }
                            },
                            onAddAccount = {
                                addAccountViewModel.clearImportUri()
                                route = AppRoute.ADD_ACCOUNT
                            },
                            onSearchClick = {
                                searchViewModel.setAccounts(accountsViewModel.state.value.accounts)
                                route = AppRoute.SEARCH
                            },
                            onLobbyClick = { route = AppRoute.LOBBY },
                        )
                    }

                    AppRoute.SEARCH -> {
                        val state by searchViewModel.state.collectAsState()
                        SearchScreen(
                            state = state,
                            onQueryChanged = searchViewModel::updateQuery,
                            onClear = searchViewModel::clear,
                            onBack = {
                                searchViewModel.clear()
                                route = AppRoute.ACCOUNTS
                            },
                            onAccountClick = { item ->
                                coroutineScope.launch {
                                    selectedAccount = repository.getAll().firstOrNull { it.id == item.id }
                                    if (selectedAccount != null) route = AppRoute.ACCOUNT_DETAILS
                                }
                            },
                        )
                    }

                    AppRoute.LOBBY -> {
                        val state by lobbyViewModel.state.collectAsState()
                        LobbyScreen(
                            state = state,
                            onAccountsClick = { route = AppRoute.ACCOUNTS },
                            onLobbyClick = { route = AppRoute.LOBBY },
                        )
                    }

                    AppRoute.ACCOUNT_DETAILS -> {
                        val account = selectedAccount
                        val uiAccount = account?.let { record ->
                            accountsViewModel.state.value.accounts.firstOrNull { it.id == record.id }
                        }

                        if (account == null || uiAccount == null) {
                            route = AppRoute.ACCOUNTS
                        } else {
                            AccountDetailsScreen(
                                account = account,
                                code = uiAccount.code,
                                secondsRemaining = uiAccount.secondsRemaining,
                                onBack = { route = AppRoute.ACCOUNTS },
                                onEdit = {
                                    editAccountViewModel = EditAccountViewModel(account)
                                    route = AppRoute.EDIT_ACCOUNT
                                },
                                onCopy = {
                                    detailActions.copyCode(uiAccount.code)
                                },
                                onToggleFavorite = {
                                    coroutineScope.launch {
                                        detailActions.setFavorite(account, !account.favorite)
                                            .onSuccess { updated ->
                                                selectedAccount = updated
                                                loadAccounts()
                                            }
                                    }
                                },
                                onDelete = {
                                    coroutineScope.launch {
                                        removalService.remove(account.id)
                                            .onSuccess {
                                                selectedAccount = null
                                                loadAccounts()
                                                route = AppRoute.ACCOUNTS
                                            }
                                    }
                                },
                            )
                        }
                    }

                    AppRoute.EDIT_ACCOUNT -> {
                        val editViewModel = editAccountViewModel
                        if (editViewModel == null) {
                            route = AppRoute.ACCOUNT_DETAILS
                        } else {
                            val state by editViewModel.state.collectAsState()
                            EditAccountScreen(
                                state = state,
                                onNameChanged = editViewModel::onNameChanged,
                                onEmailChanged = editViewModel::onEmailChanged,
                                onSecretChanged = editViewModel::onSecretChanged,
                                onDigitsChanged = editViewModel::onDigitsChanged,
                                onPeriodChanged = editViewModel::onPeriodChanged,
                                onAlgorithmChanged = editViewModel::onAlgorithmChanged,
                                onBack = { route = AppRoute.ACCOUNT_DETAILS },
                                onSave = {
                                    editViewModel.prepareSave()
                                        .onFailure { error ->
                                            editViewModel.onSaveFailed(error.message ?: "Invalid account details.")
                                        }
                                        .onSuccess { request ->
                                            editViewModel.onSaveStarted()
                                            coroutineScope.launch {
                                                runCatching {
                                                    val old = selectedAccount ?: error("Account not found.")
                                                    val updated = old.copy(
                                                        name = request.name,
                                                        email = request.email,
                                                        secret = request.secret,
                                                        digits = request.digits,
                                                        period = request.period,
                                                        algorithm = request.algorithm,
                                                        updatedAt = System.currentTimeMillis(),
                                                    )
                                                    repository.save(updated)
                                                    selectedAccount = updated
                                                }.onSuccess {
                                                    editViewModel.onSaveCompleted()
                                                    loadAccounts()
                                                    route = AppRoute.ACCOUNT_DETAILS
                                                }.onFailure { error ->
                                                    editViewModel.onSaveFailed(error.message ?: "Could not save account.")
                                                }
                                            }
                                        }
                                },
                            )
                        }
                    }

                    AppRoute.ADD_ACCOUNT -> {
                        val state by addAccountViewModel.state.collectAsState()
                        AddAccountScreen(
                            state = state,
                            onOtpUriChanged = addAccountViewModel::updateOtpUri,
                            onBack = { route = AppRoute.ACCOUNTS },
                            onScanQr = { route = AppRoute.SCAN_QR },
                            onEnterSetupKey = { route = AppRoute.ENTER_SETUP_KEY },
                            onImportOtpUri = { uri ->
                                importOtpUriViewModel.onUriChanged(uri)
                                route = AppRoute.IMPORT_OTP_URI
                            },
                        )
                    }

                    AppRoute.IMPORT_OTP_URI -> {
                        val state by importOtpUriViewModel.state.collectAsState()
                        ImportOtpUriScreen(
                            state = state,
                            onUriChanged = importOtpUriViewModel::onUriChanged,
                            onBack = { route = AppRoute.ADD_ACCOUNT },
                            onContinue = {
                                importOtpUriViewModel.parse().onSuccess { result ->
                                    importAccountDetailsViewModel = AccountDetailsViewModel(result)
                                    route = AppRoute.IMPORT_ACCOUNT_DETAILS
                                }
                            },
                        )
                    }

                    AppRoute.IMPORT_ACCOUNT_DETAILS -> {
                        val detailsViewModel = importAccountDetailsViewModel
                        if (detailsViewModel == null) {
                            route = AppRoute.IMPORT_OTP_URI
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
                                onBack = { route = AppRoute.IMPORT_OTP_URI },
                                onSave = { saveQrLikeAccount(detailsViewModel, state, repository, coroutineScope) { loadAccounts(); route = AppRoute.ACCOUNTS } },
                            )
                        }
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
                                QrScanResultHandler(
                                    onAccountDetailsReady = { prefill ->
                                        qrAccountDetailsViewModel = AccountDetailsViewModel(prefill.toQrAccountResult())
                                        scanQrViewModel.onQrDetected()
                                        route = AppRoute.QR_ACCOUNT_DETAILS
                                    },
                                    onInvalidQr = scanQrViewModel::onCameraError,
                                ).handle(rawValue)
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
                                onBack = { route = AppRoute.SCAN_QR },
                                onSave = { saveQrLikeAccount(detailsViewModel, state, repository, coroutineScope) { loadAccounts(); route = AppRoute.ACCOUNTS } },
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
                            onBack = { route = AppRoute.ADD_ACCOUNT },
                            onSave = {
                                enterSetupKeyViewModel.prepareSave()
                                    .onFailure { error -> enterSetupKeyViewModel.onSaveFailed(error.message ?: "Invalid account details.") }
                                    .onSuccess { request ->
                                        enterSetupKeyViewModel.onSaveStarted()
                                        coroutineScope.launch {
                                            runCatching {
                                                val record = AccountRecordMapper.from(request)
                                                repository.save(record)
                                            }.onSuccess {
                                                enterSetupKeyViewModel.onSaveCompleted()
                                                loadAccounts()
                                                route = AppRoute.ACCOUNTS
                                            }.onFailure { error -> enterSetupKeyViewModel.onSaveFailed(error.message ?: "Could not save account.") }
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

private fun saveQrLikeAccount(
    viewModel: AccountDetailsViewModel,
    state: com.indoone.accounts.addaccount.scanqr.accountdetails.AccountDetailsState,
    repository: com.indoone.accounts.AccountRepository,
    scope: kotlinx.coroutines.CoroutineScope,
    onSuccess: () -> Unit,
) {
    viewModel.prepareSave()
        .onFailure { error -> viewModel.onSaveFailed(error.message ?: "Invalid account details.") }
        .onSuccess {
            viewModel.onSaveStarted()
            scope.launch {
                AccountSaveCoordinator(repository).save(state)
                    .onSuccess { viewModel.onSaveCompleted(); onSuccess() }
                    .onFailure { error -> viewModel.onSaveFailed(error.message ?: "Could not save account.") }
            }
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

private fun List<AccountRecord>.toUiAccounts(nowMillis: Long = System.currentTimeMillis()): List<AccountItem> {
    return map { record ->
        val period = record.period.coerceAtLeast(1)
        val nowSeconds = nowMillis / 1000L
        val elapsed = (nowSeconds % period).toInt()
        val secondsRemaining = (period - elapsed).coerceIn(1, period)
        val code = runCatching {
            AccountTotpGenerator.generate(record.secret, nowMillis, period, record.digits, record.algorithm)
        }.getOrDefault("------")
        AccountItem(
            id = record.id,
            name = record.name,
            email = record.email,
            code = code,
            secondsRemaining = secondsRemaining,
            periodSeconds = period,
            favorite = record.favorite,
        )
    }
}
