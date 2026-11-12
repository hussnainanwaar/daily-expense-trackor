package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val currency: String = "$",
    val category: String,
    val dateMillis: Long,
    val notes: String = "",
    val isRecurring: Boolean = false,
    val recurringFrequency: String? = null, // "Monthly", "Weekly", "Yearly"
    val createdAt: Long = System.currentTimeMillis()
)
