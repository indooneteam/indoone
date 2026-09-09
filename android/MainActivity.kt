package com.indoone

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.indoone.accounts.search.SearchViewModel
import com.indoone.accounts.storage.AccountRepositoryProvider
import com.indoone.connect.ConnectScreen
import com.indoone.lobby.LobbyScreen
import com.indoone.lobby.LobbyViewModel
import com.indoone.menu.MenuDrawer
import com.indoone.menu.favorites.FavoritesScreen
import com.indoone.menu.privacypolicy.PrivacyPolicyScreen
import com.indoone.menu.security.SecurityScreen
import com.indoone.settings.SettingsScreen
import com.indoone.settings.profile.ProfileScreen
import com.indoone.settings.profile.ProfileViewModel
import com.indoone.settings.profile.change_email.ChangeEmailScreen
import com.indoone.settings.profile.change_mobile_number.ChangeMobileNumberScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class AppRoute {
    ACCOUNTS,
    LOBBY,
    CONNECT,
    SETTINGS,
    PROFILE,
    CHANGE_MOBILE,
    CHANGE_EMAIL,
    FAVORITES,
    SECURITY,
    PRIVACY_POLICY,
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
                val profileViewModel: ProfileViewModel = viewModel()
                var route by remember { mutableStateOf(AppRoute.ACCOUNTS) }
                var menuOpen by remember { mutableStateOf(false) }
                var selectedAccount by remember { mutableStateOf<AccountRecord?>(null) }
                var qrAccountDetailsViewModel by remember { mutableStateOf<AccountDetailsViewModel?>(null) }
                var importAccountDetailsViewModel by remember { mutableStateOf<AccountDetailsViewModel?>(null) }
                var editAccountViewModel by remember { mutableStateOf<EditAccountViewModel?>(null) }

                fun navigate(target: AppRoute) {
                    menuOpen = false
                    route = target
                }

                fun menuToast(message: String) {
                    menuOpen = false
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }

                fun loadAccounts() {
                    coroutineScope.launch {
                        runCatching {
                            repository.getAll()
                        }.onSuccess { records ->
                            val uiAccounts = records.toUiAccounts()
                            accountsViewModel.setAccounts(uiAccounts)
                            searchViewModel.setAccounts(uiAccounts)
                        }.onFailure { error ->
                            Toast.makeText(
                                this@MainActivity,
                                error.message ?: "Could not load accounts.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                }

                LaunchedEffect(Unit) { loadAccounts() }
                LaunchedEffect(repository) {
                    while (true) {
                        runCatching { repository.getAll() }
                            .onSuccess { records ->
                                val uiAccounts = records.toUiAccounts(System.currentTimeMillis())
                                accountsViewModel.setAccounts(uiAccounts)
                                searchViewModel.setAccounts(uiAccounts)
                            }
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
                                    repository.getAll().firstOrNull { it.id == id }?.let {
                                        detailActions.setFavorite(it, !it.favorite)
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
                            onSearchClick = {},
                            onLobbyClick = { navigate(AppRoute.LOBBY) },
                            onConnectClick = { navigate(AppRoute.CONNECT) },
                            onSettingsClick = { navigate(AppRoute.SETTINGS) },
                            onMenuClick = { menuOpen = true },
                        )
                    }

                    AppRoute.LOBBY -> {
                        val state by lobbyViewModel.state.collectAsState()
                        LobbyScreen(
                            state = state,
                            onMenuClick = { menuOpen = true },
                            onAccountsClick = { navigate(AppRoute.ACCOUNTS) },
                            onLobbyClick = { navigate(AppRoute.LOBBY) },
                            onConnectClick = { navigate(AppRoute.CONNECT) },
                            onSettingsClick = { navigate(AppRoute.SETTINGS) },
                        )
                    }

                    AppRoute.CONNECT -> ConnectScreen(
                        onMenuClick = { menuOpen = true },
                        onAccountsClick = { navigate(AppRoute.ACCOUNTS) },
                        onLobbyClick = { navigate(AppRoute.LOBBY) },
                        onConnectClick = { navigate(AppRoute.CONNECT) },
                        onSettingsClick = { navigate(AppRoute.SETTINGS) },
                    )

                    AppRoute.SETTINGS -> SettingsScreen(
                        onMenuClick = { menuOpen = true },
                        onProfileClick = {
                            profileViewModel.loadProfile()
                            navigate(AppRoute.PROFILE)
                        },
                        onAccountsClick = { navigate(AppRoute.ACCOUNTS) },
                        onLobbyClick = { navigate(AppRoute.LOBBY) },
                        onConnectClick = { navigate(AppRoute.CONNECT) },
                        onSettingsClick = { navigate(AppRoute.SETTINGS) },
                    )

                    AppRoute.PROFILE -> {
                        val state by profileViewModel.state.collectAsState()
                        ProfileScreen(
                            state = state,
                            onBack = { navigate(AppRoute.SETTINGS) },
                            onMobileClick = {
                                profileViewModel.clearFeedback()
                                route = AppRoute.CHANGE_MOBILE
                            },
                            onEmailClick = {
                                profileViewModel.clearFeedback()
                                route = AppRoute.CHANGE_EMAIL
                            },
                            onAccountsClick = { navigate(AppRoute.ACCOUNTS) },
                            onLobbyClick = { navigate(AppRoute.LOBBY) },
                            onConnectClick = { navigate(AppRoute.CONNECT) },
                            onSettingsClick = { navigate(AppRoute.SETTINGS) },
                        )
                    }

                    AppRoute.CHANGE_MOBILE -> {
                        val state by profileViewModel.state.collectAsState()
                        ChangeMobileNumberScreen(
                            state = state,
                            onBack = {
                                profileViewModel.loadProfile()
                                route = AppRoute.PROFILE
                            },
                            onSave = profileViewModel::updateMobile,
                            onAccountsClick = { navigate(AppRoute.ACCOUNTS) },
                            onLobbyClick = { navigate(AppRoute.LOBBY) },
                            onConnectClick = { navigate(AppRoute.CONNECT) },
                            onSettingsClick = { navigate(AppRoute.SETTINGS) },
                        )
                    }

                    AppRoute.CHANGE_EMAIL -> {
                        val state by profileViewModel.state.collectAsState()
                        ChangeEmailScreen(
                            state = state,
                            onBack = {
                                profileViewModel.loadProfile()
                                route = AppRoute.PROFILE
                            },
                            onSave = profileViewModel::updateEmail,
                            onAccountsClick = { navigate(AppRoute.ACCOUNTS) },
                            onLobbyClick = { navigate(AppRoute.LOBBY) },
                            onConnectClick = { navigate(AppRoute.CONNECT) },
                            onSettingsClick = { navigate(AppRoute.SETTINGS) },
                        )
                    }

                    AppRoute.FAVORITES -> {
                        FavoritesRoute(
                            repository = repository,
                            onBack = { navigate(AppRoute.ACCOUNTS) },
                            onAccountClick = { account ->
                                selectedAccount = account
                                route = AppRoute.ACCOUNT_DETAILS
                            },
                            onToggleFavorite = { account ->
                                coroutineScope.launch {
                                    detailActions.setFavorite(account, false).onSuccess {
                                        loadAccounts()
                                    }
                                }
                            },
                            onAccountsClick = { navigate(AppRoute.ACCOUNTS) },
                            onLobbyClick = { navigate(AppRoute.LOBBY) },
                            onConnectClick = { navigate(AppRoute.CONNECT) },
                            onSettingsClick = { navigate(AppRoute.SETTINGS) },
                        )
                    }

                    AppRoute.SECURITY -> SecurityScreen(
                        onBack = { navigate(AppRoute.ACCOUNTS) },
                        onAccountsClick = { navigate(AppRoute.ACCOUNTS) },
                        onLobbyClick = { navigate(AppRoute.LOBBY) },
                        onConnectClick = { navigate(AppRoute.CONNECT) },
                        onSettingsClick = { navigate(AppRoute.SETTINGS) },
                    )

                    AppRoute.PRIVACY_POLICY -> PrivacyPolicyScreen(
                        onBack = { navigate(AppRoute.ACCOUNTS) },
                        onAccountsClick = { navigate(AppRoute.ACCOUNTS) },
                        onLobbyClick = { navigate(AppRoute.LOBBY) },
                        onConnectClick = { navigate(AppRoute.CONNECT) },
                        onSettingsClick = { navigate(AppRoute.SETTINGS) },
                    )

                    AppRoute.ACCOUNT_DETAILS -> {
                        val account = selectedAccount
                        val uiAccount = account?.let {
                            accountsViewModel.state.value.accounts.firstOrNull { item -> item.id == account.id }
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
                                onCopy = { detailActions.copyCode(uiAccount.code) },
                                onToggleFavorite = {
                                    coroutineScope.launch {
                                        detailActions.setFavorite(account, !account.favorite).onSuccess { updated ->
                                            selectedAccount = updated
                                            loadAccounts()
                                        }
                                    }
                                },
                                onDelete = {
                                    coroutineScope.launch {
                                        removalService.remove(account.id).onSuccess {
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
                        val vm = editAccountViewModel
                        if (vm == null) {
                            route = AppRoute.ACCOUNT_DETAILS
                        } else {
                            val state by vm.state.collectAsState()
                            EditAccountScreen(
                                state = state,
                                onNameChanged = vm::onNameChanged,
                                onEmailChanged = vm::onEmailChanged,
                                onSecretChanged = vm::onSecretChanged,
                                onDigitsChanged = vm::onDigitsChanged,
                                onPeriodChanged = vm::onPeriodChanged,
                                onAlgorithmChanged = vm::onAlgorithmChanged,
                                onBack = { route = AppRoute.ACCOUNT_DETAILS },
                                onSave = {
                                    vm.prepareSave()
                                        .onFailure { e -> vm.onSaveFailed(e.message ?: "Invalid account details.") }
                                        .onSuccess { request ->
                                            vm.onSaveStarted()
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
                                                    vm.onSaveCompleted()
                                                    loadAccounts()
                                                    route = AppRoute.ACCOUNT_DETAILS
                                                }.onFailure { e ->
                                                    vm.onSaveFailed(e.message ?: "Could not save account.")
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
                            onMenuClick = { menuOpen = true },
                            onSearchClick = {},
                            onAccountsClick = { navigate(AppRoute.ACCOUNTS) },
                            onLobbyClick = { navigate(AppRoute.LOBBY) },
                            onConnectClick = { navigate(AppRoute.CONNECT) },
                            onSettingsClick = { navigate(AppRoute.SETTINGS) },
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
                        val vm = importAccountDetailsViewModel
                        if (vm == null) {
                            route = AppRoute.IMPORT_OTP_URI
                        } else {
                            val state by vm.state.collectAsState()
                            AccountDetailsScreen(
                                state = state,
                                onNameChanged = vm::onNameChanged,
                                onEmailChanged = vm::onEmailChanged,
                                onSecretChanged = vm::onSecretChanged,
                                onDigitsChanged = vm::onDigitsChanged,
                                onPeriodChanged = vm::onPeriodChanged,
                                onAlgorithmChanged = vm::onAlgorithmChanged,
                                onBack = { route = AppRoute.IMPORT_OTP_URI },
                                onSave = {
                                    saveQrLikeAccount(vm, state, repository, coroutineScope) {
                                        loadAccounts()
                                        route = AppRoute.ACCOUNTS
                                    }
                                },
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
                        val vm = qrAccountDetailsViewModel
                        if (vm == null) {
                            route = AppRoute.ADD_ACCOUNT
                        } else {
                            val state by vm.state.collectAsState()
                            AccountDetailsScreen(
                                state = state,
                                onNameChanged = vm::onNameChanged,
                                onEmailChanged = vm::onEmailChanged,
                                onSecretChanged = vm::onSecretChanged,
                                onDigitsChanged = vm::onDigitsChanged,
                                onPeriodChanged = vm::onPeriodChanged,
                                onAlgorithmChanged = vm::onAlgorithmChanged,
                                onBack = { route = AppRoute.SCAN_QR },
                                onSave = {
                                    saveQrLikeAccount(vm, state, repository, coroutineScope) {
                                        loadAccounts()
                                        route = AppRoute.ACCOUNTS
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
                            onBack = { route = AppRoute.ADD_ACCOUNT },
                            onSave = {
                                enterSetupKeyViewModel.prepareSave()
                                    .onFailure { e -> enterSetupKeyViewModel.onSaveFailed(e.message ?: "Invalid account details.") }
                                    .onSuccess { request ->
                                        enterSetupKeyViewModel.onSaveStarted()
                                        coroutineScope.launch {
                                            runCatching { repository.save(AccountRecordMapper.from(request)) }
                                                .onSuccess {
                                                    enterSetupKeyViewModel.onSaveCompleted()
                                                    loadAccounts()
                                                    route = AppRoute.ACCOUNTS
                                                }
                                                .onFailure { e ->
                                                    enterSetupKeyViewModel.onSaveFailed(e.message ?: "Could not save account.")
                                                }
                                        }
                                    }
                            },
                        )
                    }
                }

                if (menuOpen) {
                    MenuDrawer(
                        accountCount = accountsViewModel.state.value.accounts.size,
                        onDismiss = { menuOpen = false },
                        onAccounts = { navigate(AppRoute.ACCOUNTS) },
                        onFavorites = { navigate(AppRoute.FAVORITES) },
                        onTrash = { menuToast("Trash is coming next") },
                        onSecurity = { navigate(AppRoute.SECURITY) },
                        onTerms = { menuToast("Terms of Use is coming next") },
                        onPrivacy = { navigate(AppRoute.PRIVACY_POLICY) },
                        onAbout = { navigate(AppRoute.SETTINGS) },
                        onLock = { navigate(AppRoute.SETTINGS) },
                        onDangerZone = { menuToast("Danger Zone is coming next") },
                        onLogout = { menuToast("Log out is coming next") },
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoritesRoute(
    repository: com.indoone.accounts.AccountRepository,
    onBack: () -> Unit,
    onAccountClick: (AccountRecord) -> Unit,
    onToggleFavorite: (AccountRecord) -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var accounts by remember { mutableStateOf<List<AccountRecord>>(emptyList()) }
    LaunchedEffect(repository) {
        accounts = runCatching { repository.getAll() }.getOrDefault(emptyList())
    }
    FavoritesScreen(
        accounts = accounts,
        onBack = onBack,
        onAccountClick = onAccountClick,
        onToggleFavorite = onToggleFavorite,
        onAccountsClick = onAccountsClick,
        onLobbyClick = onLobbyClick,
        onConnectClick = onConnectClick,
        onSettingsClick = onSettingsClick,
    )
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
                    .onSuccess {
                        viewModel.onSaveCompleted()
                        onSuccess()
                    }
                    .onFailure { error -> viewModel.onSaveFailed(error.message ?: "Could not save account.") }
            }
        }
}

private fun QrManualPrefill.toQrAccountResult(): com.indoone.accounts.addaccount.scanqr.QrAccountResult =
    com.indoone.accounts.addaccount.scanqr.QrAccountResult(
        name = name,
        email = email,
        secret = secret,
        algorithm = algorithm,
        digits = digits,
        period = period,
        provider = provider,
        service = service,
    )

private fun List<AccountRecord>.toUiAccounts(nowMillis: Long = System.currentTimeMillis()): List<AccountItem> =
    map { record ->
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
            icon = record.icon.ifBlank { record.name.firstOrNull()?.uppercase() ?: "?" },
            serviceClass = record.cls,
        )
    }
