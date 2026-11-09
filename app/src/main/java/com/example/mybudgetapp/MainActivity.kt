package com.example.mybudgetapp

import android.content.SharedPreferences
import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import com.example.mybudgetapp.ui.theme.MyBudgetAppTheme
import com.example.mybudgetapp.ui.theme.MutedGreen
import com.example.mybudgetapp.ui.theme.MutedRed
import com.example.mybudgetapp.ui.theme.Black
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private enum class TransactionType { INCOME, EXPENSE }
private enum class AppScreen { HOME, TRANSACTION, SETTINGS_PAGE }
private enum class SettingsPage { REGULAR_INCOME, REGULAR_EXPENSES, HISTORY, SPREADS, BUCKETS, WISHLIST, IMPORT_EXPORT }
private enum class Recurrence(val label: String) {
    DAILY("Daily"), WEEKLY("Weekly"), MONTHLY("Monthly"), YEARLY("Yearly")
}

private data class Transaction(
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val startDate: String,
    val endDate: String?,
    val bucket: String,
    val recurrence: Recurrence? = null,
    val currency: String = "MYR",
    val exchangeRate: Double = 1.0,
    val originalAmount: Double = amount,
    val incomeVersion: Int? = null,
    val allocations: List<BucketAllocation> = emptyList()
)

private data class BucketAllocation(val bucket: String, val amount: Double, val percentage: Double? = null)
private data class WishlistItem(val label: String, val cost: Double, val bucket: String?)
private data class WishlistFunding(val index: Int, val bucket: String, val funded: Double)
private data class DashboardSnapshot(
    val transactions: List<Transaction>,
    val buckets: List<String>,
    val wishlist: List<WishlistItem>,
    val selectedBucket: String
)
private data class BudgetBackup(
    val buckets: List<String>,
    val transactions: List<Transaction>,
    val wishlist: List<WishlistItem>,
    val lastCurrency: String
)

