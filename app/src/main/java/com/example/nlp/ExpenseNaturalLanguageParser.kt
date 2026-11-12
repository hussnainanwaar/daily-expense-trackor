package com.example.nlp

import com.example.model.ExpenseCategory
import com.example.model.ParsedExpense
import java.util.Calendar

object ExpenseNaturalLanguageParser {

    private val CURRENCY_MAP = mapOf(
        "$" to "$",
        "dollar" to "$",
        "dollars" to "$",
        "usd" to "$",
        "bucks" to "$",
        "€" to "€",
        "euro" to "€",
        "euros" to "€",
        "eur" to "€",
        "£" to "£",
        "pound" to "£",
        "pounds" to "£",
        "gbp" to "£",
        "¥" to "¥",
        "yen" to "¥",
        "jpy" to "¥",
        "cny" to "¥",
        "₹" to "₹",
        "rupee" to "₹",
        "rupees" to "₹",
        "inr" to "₹",
        "cad" to "CA$",
        "aud" to "A$"
    )

    private val RECURRING_KEYWORDS = setOf(
        "monthly", "subscription", "recurring", "recur", "per month", "/mo", "/month",
        "every month", "weekly", "yearly", "annual", "annually", "renewal", "membership"
    )

    fun parse(text: String, defaultCurrency: String = "$"): ParsedExpense {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return ParsedExpense(
                amount = null,
                currency = defaultCurrency,
                category = ExpenseCategory.OTHER,
                dateMillis = System.currentTimeMillis(),
                notes = "",
                isRecurring = false,
                rawText = text,
                isConfident = false
            )
        }

        // 1. Detect Currency
        var detectedCurrency = defaultCurrency
        for ((symbol, curr) in CURRENCY_MAP) {
            val pattern = if (symbol.length == 1) Regex.escape(symbol) else "\\b${Regex.escape(symbol)}\\b"
            if (trimmed.contains(Regex(pattern, RegexOption.IGNORE_CASE))) {
                detectedCurrency = curr
                break
            }
        }

        // 2. Extract Amount
        val amountRegex = Regex(
            "(?:[\\$€£¥₹]|CA|AUD|USD|EUR)?\\s*(\\d+(?:[.,]\\d{1,2})?)\\s*(?:[\\$€£¥₹]|dollars?|bucks|euros?|pounds?|yen|rupees?|usd|eur|gbp|jpy|inr)?",
            RegexOption.IGNORE_CASE
        )
        val matches = amountRegex.findAll(trimmed).toList()
        var extractedAmount: Double? = null
        var amountMatchStr: String? = null

        for (match in matches) {
            val numStr = match.groups[1]?.value?.replace(',', '.')
            val parsedVal = numStr?.toDoubleOrNull()
            if (parsedVal != null && parsedVal > 0) {
                extractedAmount = parsedVal
                amountMatchStr = match.value
                break
            }
        }

        // 3. Detect Category
        val detectedCategory = ExpenseCategory.inferFromText(trimmed)

        // 4. Detect Recurring / Subscription
        val isRecurring = RECURRING_KEYWORDS.any { keyword ->
            trimmed.contains(Regex("\\b${Regex.escape(keyword)}\\b", RegexOption.IGNORE_CASE))
        } || detectedCategory == ExpenseCategory.SUBSCRIPTIONS

        // 5. Detect Date
        val dateMillis = extractDate(trimmed)

        // 6. Extract Notes / Merchant
        val notes = extractCleanNotes(trimmed, amountMatchStr, detectedCategory)

        return ParsedExpense(
            amount = extractedAmount,
            currency = detectedCurrency,
            category = detectedCategory,
            dateMillis = dateMillis,
            notes = notes,
            isRecurring = isRecurring,
            rawText = trimmed,
            isConfident = extractedAmount != null
        )
    }

    private fun extractDate(text: String): Long {
        val lower = text.lowercase()
        val calendar = Calendar.getInstance()

        when {
            lower.contains(Regex("\\byesterday\\b")) || lower.contains("last night") -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }
            lower.contains(Regex("\\b2 days ago\\b")) || lower.contains(Regex("\\btwo days ago\\b")) -> {
                calendar.add(Calendar.DAY_OF_YEAR, -2)
            }
            lower.contains(Regex("\\b3 days ago\\b")) || lower.contains(Regex("\\bthree days ago\\b")) -> {
                calendar.add(Calendar.DAY_OF_YEAR, -3)
            }
            lower.contains(Regex("\\blast week\\b")) -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
            }
            lower.contains(Regex("\\blast month\\b")) -> {
                calendar.add(Calendar.MONTH, -1)
            }
        }
        return calendar.timeInMillis
    }

    private fun extractCleanNotes(
        text: String,
        amountMatchStr: String?,
        category: ExpenseCategory
    ): String {
        var clean = text

        // Remove amount match
        if (amountMatchStr != null) {
            clean = clean.replace(amountMatchStr, "", ignoreCase = true)
        }

        // Remove common filler phrases
        val fillers = listOf(
            Regex("\\bspent\\b", RegexOption.IGNORE_CASE),
            Regex("\\bspend\\b", RegexOption.IGNORE_CASE),
            Regex("\\bpaid\\b", RegexOption.IGNORE_CASE),
            Regex("\\bbought\\b", RegexOption.IGNORE_CASE),
            Regex("\\bpay\\b", RegexOption.IGNORE_CASE),
            Regex("\\bfor\\b", RegexOption.IGNORE_CASE),
            Regex("\\bon\\b", RegexOption.IGNORE_CASE),
            Regex("\\btoday\\b", RegexOption.IGNORE_CASE),
            Regex("\\byesterday\\b", RegexOption.IGNORE_CASE),
            Regex("\\blast night\\b", RegexOption.IGNORE_CASE),
            Regex("\\b2 days ago\\b", RegexOption.IGNORE_CASE),
            Regex("\\btwo days ago\\b", RegexOption.IGNORE_CASE),
            Regex("\\blast week\\b", RegexOption.IGNORE_CASE),
            Regex("\\blast month\\b", RegexOption.IGNORE_CASE),
            Regex("\\bmonthly\\b", RegexOption.IGNORE_CASE),
            Regex("\\bsubscription\\b", RegexOption.IGNORE_CASE),
            Regex("\\brecurring\\b", RegexOption.IGNORE_CASE),
            Regex("\\bdollars?\\b", RegexOption.IGNORE_CASE),
            Regex("\\beuros?\\b", RegexOption.IGNORE_CASE),
            Regex("\\bpounds?\\b", RegexOption.IGNORE_CASE),
            Regex("\\bbucks\\b", RegexOption.IGNORE_CASE),
            Regex("[$€£¥₹]")
        )

        for (pattern in fillers) {
            clean = clean.replace(pattern, " ")
        }

        // Clean up punctuation and multiple spaces
        clean = clean.replace(Regex("[,:;\\-\\+]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        // Capitalize first letter
        return if (clean.isNotBlank()) {
            clean.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } else {
            category.displayName
        }
    }
}
