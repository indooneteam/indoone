package com.indoone

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkInfo
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
import com.indoone.connect.integrations.instagram.InstagramConnectScreen
import com.indoone.connect.integrations.whatsapp.WhatsAppConnectScreen
import com.indoone.home.HomeScreen
import com.indoone.home.vibe.VibeScreen
import com.indoone.lobby.LobbyScreen
import com.indoone.lobby.LobbyViewModel
import com.indoone.menu.MenuDrawer
import com.indoone.menu.plugins.PluginsMenuScreen
import com.indoone.menu.plugins.email.EmailPluginMenuScreen
import com.indoone.menu.plugins.email.gmail.GmailPluginMenuScreen
import com.indoone.menu.plugins.github.GitHubPluginMenuScreen
import com.indoone.menu.plugins.zoho_mail.ZohoMailPluginMenuScreen
import com.indoone.notifications.NotificationPreferences
import com.indoone.notifications.NotificationCenterScreen
import com.indoone.notifications.NotificationRepository
import com.indoone.notifications.NotificationSettingsScreen
import com.indoone.menu.favorites.FavoritesScreen
import com.indoone.menu.privacypolicy.PrivacyPolicyScreen
import com.indoone.menu.security.SecurityScreen
import com.indoone.settings.SettingsScreen
import com.indoone.settings.update.AppUpdateManager
import com.indoone.settings.update.UpdateDownloadWorker
import com.indoone.settings.profile.ProfileScreen
import com.indoone.settings.profile.ProfileViewModel
import com.indoone.settings.profile.change_email.ChangeEmailScreen
import com.indoone.settings.profile.change_mobile_number.ChangeMobileNumberScreen
import kotlinx.coroutines.delay
import java.util.Locale
import kotlinx.coroutines.launch

private enum class AppRoute {
    HOME,
    ACCOUNTS,
    LOBBY,
    CONNECT,
    CONNECT_INSTAGRAM,
    CONNECT_WHATSAPP,
    SETTINGS,
    PROFILE,
    PLUGINS,
    EMAIL_PLUGIN,
    GMAIL_PLUGIN,
    ZOHO_MAIL_PLUGIN,
    GITHUB_PLUGIN,
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
    VIBE,
    NOTIFICATIONS,
    NOTIFICATION_CENTER,
}

class MainActivity : ComponentActivity() {
    private var batteryExemptionPrompted = false