class MainActivity : ComponentActivity() {
    private var widgetAction by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetAction = intent.getStringExtra("widget_action")
        setContent {
            MyBudgetAppTheme {
                BudgetApp(widgetAction) { widgetAction = null }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        widgetAction = intent.getStringExtra("widget_action")
    }
}

@Composable
private fun BudgetApp(widgetAction: String?, onWidgetActionHandled: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val preferences = remember {
        context.getSharedPreferences("budget_data", android.content.Context.MODE_PRIVATE)
    }
    var screen by remember {
        mutableStateOf(if (widgetAction == null) AppScreen.HOME else AppScreen.TRANSACTION)
    }
    var settingsPage by remember { mutableStateOf(SettingsPage.REGULAR_INCOME) }
    var transactionType by remember {
        mutableStateOf(if (widgetAction == "expense") TransactionType.EXPENSE else TransactionType.INCOME)
    }
    var regularEntry by remember { mutableStateOf(false) }
    var unifiedEntry by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var formKey by remember { mutableStateOf(0) }
    var backgroundScreen by remember { mutableStateOf(AppScreen.HOME) }
    var spreadEditEntry by remember { mutableStateOf(false) }
    var openedFromWidget by remember { mutableStateOf(widgetAction != null) }
    var selectedBucket by remember { mutableStateOf("Needs") }
    var includeHistoricalIncome by remember {
        mutableStateOf(preferences.getBoolean("include_historical_income", false))
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val buckets = remember {
        mutableStateListOf<String>().apply { addAll(loadBuckets(preferences)) }
    }
    val transactions = remember {
        mutableStateListOf<Transaction>().apply { addAll(loadTransactions(preferences)) }
    }
    val wishlist = remember {
        mutableStateListOf<WishlistItem>().apply { addAll(loadWishlist(preferences)) }
    }
    var dashboard by remember {
        mutableStateOf(DashboardSnapshot(transactions.toList(), buckets.toList(), wishlist.toList(), selectedBucket))
    }
    val settingsVisibility = remember { MutableTransitionState(false) }
    var pendingImport by remember { mutableStateOf<BudgetBackup?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                it.write(createBackupJson(preferences, buckets, transactions, wishlist))
            } ?: error("Could not write the backup file")
        }.onSuccess {
            Toast.makeText(context, "Backup exported", Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, "Export failed: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { parseBackupJson(it.readText()) }
                ?: error("Could not read the selected file")
        }.onSuccess {
            pendingImport = it
        }.onFailure {
            Toast.makeText(context, "This backup could not be read: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(widgetAction) {
        widgetAction?.let {
            transactionType = if (it == "expense") TransactionType.EXPENSE else TransactionType.INCOME
            regularEntry = false
            unifiedEntry = false
            editingIndex = null
            formKey++
            openedFromWidget = true
            screen = AppScreen.TRANSACTION
            onWidgetActionHandled()
        }
    }

    LaunchedEffect(screen) {
        if (screen != AppScreen.TRANSACTION) backgroundScreen = screen
    }


    BackHandler(enabled = screen == AppScreen.HOME) {
        if (drawerState.isOpen) scope.launch { drawerState.close() }
        else activity?.finish()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Text("Settings", modifier = Modifier.padding(24.dp), style = MaterialTheme.typography.titleLarge)
                NavigationDrawerItem(
                    label = { Text("Regular income") },
                    selected = screen == AppScreen.SETTINGS_PAGE && settingsPage == SettingsPage.REGULAR_INCOME,
                    onClick = {
                        settingsPage = SettingsPage.REGULAR_INCOME
                        screen = AppScreen.SETTINGS_PAGE
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Regular expenses") },
                    selected = screen == AppScreen.SETTINGS_PAGE && settingsPage == SettingsPage.REGULAR_EXPENSES,
                    onClick = {
                        settingsPage = SettingsPage.REGULAR_EXPENSES
                        screen = AppScreen.SETTINGS_PAGE
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("History") },
                    selected = screen == AppScreen.SETTINGS_PAGE && settingsPage == SettingsPage.HISTORY,
                    onClick = {
                        settingsPage = SettingsPage.HISTORY
                        screen = AppScreen.SETTINGS_PAGE
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Active spreads") },
                    selected = screen == AppScreen.SETTINGS_PAGE && settingsPage == SettingsPage.SPREADS,
                    onClick = {
                        settingsPage = SettingsPage.SPREADS
                        screen = AppScreen.SETTINGS_PAGE
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Buckets") },
                    selected = screen == AppScreen.SETTINGS_PAGE && settingsPage == SettingsPage.BUCKETS,
                    onClick = {
                        settingsPage = SettingsPage.BUCKETS
                        screen = AppScreen.SETTINGS_PAGE
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Wishlist") },
                    selected = screen == AppScreen.SETTINGS_PAGE && settingsPage == SettingsPage.WISHLIST,
                    onClick = {
                        settingsPage = SettingsPage.WISHLIST
                        screen = AppScreen.SETTINGS_PAGE
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Import / Export") },
                    selected = screen == AppScreen.SETTINGS_PAGE && settingsPage == SettingsPage.IMPORT_EXPORT,
                    onClick = {
                        settingsPage = SettingsPage.IMPORT_EXPORT
                        screen = AppScreen.SETTINGS_PAGE
                        scope.launch { drawerState.close() }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        includeHistoricalIncome = !includeHistoricalIncome
                        preferences.edit().putBoolean("include_historical_income", includeHistoricalIncome).apply()
                    }.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Include income in history")
                        Text(
                            "Net one-off income against expenses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    Switch(
                        checked = includeHistoricalIncome,
                        onCheckedChange = {
                            includeHistoricalIncome = it
                            preferences.edit().putBoolean("include_historical_income", it).apply()
                        }
                    )
                }
            }
        }
    ) {
        val showSettings = screen == AppScreen.SETTINGS_PAGE ||
            (screen == AppScreen.TRANSACTION && backgroundScreen == AppScreen.SETTINGS_PAGE)
        LaunchedEffect(showSettings) { settingsVisibility.targetState = showSettings }
        LaunchedEffect(settingsVisibility.currentState, settingsVisibility.targetState, screen) {
            if (screen == AppScreen.HOME && !settingsVisibility.currentState && !settingsVisibility.targetState) {
                dashboard = DashboardSnapshot(transactions.toList(), buckets.toList(), wishlist.toList(), selectedBucket)
            }
        }
        Box(Modifier.fillMaxSize()) {
            HomeScreen(
                transactions = dashboard.transactions,
                buckets = dashboard.buckets,
                wishlist = dashboard.wishlist,
                selectedBucket = dashboard.selectedBucket,
                includeHistoricalIncome = includeHistoricalIncome,
                onBucketSelected = {
                    selectedBucket = it
                    dashboard = dashboard.copy(selectedBucket = it)
                },
                onIncome = { spreadEditEntry = false; transactionType = TransactionType.INCOME; regularEntry = false; unifiedEntry = false; editingIndex = null; formKey++; screen = AppScreen.TRANSACTION },
                onExpense = { spreadEditEntry = false; transactionType = TransactionType.EXPENSE; regularEntry = false; unifiedEntry = false; editingIndex = null; formKey++; screen = AppScreen.TRANSACTION },
                onWishlist = { settingsPage = SettingsPage.WISHLIST; screen = AppScreen.SETTINGS_PAGE },
                onSpreads = { index ->
                    transactions.getOrNull(index)?.let {
                        transactionType = it.type
                        regularEntry = false
                        unifiedEntry = false
                        editingIndex = index
                        formKey++
                        settingsPage = SettingsPage.SPREADS
                        backgroundScreen = AppScreen.SETTINGS_PAGE
                        spreadEditEntry = true
                        settingsVisibility.targetState = true
                        screen = AppScreen.TRANSACTION
                    }
                },
                onSettings = { scope.launch { drawerState.open() } }
            )
            AnimatedVisibility(
                visibleState = settingsVisibility,
                modifier = Modifier.fillMaxSize().zIndex(1f),
                enter = slideInHorizontally(tween(240, easing = FastOutSlowInEasing)) { -it },
                exit = slideOutHorizontally(tween(240, easing = FastOutSlowInEasing)) { -it }
            ) {
                Box(
                    Modifier.fillMaxSize().background(Black).clickable(
                        interactionSource = remember { MutableInteractionSource() }, indication = null
                    ) {}
                ) {
                    AnimatedContent(
                        targetState = settingsPage,
                        transitionSpec = {
                            slideInHorizontally(tween(220, easing = FastOutSlowInEasing)) { -it } togetherWith
                                slideOutHorizontally(tween(220, easing = FastOutSlowInEasing)) { -it }
                        },
                        label = "settings page"
                    ) { currentPage ->
                    SettingsPageScreen(
                page = currentPage,
                transactions = transactions,
                buckets = buckets,
                wishlist = wishlist,
                visible = screen == AppScreen.SETTINGS_PAGE && currentPage == settingsPage,
                onBack = { screen = AppScreen.HOME },
                onAddRegular = {
                    spreadEditEntry = false
                    transactionType = it
                    regularEntry = true
                    unifiedEntry = false
                    editingIndex = null
                    formKey++
                    screen = AppScreen.TRANSACTION
                },
                onAddUnified = {
                    spreadEditEntry = false
                    transactionType = TransactionType.INCOME
                    regularEntry = true
                    unifiedEntry = true
                    editingIndex = null
                    formKey++
                    screen = AppScreen.TRANSACTION
                },
                onEditRegular = {
                    spreadEditEntry = false
                    transactionType = transactions[it].type
                    regularEntry = true
                    unifiedEntry = transactions[it].incomeVersion != null
                    editingIndex = it
                    formKey++
                    screen = AppScreen.TRANSACTION
                },
                onDeleteRegular = { index ->
                    val item = transactions[index]
                    val today = todayString()
                    if (item.startDate < today && (item.endDate == null || item.endDate > today)) {
                        transactions[index] = item.copy(endDate = today)
                    } else {
                        transactions.removeAt(index)
                    }
                    saveTransactions(preferences, transactions)
                },
                onEditHistory = {
                    spreadEditEntry = false
                    transactionType = transactions[it].type
                    regularEntry = false
                    unifiedEntry = false
                    editingIndex = it
                    formKey++
                    screen = AppScreen.TRANSACTION
                },
                onDeleteHistory = {
                    transactions.removeAt(it).also { deleted ->
                        saveTransactions(preferences, transactions)
                    }
                },
                onRestoreHistory = { index, transaction ->
                    transactions.add(index.coerceAtMost(transactions.size), transaction)
                    saveTransactions(preferences, transactions)
                },
                onAddBucket = { name -> buckets.add(name); saveBuckets(preferences, buckets) },
                onDeleteBucket = { name ->
                    val today = todayString()
                    val allocated = transactions.any { transaction ->
                        val end = transaction.endDate
                        transaction.incomeVersion != null && transaction.allocations.any {
                            it.bucket == name && (it.amount > 0.0 || (it.percentage ?: 0.0) > 0.0)
                        } &&
                            (end == null || end > today)
                    }
                    if (allocated) Toast.makeText(
                        context, "Remove this bucket from active unified income first.", Toast.LENGTH_LONG
                    ).show()
                    else { buckets.remove(name); saveBuckets(preferences, buckets) }
                },
                onAddWishlist = { item -> wishlist.add(item); saveWishlist(preferences, wishlist) },
                onEditWishlist = { index, item -> wishlist[index] = item; saveWishlist(preferences, wishlist) },
                onDeleteWishlist = { index -> wishlist.removeAt(index); saveWishlist(preferences, wishlist) },
                onReorderWishlist = { from, to ->
                    wishlist.add(to, wishlist.removeAt(from))
                    saveWishlist(preferences, wishlist)
                },
                onExport = {
                    exportLauncher.launch("luxe-budget-backup-${todayString()}.json")
                },
                onImport = { importLauncher.launch(arrayOf("application/json", "text/*")) }
            )
                    }
                }
            }

            AnimatedVisibility(
                visible = screen == AppScreen.TRANSACTION,
                modifier = Modifier.fillMaxSize().zIndex(2f),
                enter = slideInHorizontally(tween(240, easing = FastOutSlowInEasing)) {
                    if (spreadEditEntry) -it else it
                },
                exit = slideOutHorizontally(tween(240, easing = FastOutSlowInEasing)) {
                    if (spreadEditEntry) -it else it
                }
            ) {
                TransactionScreen(
                type = transactionType,
                isRegular = regularEntry,
                isUnifiedIncome = unifiedEntry,
                initial = editingIndex?.let { transactions.getOrNull(it) },
                visible = screen == AppScreen.TRANSACTION,
                formKey = formKey,
                defaultBucket = selectedBucket,
                defaultCurrency = preferences.getString("last_currency", "MYR") ?: "MYR",
                preferences = preferences,
                transactions = transactions,
                buckets = buckets,
                onBack = {
                    if (openedFromWidget) activity?.finish()
                    else screen = if (regularEntry || editingIndex != null) AppScreen.SETTINGS_PAGE else AppScreen.HOME
                },
                onSave = {
                    if (it.bucket.isNotBlank()) selectedBucket = it.bucket
                    if (editingIndex == null) {
                        if (it.incomeVersion != null && transactions.none { existing -> existing.incomeVersion != null }) {
                            val today = todayString()
                            transactions.indices.forEach { index ->
                                val old = transactions[index]
                                if (old.type == TransactionType.INCOME && old.recurrence != null &&
                                    old.incomeVersion == null && (old.endDate == null || old.endDate > it.startDate)
                                ) transactions[index] = old.copy(endDate = maxOf(it.startDate, old.startDate))
                            }
                        }
                        transactions.add(it)
                    } else if (regularEntry) {
                        val old = transactions[editingIndex!!]
                        val today = todayString()
                        transactions[editingIndex!!] = old.copy(endDate = maxOf(today, old.startDate))
                        transactions.add(it.copy(
                            startDate = today,
                            endDate = old.endDate?.takeIf { end -> end > today }
                        ))
                    } else {
                        transactions[editingIndex!!] = it
                    }
                    saveTransactions(preferences, transactions)
                    if (openedFromWidget) activity?.finish()
                    else screen = if (regularEntry || editingIndex != null) AppScreen.SETTINGS_PAGE else AppScreen.HOME
                }
                )
            }
        }
    }

    pendingImport?.let { backup ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Replace current data?") },
            text = { Text("This backup contains ${backup.transactions.size} transactions, ${backup.buckets.size} buckets, and ${backup.wishlist.size} wishlist items. Importing replaces the data currently on this device.") },
            confirmButton = {
                TextButton(onClick = {
                    preferences.edit()
                        .putString("buckets", bucketsJson(backup.buckets).toString())
                        .putString("transactions", transactionsJson(backup.transactions).toString())
                        .putString("wishlist", wishlistJson(backup.wishlist).toString())
                        .putString("last_currency", backup.lastCurrency)
                        .apply()
                    buckets.clear(); buckets.addAll(backup.buckets)
                    transactions.clear(); transactions.addAll(backup.transactions)
                    wishlist.clear(); wishlist.addAll(backup.wishlist)
                    selectedBucket = backup.buckets.first()
                    pendingImport = null
                    Toast.makeText(context, "Backup imported", Toast.LENGTH_SHORT).show()
                }) { Text("Replace data") }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    transactions: List<Transaction>,
    buckets: List<String>,
    wishlist: List<WishlistItem>,
    selectedBucket: String,
    includeHistoricalIncome: Boolean,
    onBucketSelected: (String) -> Unit,
    onIncome: () -> Unit,
    onExpense: () -> Unit,
    onWishlist: () -> Unit,
    onSpreads: (Int) -> Unit,
    onSettings: () -> Unit
) {
    val auditDate = todayString()
    LaunchedEffect(transactions.toList(), buckets.toList(), selectedBucket, auditDate) {
        val today = auditDate
        val yesterday = shiftDate(today, -1)
        val tomorrow = shiftDate(today, 1)
        transactions.forEachIndexed { index, transaction ->
            if (transaction.type == TransactionType.INCOME) Log.d(
                "LUXE_INCOME_AUDIT",
                "SAVED #$index amount=${transaction.amount} bucket=${transaction.bucket} " +
                    "version=${transaction.incomeVersion} recurrence=${transaction.recurrence} " +
                    "start=${transaction.startDate} end=${transaction.endDate} " +
                    "allocations=${transaction.allocations.joinToString { "${it.bucket}:${it.amount}:${it.percentage}" }}"
            )
        }
        buckets.forEach { bucket ->
            val incomes = transactionsForBucket(transactions, bucket)
                .filter { it.type == TransactionType.INCOME }
            val dueToday = incomes.sumOf {
                availableThroughDate(listOf(it), today) - availableThroughDate(listOf(it), yesterday)
            }
            Log.d("LUXE_INCOME_AUDIT", "BUCKET $bucket incomeRecords=${incomes.size} dueToday=$dueToday")
            incomes.forEach { income ->
                val before = availableThroughDate(listOf(income), yesterday)
                val current = availableThroughDate(listOf(income), today)
                val next = availableThroughDate(listOf(income), tomorrow)
                Log.d(
                    "LUXE_INCOME_AUDIT",
                    "RESOLVED bucket=$bucket amount=${income.amount} recurrence=${income.recurrence} " +
                        "start=${income.startDate} end=${income.endDate} " +
                        "yesterday=$before today=$current dueToday=${current - before} tomorrow=$next"
                )
            }
        }
    }
    Scaffold(containerColor = Black, topBar = {
        CenterAlignedTopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Menu, contentDescription = "Open settings")
                }
            }
        )
    }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "BUDGET",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                )
                val bucketTransactions = transactionsForBucket(transactions, selectedBucket)
                val balance = availableThroughDate(bucketTransactions, todayString())
                val setAside = wishlistFunding(wishlist, transactions, buckets)
                    .filter { it.bucket == selectedBucket }
                    .sumOf { it.funded }
                val animatedBalance = animateFloatAsState(
                    balance.toFloat(), animationSpec = tween(220, easing = FastOutSlowInEasing),
                    label = "balance"
                ).value
                val dailyAmount = dailyAmountToday(bucketTransactions)
                val animatedDailyAmount = animateFloatAsState(
                    dailyAmount.toFloat(),
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    label = "daily amount"
                ).value
                var showBreakdown by remember(selectedBucket) { mutableStateOf(false) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val dailyColor = signalColor(animatedDailyAmount.toDouble())
                    Text(
                        formatMoney(animatedBalance.toDouble()),
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        buildAnnotatedString {
                            append("Today: ")
                            withStyle(SpanStyle(color = dailyColor)) {
                                append(formatSignedMoney(animatedDailyAmount.toDouble()))
                            }
                        },
                        modifier = Modifier.clickable { showBreakdown = !showBreakdown },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    AnimatedVisibility(
                        visible = showBreakdown,
                        enter = slideInVertically(tween(160)) { it / 3 } + fadeIn(tween(160)),
                        exit = slideOutVertically(tween(160)) { it / 3 } + fadeOut(tween(160))
                    ) {
                        TodayBreakdownView(
                            todayBreakdown(bucketTransactions),
                            historicalDailyCost(bucketTransactions, includeHistoricalIncome),
                            recentDailySpending(bucketTransactions),
                            includeHistoricalIncome
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    WeeklyBalanceChart(bucketTransactions, selectedBucket, setAside, includeHistoricalIncome)
                    Spacer(Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = onIncome, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Add income")
                        }
                        OutlinedButton(onClick = onExpense, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                            Text("Add expense")
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                Text(
                    "BUCKETS",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                )
                    buckets.forEach { bucket ->
                    val selected = bucket == selectedBucket
                    val rowColor = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                    val bucketTransactions = transactionsForBucket(transactions, bucket)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBucketSelected(bucket) }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            bucket,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start,
                            color = rowColor
                        )
                        Spacer(Modifier.width(12.dp))
                        VerticalDivider(
                            modifier = Modifier.height(24.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            formatMoney(availableThroughDate(bucketTransactions, todayString())),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            color = rowColor
                        )
                        Text(
                            formatMoney(availableThroughDate(bucketTransactions, shiftDate(todayString(), 1))),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            color = rowColor.copy(alpha = 0.55f),
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )
                    }
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("WISHLIST", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    )
                    WishlistSummary(wishlist, transactions, buckets, includeHistoricalIncome, onWishlist)
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SPREAD EXPENSES", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    )
                    SpreadSummary(transactions, onSpreads)
                }
            }
        }
    }
}

@Composable
private fun WishlistSummary(
    wishlist: List<WishlistItem>,
    transactions: List<Transaction>,
    buckets: List<String>,
    includeHistoricalIncome: Boolean,
    onClick: () -> Unit
) {
    val items = wishlist
    if (items.isEmpty()) {
        Text("No items yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        return
    }
    val funding = wishlistFunding(items, transactions, buckets)
    val focusIndex = funding.indexOfFirst { it.funded + 0.005 < items[it.index].cost }
    if (focusIndex < 0) {
        Text("Wishlist complete", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        return
    }
    val item = items[focusIndex]
    val itemFunding = funding[focusIndex]
    val otherItems = items.size - focusIndex - 1
    val required = funding.take(focusIndex + 1)
        .filter { it.bucket == itemFunding.bucket }
        .sumOf { items[it.index].cost }
    val forecast = wishlistForecast(item, transactions, buckets, required, itemFunding.bucket, includeHistoricalIncome)
    val targetDate = forecast.second?.let { shortDate(shiftDate(todayString(), it)) }
    val daysText = forecast.second?.let { if (it == 0) "Today" else "$it more days" }
        ?: "More than a year"
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(
            buildAnnotatedString {
                append("#${focusIndex + 1} ${item.label}")
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                ) { append(" (${forecast.first})") }
            },
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            formatMoney(item.cost),
            modifier = Modifier.weight(1f), textAlign = TextAlign.End,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            targetDate?.let {
                Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End)
            }
            if (otherItems == 0) Text(
                daysText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                fontSize = MaterialTheme.typography.bodySmall.fontSize
            )
        }
    }
    if (otherItems > 0) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "and $otherItems other item${if (otherItems == 1) "" else "s"}",
                modifier = Modifier.weight(2f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                daysText,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                fontSize = MaterialTheme.typography.bodySmall.fontSize
            )
        }
    }
}

@Composable
private fun SpreadSummary(transactions: List<Transaction>, onClick: (Int) -> Unit) {
    val today = todayString()
    val expenses = transactions.mapIndexedNotNull { index, transaction ->
        transaction.takeIf {
            it.type == TransactionType.EXPENSE && it.recurrence == null &&
                it.endDate != null && it.startDate <= today && today < it.endDate
        }?.let { index to it }
    }.sortedBy { spreadRemainingAmount(it.second, today) }
    if (expenses.isEmpty()) {
        Text("No active expense spreads.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        return
    }
    val (focusIndex, focus) = expenses.first()
    val remaining = spreadRemainingAmount(focus, today)
    Column(modifier = Modifier.fillMaxWidth().clickable { onClick(focusIndex) }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                focus.description,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                buildAnnotatedString {
                    append(formatMoney(remaining))
                    append(" / ")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))) {
                        append(formatMoney(focus.amount))
                    }
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (expenses.size > 1) {
                Text(
                    "and ${expenses.size - 1} other spread${if (expenses.size == 2) "" else "s"}",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.bodySmall
                )
            } else Spacer(Modifier.weight(1f))
            Text(
                "${formatMoney(focus.amount / daysBetween(focus.startDate, focus.endDate!!))} daily",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}

private fun wishlistFunding(
    wishlist: List<WishlistItem>,
    transactions: List<Transaction>,
    buckets: List<String>
): List<WishlistFunding> {
    val remaining = buckets.associateWith { bucket ->
        availableThroughDate(transactionsForBucket(transactions, bucket), todayString()).coerceAtLeast(0.0)
    }.toMutableMap()
    var blocked = false
    return wishlist.mapIndexed { index, item ->
        val bucket = item.bucket ?: remaining.maxByOrNull { it.value }?.key ?: "No bucket"
        val available = remaining[bucket] ?: 0.0
        val funded = if (!blocked && available + 0.005 >= item.cost) item.cost else 0.0
        remaining[bucket] = (remaining[bucket] ?: 0.0) - funded
        if (funded == 0.0) blocked = true
        Log.d(
            "LUXE_WISHLIST",
            "#${index + 1} '${item.label}' cost=${item.cost} bucket=$bucket " +
                "available=$available funded=$funded blocked=$blocked"
        )
        WishlistFunding(index, bucket, funded)
    }
}

private fun wishlistForecast(
    item: WishlistItem,
    transactions: List<Transaction>,
    buckets: List<String>,
    requiredCost: Double = item.cost,
    forecastBucket: String? = null,
    includeHistoricalIncome: Boolean = false
): Pair<String, Int?> {
    val balances = buckets.associateWith { bucket ->
        availableThroughDate(transactionsForBucket(transactions, bucket), todayString())
    }
    val bucket = forecastBucket ?: item.bucket ?: balances.maxByOrNull { it.value }?.key ?: "No bucket"
    val bucketTransactions = transactionsForBucket(transactions, bucket)
    val averageCost = historicalDailyCost(
        item.bucket?.let { bucketTransactions } ?: transactions,
        includeHistoricalIncome
    ) ?: 0.0
    val days = (0..365).firstOrNull { day ->
        availableThroughDate(bucketTransactions, shiftDate(todayString(), day)) - averageCost * day >= requiredCost
    }
    return bucket to days
}

@Composable
private fun TodayBreakdownView(
    breakdown: TodayBreakdown,
    averageDailyCost: Double?,
    recentSpending: List<DailySpending>,
    includeHistoricalIncome: Boolean
) {
    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        BreakdownRow("Regular income", breakdown.regularIncome, true)
        BreakdownRow("Spread income", breakdown.spreadIncome, true)
        BreakdownRow("Flat income", breakdown.flatIncome, true)
        BreakdownRow("Regular expenses", breakdown.regularExpenses, false)
        BreakdownRow("Spread expenses", breakdown.spreadExpenses, false)
        BreakdownRow("Flat expenses", breakdown.flatExpenses, false)
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(0.82f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (includeHistoricalIncome) "Recent average net cost" else "Recent average spending",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                averageDailyCost?.let {
                    "${if (includeHistoricalIncome) formatSignedMoney(it) else formatMoney(it)} / day"
                } ?: "Need 7 days of data",
                style = MaterialTheme.typography.labelSmall,
                color = if (includeHistoricalIncome && averageDailyCost != null) signalColor(-averageDailyCost)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        RecentSpendingChart(recentSpending)
    }
}

@Composable
private fun RecentSpendingChart(days: List<DailySpending>) {
    if (days.isEmpty()) return
    val maxAmount = days.maxOfOrNull { it.amount }?.coerceAtLeast(1.0) ?: return
    val barColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f)
    val baselineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val valueLabelPaint = remember { android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG) }
    Column(
        modifier = Modifier
            .fillMaxWidth(0.82f)
            .padding(top = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(106.dp)) {
            Canvas(modifier = Modifier.fillMaxWidth().height(86.dp).align(Alignment.TopStart)) {
                val slotWidth = size.width / days.size
                val baseline = size.height - 2.dp.toPx()
                val gap = (slotWidth * 0.22f).coerceAtMost(5.dp.toPx())
                drawLine(
                    color = baselineColor,
                    start = Offset(0f, baseline),
                    end = Offset(size.width, baseline),
                    strokeWidth = 1.dp.toPx()
                )
                days.forEachIndexed { index, day ->
                    val barHeight = (day.amount / maxAmount * (baseline - 4.dp.toPx())).toFloat()
                    val left = index * slotWidth + gap / 2f
                    drawRect(
                        color = barColor,
                        topLeft = Offset(left, baseline - barHeight),
                        size = Size((slotWidth - gap).coerceAtLeast(1f), barHeight)
                    )
                }
                val tallestIndex = days.indices.maxBy { days[it].amount }
                val tallest = days[tallestIndex]
                val tallestHeight = (tallest.amount / maxAmount * (baseline - 4.dp.toPx())).toFloat()
                valueLabelPaint.color = labelColor
                valueLabelPaint.alpha = 220
                valueLabelPaint.textSize = 22f
                valueLabelPaint.textAlign = android.graphics.Paint.Align.CENTER
                val tallestCenter = tallestIndex * slotWidth + slotWidth / 2f
                drawContext.canvas.nativeCanvas.drawText(
                    formatNumber(tallest.amount),
                    tallestCenter,
                    (baseline - tallestHeight - 5.dp.toPx()).coerceAtLeast(16f),
                    valueLabelPaint
                )
            }
            val dateColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            Text(
                shortDate(days.first().date),
                modifier = Modifier.align(Alignment.BottomStart),
                style = MaterialTheme.typography.labelSmall,
                color = dateColor
            )
            Text(
                shortDate(days[days.size / 2].date),
                modifier = Modifier.align(Alignment.BottomCenter),
                style = MaterialTheme.typography.labelSmall,
                color = dateColor
            )
            Text(
                shortDate(days.last().date),
                modifier = Modifier.align(Alignment.BottomEnd),
                style = MaterialTheme.typography.labelSmall,
                color = dateColor
            )
        }
    }
}

