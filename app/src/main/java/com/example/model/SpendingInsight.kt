package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class InsightType {
    DAILY_SUMMARY,
    BUDGET_ALERT,
    SUBSCRIPTION_FLAG,
    SAVINGS_TIP
}

data class SpendingInsight(
    val id: String,
    val type: InsightType,
    val title: String,
    val description: String,
    val accentColor: Color,
    val icon: ImageVector
)