    override fun onResume() {
        super.onResume()

        // Vibe keeps its voice session alive while the app is in another app or the
        // screen is off. Android freezes an app that is still subject to battery
        // optimisation, so ask for the exemption once, from a visible screen.
        if (!batteryExemptionPrompted) {
            val power = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!power.isIgnoringBatteryOptimizations(packageName)) {
                batteryExemptionPrompted = true
                runCatching {
                    startActivity(
                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                            .setData(Uri.parse("package:$packageName")),
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureSystemBarsDirectly()
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
                var route by remember { mutableStateOf(AppRoute.HOME) }
                var menuOpen by remember { mutableStateOf(false) }
                var menuOriginRoute by remember { mutableStateOf(AppRoute.HOME) }
                var availableUpdate by remember { mutableStateOf<AppUpdateManager.UpdateInfo?>(null) }
                var isCheckingUpdate by remember { mutableStateOf(false) }
                var updateDialogHidden by remember { mutableStateOf(false) }
                var selectedAccount by remember { mutableStateOf<AccountRecord?>(null) }
                var accountDetailReturnRoute by remember { mutableStateOf(AppRoute.ACCOUNTS) }
                var qrAccountDetailsViewModel by remember { mutableStateOf<AccountDetailsViewModel?>(null) }
                var importAccountDetailsViewModel by remember { mutableStateOf<AccountDetailsViewModel?>(null) }
                var editAccountViewModel by remember { mutableStateOf<EditAccountViewModel?>(null) }
                val notificationPreferences = remember { NotificationPreferences(this@MainActivity) }
                val notificationRepository = remember { NotificationRepository.getInstance(applicationContext) }
                val unreadNotificationCount by notificationRepository.observeUnreadCount().collectAsState(initial = 0)
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission(),
                ) { granted ->
                    if (granted) notificationPreferences.markPermissionRequested()
                }

                // The download is background work, so the dialog only reflects it.
                // Closing the dialog or leaving the app does not touch the work.
                val installedVersionCode = remember {
                    AppUpdateManager.currentVersionCode(this@MainActivity)
                }
                val updateWorkInfos by UpdateDownloadWorker
                    .observe(this@MainActivity)
                    .collectAsState(initial = emptyList())
                val updateWork = updateWorkInfos.firstOrNull()
                val isDownloadingUpdate =
                    updateWork?.state == WorkInfo.State.ENQUEUED ||
                        updateWork?.state == WorkInfo.State.RUNNING
                val updateProgress = updateWork?.progress
                    ?.getInt(UpdateDownloadWorker.KEY_PROGRESS, 0) ?: 0
                val readyVersionCode = updateWork?.outputData
                    ?.getLong(UpdateDownloadWorker.KEY_VERSION_CODE, 0L) ?: 0L
                val updateReady =
                    updateWork?.state == WorkInfo.State.SUCCEEDED &&
                        readyVersionCode > installedVersionCode &&
                        AppUpdateManager.apkFile(this@MainActivity).exists()

                // Show the dialog again when the download starts or finishes, and
                // report a failure once instead of leaving the user guessing.
                LaunchedEffect(updateWork?.state) {
                    when (updateWork?.state) {
                        WorkInfo.State.ENQUEUED,
                        WorkInfo.State.RUNNING,
                        WorkInfo.State.SUCCEEDED -> updateDialogHidden = false

                        WorkInfo.State.FAILED -> {
                            val message = updateWork?.outputData
                                ?.getString(UpdateDownloadWorker.KEY_ERROR)
                                ?: "Could not download the update."
                            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                        }

                        else -> Unit
                    }
                }

                fun checkForUpdates(showUpToDateMessage: Boolean) {
                    if (isCheckingUpdate || isDownloadingUpdate) return
                    isCheckingUpdate = true
                    coroutineScope.launch {
                        runCatching {
                            AppUpdateManager.checkForUpdate(this@MainActivity)
                        }.onSuccess { update ->
                            availableUpdate = update
                            if (update != null) updateDialogHidden = false
                            if (update == null && showUpToDateMessage) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "You're up to date.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }.onFailure { error ->
                            if (showUpToDateMessage) {
                                Toast.makeText(
                                    this@MainActivity,
                                    error.message ?: "Could not check for updates.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                        isCheckingUpdate = false
                    }
                }

                LaunchedEffect(Unit) {
                    checkForUpdates(showUpToDateMessage = false)
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val granted = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS,
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            notificationPreferences.markPermissionRequested()
                        } else if (!notificationPreferences.wasPermissionRequested()) {
                            notificationPreferences.markPermissionRequested()
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                fun navigate(target: AppRoute) {
                    menuOpen = false
                    route = target
                }

                fun openMenu() {
                    menuOriginRoute = route
                    menuOpen = true
                }

                fun backToMenu() {
                    route = menuOriginRoute
                    menuOpen = true
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
                    AppRoute.HOME -> HomeScreen(
                        onMenuClick = ::openMenu,
                        onLobbyClick = { navigate(AppRoute.LOBBY) },
                        onConnectClick = { navigate(AppRoute.CONNECT) },
                        onSettingsClick = { navigate(AppRoute.SETTINGS) },
                        startNewChat = intent.getBooleanExtra("start_new_chat", false),
                        openConversationId = intent.getStringExtra("open_conversation_id"),
                        onVibeClick = { navigate(AppRoute.VIBE) },
                        hasUnreadNotifications = unreadNotificationCount > 0,
                        onNotificationClick = { navigate(AppRoute.NOTIFICATION_CENTER) },
                    )

                    AppRoute.VIBE -> VibeScreen(
                        onClose = { navigate(AppRoute.HOME) },
                    )

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
                                    if (selectedAccount != null) {
                                        accountDetailReturnRoute = AppRoute.ACCOUNTS
                                        route = AppRoute.ACCOUNT_DETAILS
                                    }
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
                            onMenuClick = ::openMenu,
                        )
                    }

                    AppRoute.LOBBY -> {
                        val state by lobbyViewModel.state.collectAsState()
                        LobbyScreen(
                            state = state,
                            onMenuClick = ::openMenu,
                            onAccountsClick = { navigate(AppRoute.HOME) },
                            onLobbyClick = { navigate(AppRoute.LOBBY) },
                            onConnectClick = { navigate(AppRoute.CONNECT) },
                            onSettingsClick = { navigate(AppRoute.SETTINGS) },
                        )
                    }

                    AppRoute.CONNECT -> ConnectScreen(
                        onMenuClick = ::openMenu,
                        onAccountsClick = { navigate(AppRoute.HOME) },
                        onLobbyClick = { navigate(AppRoute.LOBBY) },
                        onConnectClick = { navigate(AppRoute.CONNECT) },
                        onSettingsClick = { navigate(AppRoute.SETTINGS) },
                        onWhatsAppClick = { navigate(AppRoute.CONNECT_WHATSAPP) },
                        onInstagramClick = { navigate(AppRoute.CONNECT_INSTAGRAM) },
                    )

                    AppRoute.CONNECT_INSTAGRAM -> InstagramConnectScreen(
                        onBack = { navigate(AppRoute.CONNECT) },
                    )

                    AppRoute.CONNECT_WHATSAPP -> WhatsAppConnectScreen(
                        onBack = { navigate(AppRoute.CONNECT) },
                    )

                    AppRoute.SETTINGS -> SettingsScreen(
                        onMenuClick = ::openMenu,
                        onProfileClick = {
                            profileViewModel.loadProfile()
                            navigate(AppRoute.PROFILE)
                        },
                        onAccountsClick = { navigate(AppRoute.HOME) },
                        onLobbyClick = { navigate(AppRoute.LOBBY) },
                        onConnectClick = { navigate(AppRoute.CONNECT) },
                        onSettingsClick = { navigate(AppRoute.SETTINGS) },
                        onNotificationsClick = { navigate(AppRoute.NOTIFICATIONS) },
                        onCheckForUpdates = { checkForUpdates(showUpToDateMessage = true) },
                    )

                    AppRoute.NOTIFICATIONS -> NotificationSettingsScreen(
                        onBack = { navigate(AppRoute.SETTINGS) },
                    )

                    AppRoute.NOTIFICATION_CENTER -> NotificationCenterScreen(
                        repository = notificationRepository,
                        onBack = { navigate(AppRoute.HOME) },
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
                            onPluginsClick = { navigate(AppRoute.PLUGINS) },
                            onAccountsClick = { navigate(AppRoute.HOME) },
                            onLobbyClick = { navigate(AppRoute.LOBBY) },
                            onConnectClick = { navigate(AppRoute.CONNECT) },
                            onSettingsClick = { navigate(AppRoute.SETTINGS) },
                        )
                    }

                    AppRoute.PLUGINS -> PluginsMenuScreen(
                        onBack = { navigate(AppRoute.PROFILE) },
                        onEmailClick = { navigate(AppRoute.GMAIL_PLUGIN) },
                        onGitHubClick = { navigate(AppRoute.GITHUB_PLUGIN) },
                    )

                    AppRoute.EMAIL_PLUGIN -> EmailPluginMenuScreen(
                        onBack = { navigate(AppRoute.PLUGINS) },
                        onGmailClick = { navigate(AppRoute.GMAIL_PLUGIN) },
                    )

                    AppRoute.GMAIL_PLUGIN -> GmailPluginMenuScreen(
                        onBack = { navigate(AppRoute.PLUGINS) },
                    )

                    AppRoute.ZOHO_MAIL_PLUGIN -> ZohoMailPluginMenuScreen(
                        onBack = { navigate(AppRoute.PLUGINS) },
                    )

                    AppRoute.GITHUB_PLUGIN -> GitHubPluginMenuScreen(
                        onBack = { navigate(AppRoute.PLUGINS) },
                    )

                    AppRoute.CHANGE_MOBILE -> {
                        val state by profileViewModel.state.collectAsState()
                        ChangeMobileNumberScreen(
                            state = state,
                            onBack = {
                                profileViewModel.loadProfile()
                                route = AppRoute.PROFILE
                            },
                            onSave = profileViewModel::updateMobile,
                            onAccountsClick = { navigate(AppRoute.HOME) },
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
                            onAccountsClick = { navigate(AppRoute.HOME) },
                            onLobbyClick = { navigate(AppRoute.LOBBY) },
                            onConnectClick = { navigate(AppRoute.CONNECT) },
                            onSettingsClick = { navigate(AppRoute.SETTINGS) },
                        )
                    }

                    AppRoute.FAVORITES -> {
                        FavoritesRoute(
                            repository = repository,
                            onBack = ::backToMenu,
                            onAccountClick = { account ->
                                selectedAccount = account
                                accountDetailReturnRoute = AppRoute.FAVORITES
                                route = AppRoute.ACCOUNT_DETAILS
                            },
                            onToggleFavorite = { account ->
                                coroutineScope.launch {
                                    detailActions.setFavorite(account, false).onSuccess {
                                        loadAccounts()
                                    }
                                }
                            },
                            onAccountsClick = { navigate(AppRoute.HOME) },
                            onLobbyClick = { navigate(AppRoute.LOBBY) },
                            onConnectClick = { navigate(AppRoute.CONNECT) },
                            onSettingsClick = { navigate(AppRoute.SETTINGS) },
                        )
                    }

                    AppRoute.SECURITY -> SecurityScreen(
                        onBack = ::backToMenu,
                        onAccountsClick = { navigate(AppRoute.HOME) },
                        onLobbyClick = { navigate(AppRoute.LOBBY) },
                        onConnectClick = { navigate(AppRoute.CONNECT) },
                        onSettingsClick = { navigate(AppRoute.SETTINGS) },
                    )

                    AppRoute.PRIVACY_POLICY -> PrivacyPolicyScreen(
                        onBack = ::backToMenu,
                        onAccountsClick = { navigate(AppRoute.HOME) },
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
                            route = accountDetailReturnRoute
                        } else {
                            AccountDetailsScreen(
                                account = account,
                                code = uiAccount.code,
                                secondsRemaining = uiAccount.secondsRemaining,
                                onBack = { route = accountDetailReturnRoute },
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
                                            route = accountDetailReturnRoute
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
                            onMenuClick = ::openMenu,
                            onSearchClick = {},
                            onAccountsClick = { navigate(AppRoute.HOME) },
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

                val showUpdateDialog =
                    (availableUpdate != null || isDownloadingUpdate || updateReady) &&
                        !updateDialogHidden

                if (showUpdateDialog) {
                    val update = availableUpdate
                    AlertDialog(
                        onDismissRequest = { updateDialogHidden = true },
                        title = {
                            Text(
                                when {
                                    updateReady -> "Update ready to install"
                                    isDownloadingUpdate -> "Downloading update"
                                    else -> "New update available"
                                },
                            )
                        },
                        text = {
                            Text(
                                when {
                                    updateReady ->
                                        "Indoone $readyVersionCode is downloaded. " +
                                            "Tap Install to open the system installer."

                                    isDownloadingUpdate ->
                                        "Downloading the latest Indoone APK — " +
                                            "$updateProgress%. You can close this; " +
                                            "the download keeps going."

                                    update != null ->
                                        "Indoone ${update.versionCode} is available. " +
                                            "Download size " +
                                            "${formatUpdateSize(update.sizeBytes)}."

                                    else -> "A newer Indoone update is available."
                                },
                            )
                        },
                        confirmButton = {
                            when {
                                updateReady -> TextButton(
                                    onClick = {
                                        AppUpdateManager.installApk(
                                            this@MainActivity,
                                            AppUpdateManager.apkFile(this@MainActivity),
                                        )
                                    },
                                ) {
                                    Text("Install")
                                }

                                !isDownloadingUpdate && update != null -> TextButton(
                                    onClick = {
                                        // Ask for the install permission first: without
                                        // it the download would finish and be unusable.
                                        if (!AppUpdateManager.canInstall(this@MainActivity)) {
                                            AppUpdateManager.openInstallPermissionSettings(
                                                this@MainActivity,
                                            )
                                        }
                                        UpdateDownloadWorker.enqueue(this@MainActivity, update)
                                        availableUpdate = null
                                    },
                                ) {
                                    Text("Update")
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { updateDialogHidden = true }) {
                                Text(if (isDownloadingUpdate) "Hide" else "Later")
                            }
                        },
                    )
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

    private fun configureSystemBarsDirectly() {
        // Keep the system bars aligned with the app's light lavender-purple palette.
        window.statusBarColor = Color.rgb(248, 244, 255)
        window.navigationBarColor = Color.rgb(248, 244, 255)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
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

private fun formatUpdateSize(bytes: Long): String {
    if (bytes <= 0L) return "unknown"
    val megabytes = bytes.toDouble() / (1024.0 * 1024.0)
    return String.format(Locale.US, "%.1f MB", megabytes)
}