@Composable
private fun BreakdownRow(label: String, amount: Double, income: Boolean) {
    val displayedAmount = if (income) amount else -amount
    Row(
        modifier = Modifier.fillMaxWidth(0.82f),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(
            formatSignedMoney(displayedAmount),
            style = MaterialTheme.typography.labelSmall,
            color = signalColor(displayedAmount)
        )
    }
}

@Composable
private fun signalColor(value: Double) = when {
    value > 0 -> MutedGreen
    value < 0 -> MutedRed
    else -> MaterialTheme.colorScheme.onSurface
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsPageScreen(
    page: SettingsPage,
    visible: Boolean,
    transactions: List<Transaction>,
    buckets: List<String>,
    wishlist: List<WishlistItem>,
    onBack: () -> Unit,
    onAddRegular: (TransactionType) -> Unit,
    onAddUnified: () -> Unit,
    onEditRegular: (Int) -> Unit,
                onDeleteRegular: (Int) -> Unit,
    onEditHistory: (Int) -> Unit,
    onDeleteHistory: (Int) -> Transaction,
    onRestoreHistory: (Int, Transaction) -> Unit,
    onAddBucket: (String) -> Unit,
    onDeleteBucket: (String) -> Unit,
    onAddWishlist: (WishlistItem) -> Unit,
    onEditWishlist: (Int, WishlistItem) -> Unit,
    onDeleteWishlist: (Int) -> Unit,
    onReorderWishlist: (Int, Int) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    BackHandler(enabled = visible, onBack = onBack)
    Scaffold(containerColor = Black, topBar = {
        TopAppBar(title = { Text(page.title) }, navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        })
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f).then(
                    if (page == SettingsPage.REGULAR_INCOME || page == SettingsPage.HISTORY || page == SettingsPage.SPREADS)
                        Modifier else Modifier.verticalScroll(rememberScrollState())
                )
            ) {
            when (page) {
                SettingsPage.REGULAR_INCOME -> {
                    val today = todayString()
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                        ) {
                            transactions.mapIndexedNotNull { index, item ->
                                item.takeIf { it.recurrence != null && it.type == TransactionType.INCOME }
                                    ?.let { index to it }
                            }.sortedWith(
                                compareByDescending<Pair<Int, Transaction>> {
                                    it.second.startDate <= today && (it.second.endDate?.let { end -> today < end } ?: true)
                            }.thenByDescending { it.second.startDate }
                            ).forEach { (index, item) ->
                                RegularRow(item, { onEditRegular(index) }, { onDeleteRegular(index) })
                            }
                        }
                        Text(
                            "Unified income replaces active bucketed income from its start date; earlier totals remain based on the old entries.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                        Button(onClick = onAddUnified, modifier = Modifier.fillMaxWidth()) {
                            Text("Add unified regular income")
                        }
                        OutlinedButton(onClick = { onAddRegular(TransactionType.INCOME) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Add bucketed regular income")
                        }
                    }
                }
                SettingsPage.REGULAR_EXPENSES -> {
                    val today = todayString()
                    Column(modifier = Modifier.fillMaxWidth()) {
                        transactions.mapIndexedNotNull { index, item ->
                            item.takeIf { it.recurrence != null && it.type == TransactionType.EXPENSE }
                                ?.let { index to it }
                        }.sortedWith(
                            compareByDescending<Pair<Int, Transaction>> {
                                it.second.startDate <= today && (it.second.endDate?.let { end -> today < end } ?: true)
                            }.thenByDescending { it.second.startDate }
                        ).forEach { (index, item) ->
                            RegularRow(item, { onEditRegular(index) }, { onDeleteRegular(index) })
                        }
                        Button(
                            onClick = { onAddRegular(TransactionType.EXPENSE) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Add regular expense") }
                    }
                }
                SettingsPage.HISTORY -> HistorySettings(
                    transactions, onEditHistory, onDeleteHistory, onRestoreHistory, snackbarHostState
                )
                SettingsPage.SPREADS -> SpreadSettings(transactions, buckets, onEditHistory)
                SettingsPage.BUCKETS -> BucketSettings(buckets, onAddBucket, onDeleteBucket)
                SettingsPage.WISHLIST -> WishlistSettings(
                    wishlist, buckets, onAddWishlist, onEditWishlist, onDeleteWishlist, onReorderWishlist
                )
                SettingsPage.IMPORT_EXPORT -> ImportExportSettings(
                    onExport = onExport,
                    onImport = onImport
                )
            }
            }
        }
    }
}

