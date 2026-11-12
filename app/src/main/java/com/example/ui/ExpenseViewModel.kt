package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CategoryBudgetEntity
import com.example.data.ExpenseEntity
import com.example.data.ExpenseRepository
import com.example.data.UserSettingsEntity
import com.example.model.ExpenseCategory
import com.example.model.InsightType
import com.example.model.ParsedExpense
import com.example.model.SpendingInsight
import com.example.nlp.ExpenseNaturalLanguageParser
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.SoftBlueSecondary
import com.example.ui.theme.WarningAmber
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Subscriptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ExpensePeriod(val displayName: String) {
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    ALL_TIME("All Time")
}

data class CategoryBudgetProgress(
    val category: String,
    val spent: Double,
    val limit: Double,
    val percent: Float, // 0.0f .. 1.0f+
    val isWarning: Boolean,
    val isExceeded: Boolean
)

data class DaySpend(
    val dayLabel: String, // "Mon", "Tue"
    val dateLabel: String, // "Nov 12"
    val amount: Double,
    val isToday: Boolean
)

data class MonthOverMonthStats(
    val thisMonthTotal: Double,
    val lastMonthTotal: Double,
    val difference: Double, // thisMonth - lastMonth
    val percentChange: Double,
    val isSavings: Boolean
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    val allExpenses: StateFlow<List<ExpenseEntity>>
    val categoryBudgets: StateFlow<List<CategoryBudgetEntity>>
    val userSettings: StateFlow<UserSettingsEntity>

    val naturalLanguageInput = MutableStateFlow("")
    val filterCategory = MutableStateFlow<String?>(null)
    val searchQuery = MutableStateFlow("")
    val selectedPeriod = MutableStateFlow(ExpensePeriod.THIS_MONTH)

    val parsedPreview: StateFlow<ParsedExpense?> = naturalLanguageInput
        .combine(MutableStateFlow("$")) { text, curr ->
            if (text.isBlank()) null else ExpenseNaturalLanguageParser.parse(text, curr)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(database.expenseDao())

        allExpenses = repository.allExpenses.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        categoryBudgets = repository.categoryBudgets.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        userSettings = repository.userSettings.combine(MutableStateFlow(Unit)) { settings, _ ->
            settings ?: UserSettingsEntity()
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserSettingsEntity()
        )

        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    // Month Names
    val thisMonthName: String = rememberMonthName(0)
    val lastMonthName: String = rememberMonthName(-1)

    private fun rememberMonthName(offsetMonths: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, offsetMonths)
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    // Today's total spend
    val todayTotal: StateFlow<Double> = allExpenses.combine(MutableStateFlow(Unit)) { expenses, _ ->
        val calendarToday = Calendar.getInstance()
        val year = calendarToday.get(Calendar.YEAR)
        val dayOfYear = calendarToday.get(Calendar.DAY_OF_YEAR)

        expenses.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            c.get(Calendar.YEAR) == year && c.get(Calendar.DAY_OF_YEAR) == dayOfYear
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Daily budget calculations
    val dailyBudget: StateFlow<Double> = userSettings.combine(MutableStateFlow(Unit)) { settings, _ ->
        settings.dailyBudget
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 80.0)

    val todayRemaining: StateFlow<Double> = combine(todayTotal, dailyBudget) { spent, budget ->
        budget - spent
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 80.0)

    val todayBudgetRatio: StateFlow<Float> = combine(todayTotal, dailyBudget) { spent, budget ->
        if (budget > 0) (spent / budget).toFloat() else 0f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // Current Month's total
    val monthTotal: StateFlow<Double> = allExpenses.combine(MutableStateFlow(Unit)) { expenses, _ ->
        val calendarThisMonth = Calendar.getInstance()
        val year = calendarThisMonth.get(Calendar.YEAR)
        val month = calendarThisMonth.get(Calendar.MONTH)

        expenses.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Last Month's total
    val lastMonthTotal: StateFlow<Double> = allExpenses.combine(MutableStateFlow(Unit)) { expenses, _ ->
        val calLastMonth = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        val year = calLastMonth.get(Calendar.YEAR)
        val month = calLastMonth.get(Calendar.MONTH)

        expenses.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Month-over-Month Comparison
    val monthComparison: StateFlow<MonthOverMonthStats> = combine(monthTotal, lastMonthTotal) { thisM, lastM ->
        val diff = thisM - lastM
        val pct = if (lastM > 0) ((thisM - lastM) / lastM) * 100.0 else 0.0
        MonthOverMonthStats(
            thisMonthTotal = thisM,
            lastMonthTotal = lastM,
            difference = diff,
            percentChange = pct,
            isSavings = diff <= 0
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthOverMonthStats(0.0, 0.0, 0.0, 0.0, true)
    )

    // Expenses filtered by selectedPeriod, category, and search query
    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
        allExpenses,
        selectedPeriod,
        filterCategory,
        searchQuery
    ) { expenses, period, category, query ->
        val calNow = Calendar.getInstance()
        val thisYear = calNow.get(Calendar.YEAR)
        val thisMonth = calNow.get(Calendar.MONTH)

        val calLast = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        val lastYear = calLast.get(Calendar.YEAR)
        val lastMonth = calLast.get(Calendar.MONTH)

        expenses.filter { expense ->
            val expCal = Calendar.getInstance().apply { timeInMillis = expense.dateMillis }
            val expYear = expCal.get(Calendar.YEAR)
            val expMonth = expCal.get(Calendar.MONTH)

            val matchesPeriod = when (period) {
                ExpensePeriod.THIS_MONTH -> expYear == thisYear && expMonth == thisMonth
                ExpensePeriod.LAST_MONTH -> expYear == lastYear && expMonth == lastMonth
                ExpensePeriod.ALL_TIME -> true
            }

            val matchesCategory = category == null || expense.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                expense.notes.contains(query, ignoreCase = true) ||
                expense.category.contains(query, ignoreCase = true) ||
                expense.amount.toString().contains(query)

            matchesPeriod && matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category progress list for currently selected period (This Month or Last Month)
    val categoryProgressList: StateFlow<List<CategoryBudgetProgress>> = combine(
        allExpenses,
        categoryBudgets,
        userSettings,
        selectedPeriod
    ) { expenses, budgets, settings, period ->
        val calNow = Calendar.getInstance()
        val thisYear = calNow.get(Calendar.YEAR)
        val thisMonth = calNow.get(Calendar.MONTH)

        val calLast = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        val lastYear = calLast.get(Calendar.YEAR)
        val lastMonth = calLast.get(Calendar.MONTH)

        val periodExpenses = expenses.filter {
            val expCal = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            val y = expCal.get(Calendar.YEAR)
            val m = expCal.get(Calendar.MONTH)
            when (period) {
                ExpensePeriod.THIS_MONTH -> y == thisYear && m == thisMonth
                ExpensePeriod.LAST_MONTH -> y == lastYear && m == lastMonth
                ExpensePeriod.ALL_TIME -> true
            }
        }

        val spentByCategory = periodExpenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val budgetMap = budgets.associateBy { it.category }

        ExpenseCategory.entries.map { cat ->
            val spent = spentByCategory[cat.displayName] ?: 0.0
            val limit = budgetMap[cat.displayName]?.monthlyLimit ?: 200.0
            val percent = if (limit > 0) (spent / limit).toFloat() else 0.0f
            CategoryBudgetProgress(
                category = cat.displayName,
                spent = spent,
                limit = limit,
                percent = percent,
                isWarning = percent >= (settings.alertThresholdPercent / 100f) && percent < 1.0f,
                isExceeded = percent >= 1.0f
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weekly spending (last 7 days)
    val weeklySpending: StateFlow<List<DaySpend>> = allExpenses.combine(MutableStateFlow(Unit)) { expenses, _ ->
        val list = mutableListOf<DaySpend>()
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        val todayYear = cal.get(Calendar.YEAR)
        val todayDay = cal.get(Calendar.DAY_OF_YEAR)

        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            val y = c.get(Calendar.YEAR)
            val d = c.get(Calendar.DAY_OF_YEAR)

            val dayTotal = expenses.filter {
                val expCal = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
                expCal.get(Calendar.YEAR) == y && expCal.get(Calendar.DAY_OF_YEAR) == d
            }.sumOf { it.amount }

            list.add(
                DaySpend(
                    dayLabel = dayFormat.format(c.time),
                    dateLabel = dateFormat.format(c.time),
                    amount = dayTotal,
                    isToday = (y == todayYear && d == todayDay)
                )
            )
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic smart insights
    val smartInsights: StateFlow<List<SpendingInsight>> = combine(
        allExpenses,
        categoryProgressList,
        userSettings,
        todayTotal,
        monthComparison
    ) { expenses, catProgress, settings, todaySum, comparison ->
        val insights = mutableListOf<SpendingInsight>()

        // 1. Daily Budget Summary & Alert
        val dailyBgt = settings.dailyBudget
        if (todaySum > dailyBgt) {
            insights.add(
                SpendingInsight(
                    id = "daily_budget_exceeded",
                    type = InsightType.BUDGET_ALERT,
                    title = "Daily Budget Exceeded",
                    description = "You've spent ${settings.defaultCurrency}${String.format(Locale.US, "%.2f", todaySum)} today, exceeding your ${settings.defaultCurrency}${String.format(Locale.US, "%.2f", dailyBgt)} daily budget by ${settings.defaultCurrency}${String.format(Locale.US, "%.2f", todaySum - dailyBgt)}.",
                    accentColor = DangerCoral,
                    icon = Icons.Default.NotificationsActive
                )
            )
        } else {
            insights.add(
                SpendingInsight(
                    id = "daily_budget_ontrack",
                    type = InsightType.DAILY_SUMMARY,
                    title = "Daily Budget Status",
                    description = "Spent ${settings.defaultCurrency}${String.format(Locale.US, "%.2f", todaySum)} of ${settings.defaultCurrency}${String.format(Locale.US, "%.2f", dailyBgt)} daily budget (${settings.defaultCurrency}${String.format(Locale.US, "%.2f", dailyBgt - todaySum)} remaining for today).",
                    accentColor = EmeraldPrimary,
                    icon = Icons.Default.Assessment
                )
            )
        }

        // 2. Month-over-Month Historical Analysis (This Month vs Last Month)
        if (comparison.lastMonthTotal > 0) {
            val changeStr = String.format(Locale.US, "%.1f", Math.abs(comparison.percentChange))
            val diffStr = String.format(Locale.US, "%.0f", Math.abs(comparison.difference))
            val description = if (comparison.isSavings) {
                "Pacing ${changeStr}% lower than last month (${settings.defaultCurrency}${diffStr} less spent so far). You're on track to save more!"
            } else {
                "Spending is pacing ${changeStr}% higher than last month (+${settings.defaultCurrency}${diffStr}). Check your discretionary categories."
            }
            insights.add(
                SpendingInsight(
                    id = "month_comparison",
                    type = if (comparison.isSavings) InsightType.SAVINGS_TIP else InsightType.BUDGET_ALERT,
                    title = if (comparison.isSavings) "Great Monthly Pacing" else "Higher Spending vs Last Month",
                    description = description,
                    accentColor = if (comparison.isSavings) EmeraldPrimary else WarningAmber,
                    icon = Icons.Default.Lightbulb
                )
            )
        }

        // 3. Category Warning/Exceeded Alert
        val exceeded = catProgress.filter { it.isExceeded }
        val warning = catProgress.filter { it.isWarning }
        if (exceeded.isNotEmpty()) {
            val first = exceeded.first()
            insights.add(
                SpendingInsight(
                    id = "exceeded_${first.category}",
                    type = InsightType.BUDGET_ALERT,
                    title = "Category Limit Exceeded: ${first.category}",
                    description = "${first.category} reached ${(first.percent * 100).toInt()}% of its ${settings.defaultCurrency}${first.limit.toInt()} target.",
                    accentColor = DangerCoral,
                    icon = Icons.Default.NotificationsActive
                )
            )
        } else if (warning.isNotEmpty()) {
            val first = warning.first()
            insights.add(
                SpendingInsight(
                    id = "warning_${first.category}",
                    type = InsightType.BUDGET_ALERT,
                    title = "Approaching Limit: ${first.category}",
                    description = "You're at ${(first.percent * 100).toInt()}% of ${first.category} limit (${settings.defaultCurrency}${String.format(Locale.US, "%.2f", first.limit - first.spent)} left).",
                    accentColor = WarningAmber,
                    icon = Icons.Default.NotificationsActive
                )
            )
        }

        // 4. Subscriptions Audit
        val recurringList = expenses.filter { it.isRecurring }
        if (recurringList.isNotEmpty()) {
            val recurringSum = recurringList.sumOf { it.amount }
            insights.add(
                SpendingInsight(
                    id = "recurring_flag",
                    type = InsightType.SUBSCRIPTION_FLAG,
                    title = "Subscription Audit",
                    description = "${recurringList.size} recurring subscriptions active (~${settings.defaultCurrency}${String.format(Locale.US, "%.2f", recurringSum)}/month total).",
                    accentColor = InfoCyan,
                    icon = Icons.Default.Subscriptions
                )
            )
        }

        insights
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onNaturalLanguageChange(text: String) {
        naturalLanguageInput.value = text
    }

    fun submitNaturalLanguageInput(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val currentText = naturalLanguageInput.value.trim()
        if (currentText.isBlank()) {
            onError("Please enter or dictate an expense first")
            return
        }

        val currency = userSettings.value.defaultCurrency
        val parsed = ExpenseNaturalLanguageParser.parse(currentText, currency)

        if (parsed.amount == null || parsed.amount <= 0.0) {
            onError("Could not detect amount. Try e.g. 'Coffee $4.50' or 'Lunch 12.00'")
            return
        }

        viewModelScope.launch {
            repository.insertExpense(
                ExpenseEntity(
                    amount = parsed.amount,
                    currency = parsed.currency,
                    category = parsed.category.displayName,
                    dateMillis = parsed.dateMillis,
                    notes = parsed.notes.ifBlank { parsed.category.displayName },
                    isRecurring = parsed.isRecurring,
                    recurringFrequency = if (parsed.isRecurring) "Monthly" else null
                )
            )
            naturalLanguageInput.value = ""
            onSuccess("Added ${parsed.currency}${String.format(Locale.US, "%.2f", parsed.amount)} for ${parsed.category.displayName}")
        }
    }

    fun addManualExpense(
        amount: Double,
        currency: String,
        category: ExpenseCategory,
        dateMillis: Long,
        notes: String,
        isRecurring: Boolean,
        recurringFrequency: String?
    ) {
        viewModelScope.launch {
            repository.insertExpense(
                ExpenseEntity(
                    amount = amount,
                    currency = currency,
                    category = category.displayName,
                    dateMillis = dateMillis,
                    notes = notes.ifBlank { category.displayName },
                    isRecurring = isRecurring,
                    recurringFrequency = recurringFrequency
                )
            )
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun deleteExpenseById(id: Long) {
        viewModelScope.launch {
            repository.deleteExpenseById(id)
        }
    }

    fun updateDailyBudget(amount: Double) {
        viewModelScope.launch {
            repository.upsertUserSettings(
                userSettings.value.copy(dailyBudget = amount)
            )
        }
    }

    fun updateOverallBudget(limit: Double) {
        viewModelScope.launch {
            repository.upsertUserSettings(
                userSettings.value.copy(monthlyOverallBudget = limit)
            )
        }
    }

    fun updateCategoryBudget(category: String, limit: Double) {
        viewModelScope.launch {
            repository.upsertCategoryBudget(
                CategoryBudgetEntity(
                    category = category,
                    monthlyLimit = limit,
                    currency = userSettings.value.defaultCurrency
                )
            )
        }
    }

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            repository.upsertUserSettings(
                userSettings.value.copy(defaultCurrency = currency)
            )
        }
    }

    fun updateAlertThreshold(threshold: Int) {
        viewModelScope.launch {
            repository.upsertUserSettings(
                userSettings.value.copy(alertThresholdPercent = threshold)
            )
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllExpenses()
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }
}
