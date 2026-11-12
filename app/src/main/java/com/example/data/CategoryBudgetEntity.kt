package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_budgets")
data class CategoryBudgetEntity(
    @PrimaryKey
    val category: String,
    val monthlyLimit: Double,
    val currency: String = "$"
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val monthlyOverallBudget: Double = 2500.0,
    val dailyBudget: Double = 80.0,
    val defaultCurrency: String = "$",
    val alertThresholdPercent: Int = 80
)
