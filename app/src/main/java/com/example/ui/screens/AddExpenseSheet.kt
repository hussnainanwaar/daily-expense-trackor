package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExpenseCategory
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SoftBlueLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseSheet(
    defaultCurrency: String,
    onDismiss: () -> Unit,
    onSave: (amount: Double, currency: String, category: ExpenseCategory, dateMillis: Long, notes: String, isRecurring: Boolean, frequency: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var notesText by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringFrequency by remember { mutableStateOf("Monthly") }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val formattedDate = remember(selectedDateMillis) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(selectedDateMillis))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkElevatedSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("add_expense_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Expense",
                    color = TextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = defaultCurrency,
                        color = EmeraldPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { input ->
                            // Allow valid decimal format
                            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                amountText = input
                            }
                        },
                        placeholder = { Text("0.00", color = TextSubtle, fontSize = 32.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            cursorColor = EmeraldLight
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .width(180.dp)
                            .testTag("amount_input_field")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selector
            Text(
                text = "Category",
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (cat in ExpenseCategory.entries) {
                    val isSelected = selectedCategory == cat
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) cat.color.copy(alpha = 0.25f) else DarkCardSurface)
                            .border(
                                1.dp,
                                if (isSelected) cat.color else DarkBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .testTag("category_option_${cat.name}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = cat.displayName,
                            tint = if (isSelected) cat.color else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.displayName,
                            color = if (isSelected) TextWhite else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes / Merchant Input
            Text(
                text = "Notes / Merchant",
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                placeholder = { Text("e.g. Lunch with team, Groceries, Starbucks", color = TextSubtle) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notes_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = EmeraldLight
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date Selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = EmeraldLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Date: $formattedDate",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Quick presets: Today, Yesterday, Last Month
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Today",
                        color = EmeraldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { selectedDateMillis = System.currentTimeMillis() }
                            .padding(4.dp)
                    )
                    Text(
                        text = "Yesterday",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                val c = Calendar.getInstance()
                                c.add(Calendar.DAY_OF_YEAR, -1)
                                selectedDateMillis = c.timeInMillis
                            }
                            .padding(4.dp)
                    )
                    Text(
                        text = "Last Month",
                        color = SoftBlueLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                val c = Calendar.getInstance()
                                c.add(Calendar.MONTH, -1)
                                selectedDateMillis = c.timeInMillis
                            }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recurring / Subscription Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Recurring",
                        tint = if (isRecurring) EmeraldLight else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Recurring Subscription",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isRecurring) "Renews $recurringFrequency" else "One-off expense",
                            color = TextSubtle,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = EmeraldPrimary,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkElevatedSurface
                    ),
                    modifier = Modifier.testTag("recurring_toggle")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    onSave(
                        amount,
                        defaultCurrency,
                        selectedCategory,
                        selectedDateMillis,
                        notesText,
                        isRecurring,
                        if (isRecurring) recurringFrequency else null
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_expense_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Save Expense",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
