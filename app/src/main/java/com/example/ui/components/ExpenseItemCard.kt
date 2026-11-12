package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseEntity
import com.example.model.ExpenseCategory
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseItemCard(
    expense: ExpenseEntity,
    onDelete: (ExpenseEntity) -> Unit,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val category = ExpenseCategory.fromDisplayName(expense.category)
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val formattedDate = remember(expense.dateMillis) {
        val calendar = Calendar.getInstance()
        val todayYear = calendar.get(Calendar.YEAR)
        val todayDay = calendar.get(Calendar.DAY_OF_YEAR)

        val expCal = Calendar.getInstance().apply { timeInMillis = expense.dateMillis }
        val expYear = expCal.get(Calendar.YEAR)
        val expDay = expCal.get(Calendar.DAY_OF_YEAR)

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val timeStr = timeFormat.format(Date(expense.dateMillis))

        when {
            expYear == todayYear && expDay == todayDay -> "Today, $timeStr"
            expYear == todayYear && expDay == todayDay - 1 -> "Yesterday, $timeStr"
            else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(expense.dateMillis))
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCardSurface)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("expense_item_${expense.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Icon Circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(category.color.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.displayName,
                tint = category.color,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Center Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = expense.notes.ifBlank { category.displayName },
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = category.displayName,
                    color = category.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "•",
                    color = TextSubtle,
                    fontSize = 11.sp
                )

                Text(
                    text = formattedDate,
                    color = TextMuted,
                    fontSize = 11.sp
                )

                if (expense.isRecurring) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(InfoCyan.copy(alpha = 0.18f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Recurring",
                                tint = InfoCyan,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = expense.recurringFrequency ?: "Recurring",
                                color = InfoCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Amount & Delete
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "-${expense.currency}${String.format(Locale.US, "%.2f", expense.amount)}",
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("expense_amount_${expense.id}")
            )

            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier
                    .size(28.dp)
                    .testTag("delete_expense_button_${expense.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete expense",
                    tint = TextSubtle,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Expense?", color = TextWhite) },
            text = {
                Text(
                    "Are you sure you want to remove '${expense.notes.ifBlank { category.displayName }}' (-${expense.currency}${String.format(Locale.US, "%.2f", expense.amount)})?",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(expense)
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete", color = DangerCoral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkElevatedSurface
        )
    }
}
