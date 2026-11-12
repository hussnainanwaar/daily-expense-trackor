package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ParsedExpense
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkElevatedSurface
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.SoftBlueLight
import com.example.ui.theme.SoftBlueSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    parsedPreview: ParsedExpense?,
    onSubmit: () -> Unit,
    onOpenFullForm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Voice Speech-To-Text Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val spokenMatches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = spokenMatches?.firstOrNull()
            if (!recognizedText.isNullOrBlank()) {
                onInputChange(recognizedText)
                Toast.makeText(context, "Voice recognized: \"$recognizedText\"", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun startVoiceRecognition() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say expense, e.g. 'Spent $15 on lunch'")
            }
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice input not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCardSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .padding(12.dp)
            .testTag("quick_input_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Mic Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(EmeraldDark.copy(alpha = 0.5f), DarkElevatedSurface))
                    )
                    .clickable { startVoiceRecognition() }
                    .testTag("voice_input_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice dictation input",
                    tint = EmeraldLight,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Natural Language Text Field
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                placeholder = {
                    Text(
                        "e.g. Spent $14 on coffee today...",
                        color = TextSubtle,
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier
                    .weight(1f)
                    .testTag("natural_language_text_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    cursorColor = EmeraldLight
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Quick Add / Submit Button
            val canSubmit = parsedPreview?.amount != null && parsedPreview.amount > 0
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (canSubmit) {
                            Brush.linearGradient(listOf(EmeraldLight, EmeraldPrimary))
                        } else {
                            Brush.linearGradient(listOf(DarkElevatedSurface, DarkElevatedSurface))
                        }
                    )
                    .clickable(enabled = canSubmit) { onSubmit() }
                    .testTag("quick_add_submit_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (canSubmit) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = "Add expense",
                    tint = if (canSubmit) Color.Black else TextSubtle,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Open Full Manual Form Button
            IconButton(
                onClick = onOpenFullForm,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("open_manual_form_button")
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInFull,
                    contentDescription = "Open full form",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Live Parsed Tokens Preview Chips
        AnimatedVisibility(
            visible = parsedPreview != null && parsedPreview.amount != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            parsedPreview?.let { parsed ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = "Auto-Parsed Breakdown:",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Amount Chip
                        parsed.amount?.let { amt ->
                            ParsedChip(
                                label = "${parsed.currency}${String.format(Locale.US, "%.2f", amt)}",
                                color = EmeraldLight
                            )
                        }

                        // Category Chip
                        ParsedChip(
                            label = parsed.category.displayName,
                            color = parsed.category.color
                        )

                        // Notes Chip
                        if (parsed.notes.isNotBlank() && parsed.notes != parsed.category.displayName) {
                            ParsedChip(
                                label = parsed.notes,
                                color = SoftBlueLight
                            )
                        }

                        // Recurring Chip
                        if (parsed.isRecurring) {
                            ParsedChip(
                                label = "Recurring Sub",
                                color = InfoCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParsedChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