@Composable
private fun RegularRow(transaction: Transaction, onEdit: () -> Unit, onDelete: () -> Unit) {
    var confirmDelete by remember(transaction) { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit).padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.description, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${transaction.recurrence?.label} · ${if (transaction.incomeVersion == 1) transaction.allocations.joinToString { "${formatPercent(it.percentage ?: 0.0)}% ${it.bucket}" } else if (transaction.incomeVersion == 2) transaction.allocations.joinToString { "${formatMoney(it.amount)} ${it.bucket}" } else transaction.bucket}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(transactionAmountLabel(transaction), style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { confirmDelete = true }) { Text("Delete") }
            }
        }
        Text(
            "Starts ${transaction.startDate}" + (transaction.endDate?.let { " · Ends $it" } ?: ""),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete regular entry?") },
            text = { Text("If this entry is active, it will stop from today onward and its earlier history will be kept.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

private val SettingsPage.title: String
    get() = when (this) {
        SettingsPage.REGULAR_INCOME -> "Regular income"
        SettingsPage.REGULAR_EXPENSES -> "Regular expenses"
        SettingsPage.HISTORY -> "History"
        SettingsPage.SPREADS -> "Active spreads"
        SettingsPage.BUCKETS -> "Buckets"
        SettingsPage.WISHLIST -> "Wishlist"
        SettingsPage.IMPORT_EXPORT -> "Import / Export"
    }

@Composable
private fun ImportExportSettings(onExport: () -> Unit, onImport: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Keep a JSON backup on your device so you can restore your budget data later.",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF151515))
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Export a backup", style = MaterialTheme.typography.titleMedium)
                Text("Save a copy of your buckets, transactions, wishlist, and currency setting.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                OutlinedButton(onClick = { onExport() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Choose where to save")
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF151515))
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Import a backup", style = MaterialTheme.typography.titleMedium)
                Text("Import replaces the data currently in the app. You’ll be asked to confirm first.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                OutlinedButton(onClick = { onImport() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Choose a backup file")
                }
            }
        }
    }
}

