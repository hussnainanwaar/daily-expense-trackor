package com.example.data

import com.example.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val categoryBudgets: Flow<List<CategoryBudgetEntity>> = expenseDao.getAllCategoryBudgets()
    val userSettings: Flow<UserSettingsEntity?> = expenseDao.getUserSettings()

    suspend fun insertExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }

    suspend fun deleteExpenseById(id: Long) {
        expenseDao.deleteExpenseById(id)
    }

    suspend fun clearAllExpenses() {
        expenseDao.clearAllExpenses()
    }

    suspend fun upsertCategoryBudget(budget: CategoryBudgetEntity) {
        expenseDao.upsertCategoryBudget(budget)
    }

    suspend fun upsertUserSettings(settings: UserSettingsEntity) {
        expenseDao.upsertUserSettings(settings)
    }

    suspend fun seedInitialDataIfNeeded() {
        val existing = expenseDao.getAllExpenses().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val samples = mutableListOf<ExpenseEntity>()

            // --- THIS MONTH EXPENSES ---
            val calendar = Calendar.getInstance()

            // Today
            samples.add(
                ExpenseEntity(
                    amount = 14.50,
                    currency = "$",
                    category = ExpenseCategory.FOOD.displayName,
                    dateMillis = calendar.timeInMillis,
                    notes = "Lunch at Chipotle",
                    isRecurring = false
                )
            )
            samples.add(
                ExpenseEntity(
                    amount = 4.75,
                    currency = "$",
                    category = ExpenseCategory.FOOD.displayName,
                    dateMillis = calendar.timeInMillis - (2 * 3600 * 1000),
                    notes = "Iced Latte at Starbucks",
                    isRecurring = false
                )
            )

            // Yesterday
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            samples.add(
                ExpenseEntity(
                    amount = 62.40,
                    currency = "$",
                    category = ExpenseCategory.GROCERIES.displayName,
                    dateMillis = calendar.timeInMillis,
                    notes = "Trader Joe's groceries",
                    isRecurring = false
                )
            )
            samples.add(
                ExpenseEntity(
                    amount = 22.80,
                    currency = "$",
                    category = ExpenseCategory.TRANSPORT.displayName,
                    dateMillis = calendar.timeInMillis - (4 * 3600 * 1000),
                    notes = "Uber downtown",
                    isRecurring = false
                )
            )

            // 2 days ago
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            samples.add(
                ExpenseEntity(
                    amount = 15.99,
                    currency = "$",
                    category = ExpenseCategory.SUBSCRIPTIONS.displayName,
                    dateMillis = calendar.timeInMillis,
                    notes = "Netflix Premium",
                    isRecurring = true,
                    recurringFrequency = "Monthly"
                )
            )
            samples.add(
                ExpenseEntity(
                    amount = 35.00,
                    currency = "$",
                    category = ExpenseCategory.ENTERTAINMENT.displayName,
                    dateMillis = calendar.timeInMillis,
                    notes = "Cinema tickets",
                    isRecurring = false
                )
            )

            // 3 days ago
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            samples.add(
                ExpenseEntity(
                    amount = 48.00,
                    currency = "$",
                    category = ExpenseCategory.TRANSPORT.displayName,
                    dateMillis = calendar.timeInMillis,
                    notes = "Shell Gas Station",
                    isRecurring = false
                )
            )
            samples.add(
                ExpenseEntity(
                    amount = 89.20,
                    currency = "$",
                    category = ExpenseCategory.UTILITIES.displayName,
                    dateMillis = calendar.timeInMillis,
                    notes = "Electric & Gas bill",
                    isRecurring = true,
                    recurringFrequency = "Monthly"
                )
            )

            // 5 days ago
            calendar.add(Calendar.DAY_OF_YEAR, -2)
            samples.add(
                ExpenseEntity(
                    amount = 11.99,
                    currency = "$",
                    category = ExpenseCategory.SUBSCRIPTIONS.displayName,
                    dateMillis = calendar.timeInMillis,
                    notes = "Spotify Premium",
                    isRecurring = true,
                    recurringFrequency = "Monthly"
                )
            )
            samples.add(
                ExpenseEntity(
                    amount = 75.00,
                    currency = "$",
                    category = ExpenseCategory.SHOPPING.displayName,
                    dateMillis = calendar.timeInMillis,
                    notes = "New running shoes",
                    isRecurring = false
                )
            )

            // --- LAST MONTH HISTORICAL DATA STORAGE ---
            val lastMonth = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
            }

            // Rent 1st of last month
            val lm1 = (lastMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
            samples.add(
                ExpenseEntity(
                    amount = 1200.0,
                    currency = "$",
                    category = ExpenseCategory.HOUSING.displayName,
                    dateMillis = lm1.timeInMillis,
                    notes = "Apartment Rent",
                    isRecurring = true,
                    recurringFrequency = "Monthly"
                )
            )

            // Electric & Wifi 5th of last month
            val lm5 = (lastMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 5) }
            samples.add(
                ExpenseEntity(
                    amount = 142.50,
                    currency = "$",
                    category = ExpenseCategory.UTILITIES.displayName,
                    dateMillis = lm5.timeInMillis,
                    notes = "Electric & Fiber Internet",
                    isRecurring = true,
                    recurringFrequency = "Monthly"
                )
            )

            // Groceries 12th of last month
            val lm12 = (lastMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 12) }
            samples.add(
                ExpenseEntity(
                    amount = 118.75,
                    currency = "$",
                    category = ExpenseCategory.GROCERIES.displayName,
                    dateMillis = lm12.timeInMillis,
                    notes = "Costco wholesale groceries",
                    isRecurring = false
                )
            )

            // Dining out 16th of last month
            val lm16 = (lastMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 16) }
            samples.add(
                ExpenseEntity(
                    amount = 68.00,
                    currency = "$",
                    category = ExpenseCategory.FOOD.displayName,
                    dateMillis = lm16.timeInMillis,
                    notes = "Dinner at Olive Garden",
                    isRecurring = false
                )
            )

            // Gas 19th of last month
            val lm19 = (lastMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 19) }
            samples.add(
                ExpenseEntity(
                    amount = 52.00,
                    currency = "$",
                    category = ExpenseCategory.TRANSPORT.displayName,
                    dateMillis = lm19.timeInMillis,
                    notes = "Exxon Mobil Gas",
                    isRecurring = false
                )
            )

            // Netflix last month
            val lm22 = (lastMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 22) }
            samples.add(
                ExpenseEntity(
                    amount = 15.99,
                    currency = "$",
                    category = ExpenseCategory.SUBSCRIPTIONS.displayName,
                    dateMillis = lm22.timeInMillis,
                    notes = "Netflix Subscription",
                    isRecurring = true,
                    recurringFrequency = "Monthly"
                )
            )

            // Shopping 25th of last month
            val lm25 = (lastMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 25) }
            samples.add(
                ExpenseEntity(
                    amount = 94.50,
                    currency = "$",
                    category = ExpenseCategory.SHOPPING.displayName,
                    dateMillis = lm25.timeInMillis,
                    notes = "Amazon Home Essentials",
                    isRecurring = false
                )
            )

            // Supermarket 28th of last month
            val lm28 = (lastMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 28) }
            samples.add(
                ExpenseEntity(
                    amount = 84.20,
                    currency = "$",
                    category = ExpenseCategory.GROCERIES.displayName,
                    dateMillis = lm28.timeInMillis,
                    notes = "Whole Foods Organic Groceries",
                    isRecurring = false
                )
            )

            expenseDao.insertExpenses(samples)

            // Seed default category budgets
            val defaultBudgets = listOf(
                CategoryBudgetEntity(ExpenseCategory.FOOD.displayName, 450.0, "$"),
                CategoryBudgetEntity(ExpenseCategory.GROCERIES.displayName, 400.0, "$"),
                CategoryBudgetEntity(ExpenseCategory.TRANSPORT.displayName, 200.0, "$"),
                CategoryBudgetEntity(ExpenseCategory.ENTERTAINMENT.displayName, 150.0, "$"),
                CategoryBudgetEntity(ExpenseCategory.SHOPPING.displayName, 250.0, "$"),
                CategoryBudgetEntity(ExpenseCategory.SUBSCRIPTIONS.displayName, 100.0, "$"),
                CategoryBudgetEntity(ExpenseCategory.UTILITIES.displayName, 250.0, "$"),
                CategoryBudgetEntity(ExpenseCategory.HEALTH.displayName, 150.0, "$"),
                CategoryBudgetEntity(ExpenseCategory.HOUSING.displayName, 1200.0, "$"),
                CategoryBudgetEntity(ExpenseCategory.OTHER.displayName, 100.0, "$")
            )
            expenseDao.upsertCategoryBudgets(defaultBudgets)

            // Default user settings with dailyBudget
            expenseDao.upsertUserSettings(
                UserSettingsEntity(
                    id = 1,
                    monthlyOverallBudget = 2500.0,
                    dailyBudget = 80.0,
                    defaultCurrency = "$",
                    alertThresholdPercent = 80
                )
            )
        }
    }
}
