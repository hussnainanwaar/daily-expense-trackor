package com.example.model

data class ParsedExpense(
    val amount: Double?,
    val currency: String,
    val category: ExpenseCategory,
    val dateMillis: Long,
    val notes: String,
    val isRecurring: Boolean,
    val rawText: String,
    val isConfident: Boolean = true
)