@Composable
private fun SpreadSettings(transactions: List<Transaction>, buckets: List<String>, onEdit: (Int) -> Unit) {
    val today = todayString()
    val active = transactions.mapIndexedNotNull { index, transaction ->
        transaction.takeIf {
            it.recurrence == null && it.endDate != null && today < it.endDate
        }?.let { index to it }
    }
    val byBucket = active.groupBy { it.second.bucket }
    val bucketOrder = buckets + (byBucket.keys - buckets.toSet()).sorted()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (active.isEmpty()) {
            item { Text("No active spreads.") }
        } else {
            bucketOrder.forEach { bucket ->
                val entries = byBucket[bucket].orEmpty().sortedWith(
                    compareBy<Pair<Int, Transaction>> { it.second.endDate }
                        .thenBy { it.second.amount / daysBetween(it.second.startDate, it.second.endDate!!) }
                )
                if (entries.isNotEmpty()) {
                    val outstanding = entries.sumOf { (_, transaction) ->
                        spreadRemainingAmount(transaction, today)
                    }
                    val totalSpread = entries.sumOf { (_, transaction) -> transaction.amount }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                bucket,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "${formatMoney(outstanding)} / ${formatMoney(totalSpread)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                                Text(
                                    "Outstanding / total spread",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    items(entries, key = { it.first }) { (index, transaction) ->
                        val daily = transaction.amount / daysBetween(transaction.startDate, transaction.endDate!!)
                        Column(
                            modifier = Modifier.fillMaxWidth().clickable { onEdit(index) }
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    transaction.description,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        formatMoney(daily),
                                        color = signalColor(if (transaction.type == TransactionType.INCOME) 1.0 else -1.0)
                                    )
                                    Text(
                                        "${formatMoney(spreadRemainingAmount(transaction, today))} / ${formatMoney(transaction.amount)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Text(
                                "${if (transaction.type == TransactionType.INCOME) "Income" else "Expense"} · " +
                                    "${shortDate(transaction.startDate)} – ${shortDate(transaction.endDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySettings(
    transactions: List<Transaction>,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Transaction,
    onRestore: (Int, Transaction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val history = transactions.mapIndexed { index, item -> index to item }
        .filter { it.second.recurrence == null }
        .groupBy({ it.second.startDate }, { it })
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (history.isEmpty()) {
            item { Text("No history yet.") }
        } else {
            history.entries.sortedByDescending { it.key }.forEach { (date, dateItems) ->
                item(key = "date-$date") {
                    Text(date, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                }
                items(dateItems, key = { it.first }) { (index, item) ->
                    HistoryRow(item, { onEdit(index) }) {
                        val deleted = onDelete(index)
                        scope.launch {
                            if (snackbarHostState.showSnackbar(
                                    "Transaction deleted", actionLabel = "Undo"
                                ) == SnackbarResult.ActionPerformed
                            ) onRestore(index, deleted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(transaction: Transaction, onEdit: () -> Unit, onDelete: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit).padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.description, maxLines = 1, overflow = TextOverflow.Ellipsis)
                transaction.endDate?.let {
                    Text("Through $it", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(
                    transactionAmountLabel(transaction),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = signalColor(if (transaction.type == TransactionType.INCOME) 1.0 else -1.0)
                )
                Text(
                    transaction.bucket,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete transaction")
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete transaction?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun WishlistCard(
    item: WishlistItem,
    index: Int,
    rankIndex: Int = index,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    interactive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val fill = when (rankIndex) {
        0 -> Color(0xFF6B5125)
        1 -> Color(0xFF5C5C5C)
        2 -> Color(0xFF66432F)
        else -> Color(0xFF181818)
    }
    Row(modifier = modifier.fillMaxWidth().height(72.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            if (rankIndex < 3) "#${rankIndex + 1}" else "",
            modifier = Modifier.width(28.dp),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleMedium
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = fill),
            modifier = Modifier.weight(1f).then(if (interactive) Modifier.clickable(onClick = onEdit) else Modifier)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.label, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(formatMoney(item.cost) + (item.bucket?.let { " · $it" } ?: ""), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (interactive) TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun WishlistSettings(
    wishlist: List<WishlistItem>,
    buckets: List<String>,
    onAdd: (WishlistItem) -> Unit,
    onEdit: (Int, WishlistItem) -> Unit,
    onDelete: (Int) -> Unit,
    onReorder: (Int, Int) -> Unit
) {
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragStartOrder by remember { mutableStateOf<List<WishlistItem>?>(null) }
    var dragTop by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val rowHeight = with(density) { 72.dp.toPx() }
    val rowSlot = with(density) { 80.dp.toPx() }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                wishlist.forEachIndexed { index, item ->
                    val isDragging = draggingIndex == index
                    val rankIndex = dragStartOrder?.indexOf(item)?.takeIf { it >= 0 } ?: index
                    WishlistCard(
                        item = item,
                        index = index,
                        rankIndex = rankIndex,
                        onEdit = {
                            editingIndex = index
                            showEditor = true
                        },
                        onDelete = { onDelete(index) },
                        modifier = Modifier.alpha(if (isDragging) 0f else 1f).pointerInput(Unit) {
                            var currentIndex = index
                            detectDragGesturesAfterLongPress(
                                onDragStart = { touch ->
                                    draggingIndex = index
                                    dragStartOrder = wishlist.toList()
                                    dragTop = currentIndex * rowSlot - touch.y
                                },
                                onDragEnd = { draggingIndex = null; dragStartOrder = null },
                                onDragCancel = { draggingIndex = null; dragStartOrder = null },
                                onDrag = { _, dragAmount ->
                                    dragTop += dragAmount.y
                                    val target = ((dragTop + rowHeight / 2) / rowSlot)
                                        .roundToInt().coerceIn(0, wishlist.lastIndex)
                                    if (target != currentIndex) {
                                        onReorder(currentIndex, target)
                                        currentIndex = target
                                        draggingIndex = target
                                    }
                                }
                            )
                        }
                    )
                }
            }
            draggingIndex?.let { index ->
                wishlist.getOrNull(index)?.let { item ->
                    val rankIndex = dragStartOrder?.indexOf(item)?.takeIf { it >= 0 } ?: index
                    WishlistCard(
                        item = item,
                        index = index,
                        rankIndex = rankIndex,
                        onEdit = {},
                        onDelete = {},
                        interactive = false,
                        modifier = Modifier.offset { IntOffset(0, dragTop.roundToInt()) }
                            .zIndex(2f)
                            .graphicsLayer {
                                scaleX = 1.03f
                                scaleY = 1.03f
                                shadowElevation = 10.dp.toPx()
                            }
                    )
                }
            }
        }
        Button(onClick = { editingIndex = null; showEditor = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Add wishlist item")
        }
    }
    if (showEditor) {
        WishlistDialog(
            initial = editingIndex?.let { wishlist[it] },
            buckets = buckets,
            onDismiss = { showEditor = false },
            onSave = { item ->
                editingIndex?.let { onEdit(it, item) } ?: onAdd(item)
                showEditor = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WishlistDialog(
    initial: WishlistItem?,
    buckets: List<String>,
    onDismiss: () -> Unit,
    onSave: (WishlistItem) -> Unit
) {
    var label by remember { mutableStateOf(initial?.label ?: "") }
    var amount by remember { mutableStateOf(TextFieldValue(formatAmountInput(initial?.cost ?: 0.0))) }
    var bucket by remember { mutableStateOf(initial?.bucket) }
    var expanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val amountFocusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add wishlist item" else "Edit wishlist item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { amountFocusRequester.requestFocus() }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = TextFieldValue(cashInput(it.text), TextRange(cashInput(it.text).length)) },
                    label = { Text("Cost") },
                    prefix = { Text("RM ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(amountFocusRequester)
                )
                ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
                    OutlinedTextField(
                        value = bucket ?: "No bucket",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bucket (optional)") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    DropdownMenu(expanded, { expanded = false }) {
                        DropdownMenuItem(text = { Text("No bucket") }, onClick = { bucket = null; expanded = false })
                        buckets.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = { bucket = option; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val cost = amount.text.toDoubleOrNull() ?: 0.0
                if (label.isNotBlank() && cost > 0) onSave(WishlistItem(label.trim(), cost, bucket))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun BucketSettings(
    buckets: List<String>,
    onAdd: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var addError by remember { mutableStateOf<String?>(null) }
    Column(modifier = Modifier.fillMaxWidth()) {
        buckets.forEach { bucket ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(bucket, modifier = Modifier.padding(12.dp))
                TextButton(onClick = { if (buckets.size > 1) onDelete(bucket) }) { Text("Delete") }
            }
        }
        Button(onClick = { addError = null; showAdd = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Add bucket")
        }
        if (showAdd) {
            AddBucketDialog(
                error = addError,
                onDismiss = { showAdd = false },
                onSave = {
                    if (buckets.any { bucket -> bucket.equals(it.trim(), ignoreCase = true) }) {
                        addError = "A bucket with this name already exists."
                    } else {
                        onAdd(it.trim())
                        showAdd = false
                    }
                }
            )
        }
    }
}

@Composable
private fun WeeklyBalanceChart(
    transactions: List<Transaction>,
    bucket: String,
    setAside: Double,
    includeHistoricalIncome: Boolean
) {
    val today = todayString()
    val chartTransactions = transactions.toList()
    val costAudit = remember(today, chartTransactions, includeHistoricalIncome) {
        historicalCostAudit(chartTransactions, today, includeHistoricalIncome)
    }
    val points = remember(today, chartTransactions) {
        (listOf(-1, 0, 1, 2, 3, 4, 5)).map { offset ->
            val date = shiftDate(today, offset)
            DayPoint(date, offset, availableThroughDate(chartTransactions, date))
        }
    }
    val todayBalance = points[1].value
    val averageCost = costAudit.dailyAverage ?: 0.0
    val historicalValues = remember(points, averageCost) {
        points.map { point ->
            if (point.offset >= 0) point.value - averageCost * point.offset else point.value
        }
    }
    val forecastVisibility = remember(points, historicalValues, averageCost, setAside) {
        points.mapIndexed { index, point ->
            val funded = setAside.takeIf { point.offset >= 0 } ?: 0.0
            val historical = historicalValues[index]
            val showFunded = funded > 0.005
            val showHistorical = point.offset >= 0 &&
                (!showFunded || kotlin.math.abs(historical - funded) > 0.005)
            val showBase = point.offset >= 0 &&
                kotlin.math.abs(point.value - historical) > 0.005 &&
                (!showFunded || kotlin.math.abs(point.value - funded) > 0.005)
            Triple(showBase, showHistorical, showFunded)
        }
    }
    val targetFrame = remember(points, historicalValues, forecastVisibility, setAside) {
        val minValue = minOf(0.0, points.minOf { it.value }, historicalValues.minOf { it })
        val maxValue = maxOf(0.0, points.maxOf { it.value }, historicalValues.maxOf { it })
        val padding = (maxValue - minValue).coerceAtLeast(1.0) * 0.1
        val minimum = minValue - padding
        val maximum = maxValue + padding
        val range = (maximum - minimum).coerceAtLeast(0.01)
        val layers = points.mapIndexed { index, point ->
            val visible = forecastVisibility[index]
            listOf(
                if (point.offset >= 0) point.value else 0.0,
                if (visible.second) historicalValues[index] else 0.0,
                if (visible.third) setAside else 0.0
            )
        }
        ChartFrame(
            balances = points.map { it.value },
            layers = layers,
            visibility = forecastVisibility.map { listOf(it.first, it.second, it.third) },
            balanceY = points.map { (maximum - it.value) / range },
            layerY = layers.map { values -> values.map { (maximum - it) / range } },
            zeroY = maximum / range
        )
    }
    var fromFrame by remember { mutableStateOf(targetFrame) }
    var toFrame by remember { mutableStateOf(targetFrame) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(targetFrame) {
        fromFrame = fromFrame.interpolate(toFrame, progress.value)
        toFrame = targetFrame
        progress.snapTo(0f)
        progress.animateTo(1f, tween(800, easing = LinearEasing))
        fromFrame = targetFrame
        toFrame = targetFrame
    }
    LaunchedEffect(today, chartTransactions, bucket, setAside, includeHistoricalIncome) {
        val breakdown = todayBreakdown(chartTransactions)
        val yesterday = shiftDate(today, -1)
        val yesterdayBalance = availableThroughDate(chartTransactions, yesterday)
        Log.d(
            "LUXE_CHART",
            "CHECK today=$today yesterdayBalance=$yesterdayBalance todayBalance=$todayBalance " +
                "balanceChange=${todayBalance - yesterdayBalance} " +
                "dailyNet=${dailyAmountToday(transactions)} " +
                "regularIncome=${breakdown.regularIncome} spreadIncome=${breakdown.spreadIncome} " +
                "flatIncome=${breakdown.flatIncome} regularExpenses=${breakdown.regularExpenses} " +
                "spreadExpenses=${breakdown.spreadExpenses} flatExpenses=${breakdown.flatExpenses} " +
                "historicalDailyCost=$averageCost historicalBalance=${historicalValues[1]} " +
                "wishlistOverlay=$setAside"
        )
        Log.d(
            "LUXE_HISTORICAL_COST",
            "SUMMARY bucket=$bucket includeIncome=$includeHistoricalIncome " +
                "window=${costAudit.windowStart}..${costAudit.windowEnd} " +
                "firstActivity=${costAudit.firstActivityDate} divisorDays=${costAudit.daysCovered} " +
                "distinctActivityDays=${costAudit.distinctActivityDays} " +
                "emptyLeadingDays=${costAudit.emptyLeadingDays} totalExpenses=${costAudit.totalExpenses} " +
                "totalIncome=${costAudit.totalIncome} netCost=${costAudit.netCost} " +
                "minimumDaysMet=${costAudit.minimumDaysMet} dailyAverageNetCost=${costAudit.dailyAverage} " +
                "usedByChart=$averageCost historicalToday=${historicalValues[1]}"
        )
        val auditDate = "2026-11-02"
        val previousAuditDate = shiftDate(auditDate, -1)
        val dayOffset = daysBetween(today, auditDate)
        val actualExpenses = chartTransactions.mapNotNull { transaction ->
            if (transaction.type != TransactionType.EXPENSE) return@mapNotNull null
            val amount = availableThroughDate(listOf(transaction), previousAuditDate) -
                availableThroughDate(listOf(transaction), auditDate)
            if (amount > 0.005) transaction to amount else null
        }
        val actualExpenseTotal = actualExpenses.sumOf { it.second }
        val includedFlatExpenseTotal = costAudit.activities
            .filter { it.type == TransactionType.EXPENSE && it.startDate == auditDate }
            .sumOf { it.amount }
        val baseAtAuditDate = availableThroughDate(chartTransactions, auditDate)
        val historicalAdjustment = if (dayOffset >= 0) averageCost * dayOffset else null
        Log.d(
            "LUXE_HISTORICAL_COST",
            "NOV02 bucket=$bucket today=$today offsetDays=$dayOffset " +
                "actualExpenseRM=$actualExpenseTotal flatExpenseInAverageRM=$includedFlatExpenseTotal " +
                "averageRMPerDay=$averageCost historicalAdjustmentRM=$historicalAdjustment " +
                "baseBalanceRM=$baseAtAuditDate " +
                "historicalValueRM=${historicalAdjustment?.let { baseAtAuditDate - it } ?: "not forecast; date is past"}"
        )
        actualExpenses.forEach { (transaction, amount) ->
            val included = transaction.recurrence == null && transaction.endDate == null &&
                transaction.startDate in costAudit.windowStart..costAudit.windowEnd
            Log.d(
                "LUXE_HISTORICAL_COST",
                "NOV02_EXPENSE description='${transaction.description}' amountRM=$amount " +
                    "start=${transaction.startDate} end=${transaction.endDate} " +
                    "recurrence=${transaction.recurrence} includedInAverage=$included"
            )
        }
        points.firstOrNull { it.date == auditDate }?.let { point ->
            val index = points.indexOf(point)
            Log.d(
                "LUXE_HISTORICAL_COST",
                "NOV02_CHART baseRM=${point.value} historicalRM=${historicalValues[index]} " +
                    "historicalLayerVisible=${forecastVisibility[index].second}"
            )
        }
        chartTransactions.forEach { transaction ->
            val dayChange = availableThroughDate(listOf(transaction), auditDate) -
                availableThroughDate(listOf(transaction), previousAuditDate)
            if (transaction.startDate != auditDate && dayChange == 0.0) return@forEach
            Log.d(
                "LUXE_HISTORICAL_COST",
                "NOV02_ITEM bucket=${transaction.bucket} type=${transaction.type} " +
                    "description='${transaction.description}' amount=${transaction.amount} " +
                    "start=${transaction.startDate} end=${transaction.endDate} " +
                    "recurrence=${transaction.recurrence} dailyBalanceChangeRM=$dayChange"
            )
        }
    }
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline
    val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val labelPaint = remember { android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG) }
    val pastColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    val baseColor = primary.copy(alpha = 0.16f)
    val historicalColor = primary.copy(alpha = 0.75f)

    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(128.dp)) {
            // Read animation state while drawing so animation frames invalidate the Canvas,
            // not the surrounding composable tree.
            val rawProgress = progress.value
            val frameProgress = rawProgress
            val frame = fromFrame.interpolate(toFrame, frameProgress)
            val zeroY = (frame.zeroY * size.height).toFloat()
            val slotWidth = size.width / points.size
            val barWidth = slotWidth * 0.52f
            drawLine(
                color = outline,
                start = Offset(0f, zeroY),
                end = Offset(size.width, zeroY),
                strokeWidth = 1.dp.toPx()
            )
            points.forEachIndexed { index, point ->
                val value = frame.balances[index]
                val center = index * slotWidth + slotWidth / 2f
                fun drawLabel(value: Double, y: Float, center: Float, alpha: Float) {
                    labelPaint.color = labelColor
                    labelPaint.alpha = (alpha * 255).toInt().coerceIn(0, 255)
                    labelPaint.textSize = 28f
                    labelPaint.textAlign = android.graphics.Paint.Align.CENTER
                    drawContext.canvas.nativeCanvas.drawText(
                        formatNumber(value), center, (y - 8f).coerceAtLeast(22f), labelPaint
                    )
                }
                fun drawBar(
                    barValue: Double,
                    endY: Float,
                    barCenter: Float,
                    color: Color,
                    width: Float,
                    labelAlpha: Float = 1f
                ) {
                    val barHeight = kotlin.math.abs(endY - zeroY)
                    val left = barCenter - width / 2f
                    val aboveZero = endY <= zeroY
                    val top = minOf(endY, zeroY)
                    drawRect(color, Offset(left, top), Size(width, barHeight))
                    if (labelAlpha > 0f) {
                        if (aboveZero) drawLabel(barValue, top, barCenter, labelAlpha)
                        else drawLabel(barValue, endY + 32f, barCenter, labelAlpha)
                    }
                }
                fun drawForecastBar(
                    base: Double,
                    historical: Double,
                    funded: Double,
                    positions: List<Double>,
                    labelAlphas: List<Float>,
                    center: Float,
                    width: Float
                ) {
                    drawBar(base, (positions[0] * size.height).toFloat(), center, baseColor, width, labelAlphas[0])
                    drawBar(historical, (positions[1] * size.height).toFloat(), center, historicalColor, width, labelAlphas[1])
                    drawBar(funded, (positions[2] * size.height).toFloat(), center, Color(0xFF4F7A52), width, labelAlphas[2])
                }
                when {
                    point.offset >= 0 -> {
                        val labelAlphas = (0..2).map { layer ->
                            val wasVisible = fromFrame.visibility[index][layer]
                            val isVisible = toFrame.visibility[index][layer]
                            val coveredByEqualOverlay = (layer + 1..2).any { overlay ->
                                toFrame.visibility[index][overlay] &&
                                    kotlin.math.abs(toFrame.layers[index][layer] - toFrame.layers[index][overlay]) < 0.005
                            }
                            if (coveredByEqualOverlay) 1f else when {
                                wasVisible == isVisible -> if (isVisible) 1f else 0f
                                isVisible -> (rawProgress * 3f).coerceIn(0f, 1f)
                                else -> ((1f - rawProgress) * 3f).coerceIn(0f, 1f)
                            }
                        }
                        drawForecastBar(
                            frame.layers[index][0],
                            frame.layers[index][1],
                            frame.layers[index][2],
                            frame.layerY[index],
                            labelAlphas,
                            center,
                            barWidth
                        )
                    }
                    else -> {
                        drawBar(value, (frame.balanceY[index] * size.height).toFloat(), center, pastColor, barWidth)
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            points.forEachIndexed { index, point ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        dayLabel(point.date),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

private data class DayPoint(val date: String, val offset: Int, val value: Double)

private data class ChartFrame(
    val balances: List<Double>,
    val layers: List<List<Double>>,
    val visibility: List<List<Boolean>>,
    val balanceY: List<Double>,
    val layerY: List<List<Double>>,
    val zeroY: Double
) {
    fun interpolate(target: ChartFrame, fraction: Float) = ChartFrame(
        balances.mapIndexed { i, value -> value + (target.balances[i] - value) * fraction },
        layers.mapIndexed { i, values ->
            values.mapIndexed { j, value -> value + (target.layers[i][j] - value) * fraction }
        },
        if (fraction >= 1f) target.visibility else visibility,
        balanceY.mapIndexed { i, value -> value + (target.balanceY[i] - value) * fraction },
        layerY.mapIndexed { i, values ->
            values.mapIndexed { j, value -> value + (target.layerY[i][j] - value) * fraction }
        },
        zeroY + (target.zeroY - zeroY) * fraction
    )
}

private data class TodayBreakdown(
    val regularIncome: Double = 0.0,
    val spreadIncome: Double = 0.0,
    val regularExpenses: Double = 0.0,
    val spreadExpenses: Double = 0.0,
    val flatIncome: Double = 0.0,
    val flatExpenses: Double = 0.0
)

private fun todayBreakdown(transactions: List<Transaction>): TodayBreakdown {
    val today = parseDate(todayString()) ?: return TodayBreakdown()
    var regularIncome = 0.0
    var spreadIncome = 0.0
    var regularExpenses = 0.0
    var spreadExpenses = 0.0
    var flatIncome = 0.0
    var flatExpenses = 0.0

    transactions.forEach { transaction ->
        val amount = if (transaction.recurrence != null) {
            val start = parseDate(transaction.startDate)
            val end = transaction.endDate?.let { parseDate(it) }
            val due = start != null && !today.before(start) &&
                (end == null || today.before(end)) &&
                isRecurrenceDate(Calendar.getInstance().apply { time = today }, start,
                    transaction.recurrence)
            if (due) transaction.amount else 0.0
        } else {
            val end = transaction.endDate
            if (end == null) {
                if (transaction.startDate == todayString()) transaction.amount else 0.0
            } else {
                val days = daysBetween(transaction.startDate, end)
                val startDate = parseDate(transaction.startDate)
                val endDate = parseDate(end)
                if (days > 0 && startDate != null && endDate != null &&
                    !today.before(startDate) && today.before(endDate)
                ) transaction.amount / days else 0.0
            }
        }
        when {
            transaction.recurrence != null && transaction.type == TransactionType.INCOME ->
                regularIncome += amount
            transaction.recurrence != null -> regularExpenses += amount
            transaction.endDate != null && transaction.type == TransactionType.INCOME -> spreadIncome += amount
            transaction.endDate != null -> spreadExpenses += amount
            transaction.type == TransactionType.INCOME -> flatIncome += amount
            else -> flatExpenses += amount
        }
    }
    return TodayBreakdown(regularIncome, spreadIncome, regularExpenses, spreadExpenses, flatIncome, flatExpenses)
}

private fun dailyAmountToday(transactions: List<Transaction>): Double {
    val breakdown = todayBreakdown(transactions)
    return breakdown.regularIncome + breakdown.spreadIncome + breakdown.flatIncome -
        breakdown.regularExpenses - breakdown.spreadExpenses - breakdown.flatExpenses
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionScreen(
    type: TransactionType,
    isRegular: Boolean,
    isUnifiedIncome: Boolean,
    initial: Transaction?,
    visible: Boolean,
    formKey: Int,
    defaultBucket: String,
    defaultCurrency: String,
    preferences: SharedPreferences,
    transactions: List<Transaction>,
    buckets: List<String>,
    onBack: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    BackHandler(enabled = visible, onBack = onBack)

    val today = remember { todayString() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val amountFocusRequester = remember { FocusRequester() }
    var amount by remember(formKey, initial) {
        val text = (initial?.originalAmount ?: initial?.amount)?.let(::formatAmountInput) ?: "0.00"
        mutableStateOf(TextFieldValue(text, TextRange(text.length)))
    }
    var currency by remember(formKey, initial) { mutableStateOf(initial?.currency ?: defaultCurrency) }
    var description by remember(formKey, initial) { mutableStateOf(initial?.description ?: "") }
    var startDate by remember(formKey, initial) { mutableStateOf(initial?.startDate ?: today) }
    var endDate by remember(formKey, initial) { mutableStateOf(initial?.endDate ?: "") }
    var recurrence by remember(formKey, initial) { mutableStateOf(initial?.recurrence ?: Recurrence.DAILY) }
    var bucket by remember(formKey, initial) { mutableStateOf(initial?.bucket ?: defaultBucket) }
    val availableToday = remember(transactions, bucket, today) {
        availableThroughDate(transactionsForBucket(transactions, bucket), today)
    }
    val allocationAmounts = remember(formKey, initial, buckets.toList()) {
        mutableStateListOf<TextFieldValue>().apply {
            buckets.forEachIndexed { index, name ->
                val saved = initial?.allocations?.firstOrNull { it.bucket == name }
                val value = when (initial?.incomeVersion) {
                    1 -> if (initial != null) saved?.percentage?.let {
                        kotlin.math.round(initial.amount * it) / 100.0
                    } ?: 0.0 else 0.0
                    2 -> saved?.amount ?: 0.0
                    else -> if (index == 0) initial?.amount ?: 0.0 else 0.0
                }
                val text = formatAmountInput(value)
                add(TextFieldValue(text, TextRange(text.length)))
            }
        }
    }
    var allocationCompleted by remember(formKey, initial, buckets.toList()) {
        mutableStateOf(initial?.incomeVersion != null)
    }
    fun masterAmountInBase(text: String, selectedCurrency: String = currency): Double {
        val entered = text.toDoubleOrNull() ?: 0.0
        val rate = if (initial != null && entered == initial.originalAmount && selectedCurrency == initial.currency)
            initial.exchangeRate else effectiveRate(selectedCurrency, TransactionType.INCOME)
        return roundMoney(entered * rate)
    }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(visible, formKey) {
        if (visible) {
            amountFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Scaffold(
        containerColor = Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(when {
                        isUnifiedIncome -> if (initial == null) "Add unified income" else "Edit unified income"
                        isRegular && type == TransactionType.INCOME -> "Add regular income"
                        isRegular -> "Add regular expense"
                        initial != null && type == TransactionType.INCOME -> "Edit income"
                        initial != null -> "Edit expense"
                        type == TransactionType.INCOME -> "Add income"
                        else -> "Add expense"
                    })
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            if (!isUnifiedIncome) {
                BucketDropdown(bucket, buckets, onBucketChange = { bucket = it })
                Text(
                    "Available today: ${formatMoney(availableToday)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurrencyDropdown(currency, {
                    currency = it
                    preferences.edit().putString("last_currency", it).apply()
                    if (isUnifiedIncome && allocationAmounts.isNotEmpty()) {
                        val remainder = masterAmountInBase(amount.text, it) - allocationAmounts.drop(1)
                            .sumOf { value -> value.text.toDoubleOrNull() ?: 0.0 }
                        val text = formatAmountInput(remainder.coerceAtLeast(0.0))
                        allocationAmounts[0] = TextFieldValue(text, TextRange(text.length))
                    }
                }, Modifier.width(76.dp))
                OutlinedTextField(
                    amount,
                    {
                        val next = cashInput(it.text)
                        amount = TextFieldValue(next, TextRange(next.length))
                        if (isUnifiedIncome && allocationAmounts.isNotEmpty()) {
                            val remainder = masterAmountInBase(next) - allocationAmounts.drop(1)
                                .sumOf { value -> value.text.toDoubleOrNull() ?: 0.0 }
                            val allocation = formatAmountInput(remainder.coerceAtLeast(0.0))
                            allocationAmounts[0] = TextFieldValue(allocation, TextRange(allocation.length))
                        }
                    },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                    modifier = Modifier.weight(1f).focusRequester(amountFocusRequester)
                )
            }
            if (currency != "MYR") {
                val rate = effectiveRate(currency, type)
                val converted = (amount.text.toDoubleOrNull() ?: 0.0) * rate
                Text(
                    "≈ ${formatMoney(converted)} at " +
                        String.format(Locale.US, "RM %.4f per %s", rate, currency),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            if (isUnifiedIncome) {
                val master = masterAmountInBase(amount.text)
                Text("Allocate ${formatMoney(master)} across buckets (RM)", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Press Enter to move down. The last bucket fills the remainder; later edits rebalance the first bucket.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
                buckets.forEachIndexed { index, bucketName ->
                    OutlinedTextField(
                        value = allocationAmounts[index],
                        onValueChange = { raw ->
                            val text = cashInput(raw.text)
                            val entered = text.toDoubleOrNull() ?: 0.0
                            fun setAllocation(position: Int, value: Double) {
                                val formatted = formatAmountInput(value)
                                allocationAmounts[position] = TextFieldValue(formatted, TextRange(formatted.length))
                            }
                            if (!allocationCompleted || buckets.size == 1) {
                                setAllocation(index, entered)
                            } else {
                                val balanceIndex = if (index == 0) buckets.lastIndex else 0
                                if (balanceIndex == index) {
                                    val others = allocationAmounts.indices.filter { it != index }
                                        .sumOf { allocationAmounts[it].text.toDoubleOrNull() ?: 0.0 }
                                    setAllocation(index, (master - others).coerceAtLeast(0.0))
                                } else {
                                    val otherTotal = allocationAmounts.indices
                                        .filter { it != index && it != balanceIndex }
                                        .sumOf { allocationAmounts[it].text.toDoubleOrNull() ?: 0.0 }
                                    val accepted = entered.coerceIn(0.0, (master - otherTotal).coerceAtLeast(0.0))
                                    setAllocation(index, accepted)
                                    val balance = master - allocationAmounts.indices
                                        .filter { it != balanceIndex }
                                        .sumOf { allocationAmounts[it].text.toDoubleOrNull() ?: 0.0 }
                                    setAllocation(balanceIndex, balance.coerceAtLeast(0.0))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().onFocusChanged { state ->
                            if (state.isFocused && index == buckets.lastIndex && !allocationCompleted) {
                                val remainder = master - allocationAmounts.indices
                                    .filter { it != index }
                                    .sumOf { allocationAmounts[it].text.toDoubleOrNull() ?: 0.0 }
                                if (remainder >= 0.0) {
                                    val text = formatAmountInput(remainder)
                                    allocationAmounts[index] = TextFieldValue(text, TextRange(text.length))
                                } else {
                                    val firstRemainder = master - allocationAmounts.indices
                                        .filter { it != 0 && it != index }
                                        .sumOf { allocationAmounts[it].text.toDoubleOrNull() ?: 0.0 }
                                    allocationAmounts[index] = TextFieldValue("0.00", TextRange(4))
                                    if (firstRemainder >= 0.0) {
                                        val text = formatAmountInput(firstRemainder)
                                        allocationAmounts[0] = TextFieldValue(text, TextRange(text.length))
                                    }
                                }
                                allocationCompleted = true
                            }
                        },
                        label = { Text(bucketName) },
                        prefix = { Text("RM") },
                        singleLine = true,
                        readOnly = buckets.size == 1 && allocationCompleted,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        })
                    )
                }
                val allocated = allocationAmounts.sumOf { it.text.toDoubleOrNull() ?: 0.0 }
                Text(
                    "Allocated: ${formatMoney(allocated)} / ${formatMoney(master)}",
                    color = if (kotlin.math.abs(allocated - master) <= 0.005) MutedGreen else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            OutlinedTextField(
                description,
                { description = it },
                label = { Text("Description") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(Modifier.weight(1f)) {
                    DateField("Start date", startDate) { startDate = it }
                }
                Box(Modifier.weight(1f)) {
                    DateField("Ends before", endDate, allowClear = true) { endDate = it }
                }
            }
            if (isRegular) {
                RecurrenceDropdown(recurrence, { recurrence = it })
            }
            if (endDate.isNotBlank()) {
                val dailyRate = spreadDailyRate(amount.text, startDate, endDate)
                Text(
                    if (dailyRate == null) "Enter valid dates to see the daily rate."
                    else if (currency == "MYR") "${formatMoney(dailyRate)} / day"
                    else {
                        val exchangeRate = effectiveRate(currency, type)
                        "${formatDisplayAmount(currency, dailyRate)} / day ≈ " +
                            "${formatMoney(dailyRate * exchangeRate)} / day"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = {
                val parsedAmount = amount.text.toDoubleOrNull()
                val validStart = parseDate(startDate) != null
                val validEnd = endDate.isBlank() || parseDate(endDate) != null
                val validInterval = endDate.isBlank() || daysBetween(startDate, endDate) > 0
                error = when {
                    parsedAmount == null || parsedAmount <= 0 -> "Enter an amount greater than zero."
                    description.isBlank() -> "Enter a description."
                    !validStart -> "Use a valid start date in YYYY-MM-DD format."
                    isUnifiedIncome && startDate < today -> "Unified income must start today or later to preserve past totals."
                    !validEnd || !validInterval -> "End date must be valid and after the start date."
                    isUnifiedIncome && (
                        allocationAmounts.any { it.text.toDoubleOrNull() == null || it.text.toDoubleOrNull()!! < 0.0 } ||
                            kotlin.math.abs(allocationAmounts.sumOf { it.text.toDoubleOrNull() ?: 0.0 } - masterAmountInBase(amount.text)) > 0.005
                        ) -> "Bucket amounts must equal the master income and cannot be negative."
                    else -> null
                }
                if (error == null && parsedAmount != null) {
                    val appliedRate = if (
                        initial != null &&
                        parsedAmount == initial.originalAmount &&
                        currency == initial.currency
                    ) initial.exchangeRate else effectiveRate(currency, type)
                    onSave(Transaction(
                        amount = if (isUnifiedIncome) roundMoney(parsedAmount * appliedRate) else parsedAmount * appliedRate,
                        description = description,
                        startDate = startDate,
                        endDate = endDate.ifBlank { null },
                        recurrence = if (isRegular) recurrence else null,
                        currency = currency,
                        exchangeRate = appliedRate,
                        originalAmount = parsedAmount,
                        type = type,
                        incomeVersion = if (isUnifiedIncome) 2 else null,
                        allocations = if (isUnifiedIncome) buckets.mapIndexed { index, bucketName ->
                            BucketAllocation(bucketName, allocationAmounts[index].text.toDoubleOrNull() ?: 0.0)
                        } else emptyList(),
                        bucket = if (isUnifiedIncome) "" else bucket
                    ))
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Save ${if (isUnifiedIncome) "unified " else if (isRegular) "regular " else ""}" +
                        if (type == TransactionType.INCOME) "income" else "expense"
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurrenceDropdown(selected: Recurrence, onChange: (Recurrence) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
        Row(
            modifier = Modifier.menuAnchor().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selected.label, style = MaterialTheme.typography.titleLarge)
            Text(
                "▾",
                modifier = Modifier.padding(horizontal = 6.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 24.sp
            )
        }
        DropdownMenu(expanded, { expanded = false }) {
            Recurrence.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = { onChange(option); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BucketDropdown(
    selected: String,
    buckets: List<String>,
    onBucketChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
        Row(
            modifier = Modifier.menuAnchor().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(selected, style = MaterialTheme.typography.titleLarge)
            Text(
                "▾",
                modifier = Modifier.padding(horizontal = 6.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 24.sp
            )
        }
        DropdownMenu(expanded, { expanded = false }) {
            buckets.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onBucketChange(option); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun CurrencyDropdown(selected: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(
            onClick = { expanded = true },
            modifier = modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (selected == "MYR") Color.Transparent
                    else MaterialTheme.colorScheme.surfaceVariant
                )
        ) {
            Text("$selected ▾")
        }
        DropdownMenu(expanded, { expanded = false }) {
            listOf("MYR", "SGD", "IDR", "USD", "GBP").forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onChange(option); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    value: String,
    allowClear: Boolean = false,
    onChange: (String) -> Unit
) {
    val context = LocalContext.current
    Box(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Select date") },
            modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.matchParentSize().clickable {
            val date = parseDate(value) ?: Calendar.getInstance().time
            Calendar.getInstance().apply { time = date }.let { selected ->
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        onChange(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day))
                    },
                    selected.get(Calendar.YEAR),
                    selected.get(Calendar.MONTH),
                    selected.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        })
    }
    if (allowClear && value.isNotBlank()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onChange("") }) { Text("Clear") }
        }
    }
}

@Composable
private fun AddBucketDialog(
    error: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val nameFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        nameFocusRequester.requestFocus()
        keyboardController?.show()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add bucket") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    name,
                    { name = it },
                    label = { Text("Bucket name") },
                    singleLine = true,
                    modifier = Modifier.focusRequester(nameFocusRequester)
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onSave(name.trim()) }) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun availableThroughDate(transactions: List<Transaction>, date: String): Double =
    transactions.sumOf { transaction ->
        val value = when {
            transaction.recurrence != null -> recurringValueThroughDate(
                transaction.amount,
                transaction.startDate,
                transaction.endDate,
                transaction.recurrence,
                date
            )
            transaction.endDate != null -> spreadValueThroughDate(
                transaction.amount,
                transaction.startDate,
                transaction.endDate,
                date
            )
            else -> oneOffValueThroughDate(transaction.amount, transaction.startDate, date)
        }
        if (transaction.type == TransactionType.INCOME) value else -value
    }

private fun transactionsForBucket(transactions: List<Transaction>, bucket: String): List<Transaction> {
    val direct = transactions.filter { it.bucket == bucket && it.incomeVersion == null }
    val unified = transactions.asSequence()
        .filter { it.type == TransactionType.INCOME && it.recurrence != null &&
            (it.incomeVersion == 1 || it.incomeVersion == 2) }
        .mapNotNull { source ->
            val shares = source.allocations.mapIndexed { index, allocation ->
                val amount = if (source.incomeVersion == 1) {
                    val percent = allocation.percentage ?: 0.0
                    if (index == source.allocations.lastIndex) source.amount - source.allocations.dropLast(1).sumOf {
                        kotlin.math.round(source.amount * (it.percentage ?: 0.0)) / 100.0
                    } else kotlin.math.round(source.amount * percent) / 100.0
                } else allocation.amount
                allocation to amount
            }
            shares.firstOrNull { it.first.bucket == bucket }?.second?.let { amount ->
                source.copy(
                    amount = amount,
                    originalAmount = amount,
                    bucket = bucket,
                    incomeVersion = null,
                    allocations = emptyList()
                )
            }
        }
        .toList()
    return direct + unified
}

private fun spreadRemainingAmount(transaction: Transaction, today: String): Double {
    val end = transaction.endDate ?: return 0.0
    val totalDays = daysBetween(transaction.startDate, end)
    if (totalDays <= 0) return 0.0
    val remainingDays = daysBetween(shiftDate(today, 1), end).coerceIn(0, totalDays)
    return transaction.amount * remainingDays / totalDays
}

private fun recurringValueThroughDate(
    amount: Double,
    start: String,
    end: String?,
    recurrence: Recurrence,
    date: String
): Double {
    val startDate = parseDate(start) ?: return 0.0
    val selectedDate = parseDate(date) ?: return 0.0
    val endDate = end?.let { parseDate(it) }
    if (selectedDate.before(startDate)) return 0.0
    var count = 0
    val cursor = Calendar.getInstance().apply { time = startDate }
    while (!cursor.time.after(selectedDate) && (endDate == null || cursor.time.before(endDate))) {
        if (isRecurrenceDate(cursor, startDate, recurrence)) count++
        cursor.add(Calendar.DAY_OF_YEAR, 1)
    }
    return amount * count
}

private fun isRecurrenceDate(cursor: Calendar, start: java.util.Date, recurrence: Recurrence): Boolean {
    val startCalendar = Calendar.getInstance().apply { time = start }
    return when (recurrence) {
    Recurrence.DAILY -> true
    Recurrence.WEEKLY -> cursor.get(Calendar.DAY_OF_WEEK) == startCalendar.get(Calendar.DAY_OF_WEEK)
    Recurrence.MONTHLY -> cursor.get(Calendar.DAY_OF_MONTH) == startCalendar.get(Calendar.DAY_OF_MONTH)
    Recurrence.YEARLY -> cursor.get(Calendar.MONTH) == startCalendar.get(Calendar.MONTH) &&
        cursor.get(Calendar.DAY_OF_MONTH) == startCalendar.get(Calendar.DAY_OF_MONTH)
    }
}

private fun oneOffValueThroughDate(amount: Double, start: String, date: String): Double {
    val startDate = parseDate(start) ?: return 0.0
    val selectedDate = parseDate(date) ?: return 0.0
    return if (startDate.after(selectedDate)) 0.0 else amount
}

private fun spreadValueThroughDate(amount: Double, start: String, end: String, date: String): Double {
    val totalDays = daysBetween(start, end)
    val startDate = parseDate(start) ?: return 0.0
    val endDate = parseDate(end) ?: return 0.0
    val selectedDate = parseDate(date) ?: return 0.0
    if (totalDays <= 0 || selectedDate.before(startDate)) return 0.0
    val elapsedDays = ((selectedDate.time - startDate.time) / MILLIS_PER_DAY).toInt() + 1
    return amount * elapsedDays.coerceAtMost(totalDays) / totalDays
}

private fun shiftDate(date: String, offset: Int): String {
    val calendar = Calendar.getInstance().apply {
        time = parseDate(date) ?: time
        add(Calendar.DAY_OF_YEAR, offset)
    }
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
}

private fun dayLabel(date: String): String =
    SimpleDateFormat("EEE", Locale.US).format(parseDate(date) ?: Calendar.getInstance().time)

private fun shortDate(date: String): String =
    SimpleDateFormat("d MMM", Locale.US).format(parseDate(date) ?: Calendar.getInstance().time)

private data class DailySpending(val date: String, val amount: Double)

private fun recentDailySpending(transactions: List<Transaction>): List<DailySpending> {
    val today = todayString()
    val start = shiftDate(today, -29)
    val purchases = transactions.filter {
        it.type == TransactionType.EXPENSE && it.recurrence == null && it.endDate == null &&
            it.startDate in start..today
    }
    val amountsByDate = purchases.groupingBy { it.startDate }.fold(0.0) { total, purchase ->
        total + purchase.amount
    }
    return (0 until 30).map { offset ->
        val date = shiftDate(start, offset)
        DailySpending(date, amountsByDate[date] ?: 0.0)
    }
}

private data class HistoricalCostAudit(
    val windowStart: String,
    val windowEnd: String,
    val activities: List<Transaction>,
    val firstActivityDate: String?,
    val daysCovered: Int,
    val distinctActivityDays: Int,
    val emptyLeadingDays: Int,
    val totalExpenses: Double,
    val totalIncome: Double,
    val netCost: Double,
    val minimumDaysMet: Boolean,
    val dailyAverage: Double?
)

private fun historicalCostAudit(
    transactions: List<Transaction>,
    today: String,
    includeIncome: Boolean
): HistoricalCostAudit {
    val start = shiftDate(today, -29)
    val activities = transactions.filter {
        (it.type == TransactionType.EXPENSE || includeIncome) &&
            it.recurrence == null && it.endDate == null &&
            it.startDate in start..today
    }
    val firstDate = activities.minOfOrNull { it.startDate }
    val daysCovered = firstDate?.let { (daysBetween(it, today) + 1).coerceIn(1, 30) } ?: 0
    val distinctDays = activities.map { it.startDate }.distinct().size
    val expenses = activities.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val income = activities.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val netCost = expenses - income
    val enoughDays = distinctDays >= 7
    return HistoricalCostAudit(
        windowStart = start,
        windowEnd = today,
        activities = activities,
        firstActivityDate = firstDate,
        daysCovered = daysCovered,
        distinctActivityDays = distinctDays,
        emptyLeadingDays = firstDate?.let { daysBetween(start, it) } ?: 30,
        totalExpenses = expenses,
        totalIncome = income,
        netCost = netCost,
        minimumDaysMet = enoughDays,
        dailyAverage = if (enoughDays) netCost / daysCovered else null
    )
}

private fun historicalDailyCost(transactions: List<Transaction>, includeIncome: Boolean = false): Double? =
    historicalCostAudit(transactions, todayString(), includeIncome).dailyAverage

private fun spreadDailyRate(amount: String, start: String, end: String): Double? {
    val days = daysBetween(start, end)
    return amount.toDoubleOrNull()?.takeIf { it > 0 }?.let { if (days > 0) it / days else null }
}

private fun daysBetween(start: String, end: String): Int {
    val startDate = parseDate(start) ?: return 0
    val endDate = parseDate(end) ?: return 0
    return ((endDate.time - startDate.time) / MILLIS_PER_DAY).toInt()
}

private fun parseDate(value: String) = try {
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(value)
} catch (_: Exception) { null }

private fun todayString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    .format(Calendar.getInstance().time)

private fun formatAmountInput(value: Double): String = String.format(Locale.US, "%.2f", value)

private fun formatDisplayAmount(currency: String, value: Double): String {
    val pattern = if (kotlin.math.abs(value) >= 1000) "%,.0f" else "%,.2f"
    return "$currency ${String.format(Locale.US, pattern, value)}"
}

private fun cashInput(value: String): String {
    val digits = value.filter(Char::isDigit).trimStart('0')
    if (digits.isEmpty()) return "0.00"
    val cents = digits.toLongOrNull() ?: return "0.00"
    return "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
}

private fun formatMoney(value: Double): String = formatDisplayAmount("RM", value)

private fun transactionAmountLabel(transaction: Transaction): String =
    if (transaction.currency == "MYR") formatMoney(transaction.amount)
    else "${formatDisplayAmount(transaction.currency, transaction.originalAmount)} → ${formatMoney(transaction.amount)}"

private fun effectiveRate(currency: String, type: TransactionType): Double {
    val rate = when (currency) {
        "SGD" -> 3.1820
        "IDR" -> 0.000228
        "USD" -> 4.0715
        "GBP" -> 5.3803
        else -> 1.0
    }
    return rate * if (currency == "MYR") 1.0 else if (type == TransactionType.INCOME) 0.9 else 1.1
}

private fun formatSignedMoney(value: Double): String = String.format(
    Locale.US, "%s%s", if (value >= 0) "+" else "−", formatMoney(kotlin.math.abs(value))
)

private fun formatNumber(value: Double): String {
    val pattern = if (kotlin.math.abs(value) >= 1000) "%,.0f" else "%,.2f"
    return String.format(Locale.US, pattern, value)
}

private fun formatPercent(value: Double) = String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
private fun roundMoney(value: Double) = kotlin.math.round(value * 100.0) / 100.0

private fun createBackupJson(
    preferences: SharedPreferences,
    buckets: List<String>,
    transactions: List<Transaction>,
    wishlist: List<WishlistItem>
): String = JSONObject().apply {
    put("format", "LUXE_BUDGET_BACKUP")
    put("version", 3)
    put("exportedOn", todayString())
    put("data", JSONObject().apply {
        put("buckets", bucketsJson(buckets))
        put("transactions", transactionsJson(transactions))
        put("wishlist", wishlistJson(wishlist))
        put("lastCurrency", preferences.getString("last_currency", "MYR") ?: "MYR")
    })
}.toString(2)

private fun parseBackupJson(text: String): BudgetBackup {
    val root = JSONObject(text)
    require(root.getString("format") == "LUXE_BUDGET_BACKUP" && root.getInt("version") in 1..3) {
        "Unsupported backup format or version"
    }
    val data = root.getJSONObject("data")
    val buckets = decodeBuckets(data.getJSONArray("buckets"))
    require(buckets.isNotEmpty() && buckets.all(String::isNotBlank) && buckets.distinct().size == buckets.size) {
        "The backup must contain unique, non-empty buckets"
    }
    val transactions = decodeTransactions(data.getJSONArray("transactions"))
    transactions.filter { it.incomeVersion != null }.forEach { transaction ->
        val valuesAreValid = when (transaction.incomeVersion) {
            1 -> transaction.allocations.all { it.bucket.isNotBlank() && (it.percentage ?: 0.0) > 0.0 } &&
                kotlin.math.abs(transaction.allocations.sumOf { it.percentage ?: 0.0 } - 100.0) < 0.005
            2 -> transaction.allocations.all { it.bucket.isNotBlank() && it.amount >= 0.0 } &&
                kotlin.math.abs(transaction.allocations.sumOf { it.amount } - transaction.amount) < 0.005
            else -> false
        }
        require(transaction.type == TransactionType.INCOME && transaction.recurrence != null &&
            transaction.allocations.isNotEmpty() && valuesAreValid
        ) { "The backup contains an invalid unified income allocation" }
    }
    return BudgetBackup(
        buckets,
        transactions,
        decodeWishlist(data.getJSONArray("wishlist")),
        data.optString("lastCurrency", "MYR").ifBlank { "MYR" }
    )
}

private fun loadBuckets(preferences: SharedPreferences): List<String> {
    val saved = preferences.getString("buckets", null) ?: return listOf("Needs")
    return decodeBuckets(JSONArray(saved)).ifEmpty { listOf("Needs") }
}

private fun decodeBuckets(json: JSONArray): List<String> = List(json.length()) { json.getString(it).trim() }

private fun bucketsJson(buckets: List<String>) = JSONArray().apply { buckets.forEach(::put) }

private fun saveBuckets(preferences: SharedPreferences, buckets: List<String>) {
    preferences.edit().putString("buckets", bucketsJson(buckets).toString()).apply()
}

private fun loadWishlist(preferences: SharedPreferences): List<WishlistItem> {
    val saved = preferences.getString("wishlist", null) ?: return emptyList()
    return decodeWishlist(JSONArray(saved))
}

private fun decodeWishlist(json: JSONArray): List<WishlistItem> = List(json.length()) { index ->
    val item = json.getJSONObject(index)
    WishlistItem(item.getString("label"), item.getDouble("cost"), item.optString("bucket").takeIf { it.isNotBlank() })
}

private fun wishlistJson(wishlist: List<WishlistItem>) = JSONArray().apply {
    wishlist.forEach { item -> put(JSONObject().apply {
        put("label", item.label)
        put("cost", item.cost)
        put("bucket", item.bucket ?: "")
    }) }
}

private fun saveWishlist(preferences: SharedPreferences, wishlist: List<WishlistItem>) {
    preferences.edit().putString("wishlist", wishlistJson(wishlist).toString()).apply()
}

private fun loadTransactions(preferences: SharedPreferences): List<Transaction> {
    val saved = preferences.getString("transactions", null) ?: return emptyList()
    return decodeTransactions(JSONArray(saved))
}

private fun decodeTransactions(json: JSONArray): List<Transaction> = List(json.length()) { index ->
    val item = json.getJSONObject(index)
    val recurrence = item.optString("recurrence", "")
        .takeIf { it.isNotBlank() }
        ?.let { Recurrence.valueOf(it) }
    Transaction(
        type = TransactionType.valueOf(item.getString("type")),
        amount = item.getDouble("amount"),
        description = item.getString("description"),
        startDate = item.getString("startDate"),
        endDate = item.optString("endDate", "").takeIf { it.isNotBlank() },
        bucket = item.getString("bucket"),
        recurrence = recurrence,
        currency = item.optString("currency", "MYR"),
        exchangeRate = item.optDouble("exchangeRate", 1.0),
        originalAmount = item.optDouble("originalAmount", item.getDouble("amount")),
        incomeVersion = if (item.isNull("incomeVersion")) null else item.optInt("incomeVersion").takeIf { it > 0 },
        allocations = item.optJSONArray("allocations")?.let { allocationJson ->
            val version = item.optInt("incomeVersion", 0)
            List(allocationJson.length()) { allocationIndex ->
                val allocation = allocationJson.getJSONObject(allocationIndex)
                if (version == 1) BucketAllocation(
                    allocation.getString("bucket"), 0.0, allocation.getDouble("percentage")
                ) else BucketAllocation(
                    allocation.getString("bucket"), allocation.optDouble("amount", 0.0)
                )
            }
        } ?: emptyList()
    )
}

private fun transactionsJson(transactions: List<Transaction>) = JSONArray().apply {
    transactions.forEach { transaction -> put(JSONObject().apply {
        put("type", transaction.type.name)
        put("amount", transaction.amount)
        put("description", transaction.description)
        put("startDate", transaction.startDate)
        put("endDate", transaction.endDate ?: "")
        put("bucket", transaction.bucket)
        put("recurrence", transaction.recurrence?.name ?: "")
        put("currency", transaction.currency)
        put("exchangeRate", transaction.exchangeRate)
        put("originalAmount", transaction.originalAmount)
        if (transaction.incomeVersion != null) put("incomeVersion", transaction.incomeVersion)
        put("allocations", JSONArray().apply {
            transaction.allocations.forEach { allocation -> put(JSONObject().apply {
                put("bucket", allocation.bucket)
                if (transaction.incomeVersion == 1) put("percentage", allocation.percentage)
                else put("amount", allocation.amount)
            }) }
        })
    }) }
}

private fun saveTransactions(preferences: SharedPreferences, transactions: List<Transaction>) {
    preferences.edit().putString("transactions", transactionsJson(transactions).toString()).apply()
}

private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
