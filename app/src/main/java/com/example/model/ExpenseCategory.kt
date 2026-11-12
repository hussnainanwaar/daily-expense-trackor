package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExpenseCategory(
    val displayName: String,
    val icon: ImageVector,
    val color: Color,
    val keywords: List<String>
) {
    FOOD(
        displayName = "Food & Dining",
        icon = Icons.Default.Fastfood,
        color = Color(0xFFF59E0B),
        keywords = listOf("food", "lunch", "dinner", "breakfast", "coffee", "cafe", "starbucks", "pizza", "burger", "restaurant", "bistro", "meal", "snack", "eat", "drinks", "bar", "sushi", "takeout", "mcdonalds", "subway")
    ),
    GROCERIES(
        displayName = "Groceries",
        icon = Icons.Default.LocalGroceryStore,
        color = Color(0xFF10B981),
        keywords = listOf("groceries", "grocery", "supermarket", "walmart", "trader joe", "target", "whole foods", "costco", "market", "milk", "bread", "eggs", "veggies", "fruits")
    ),
    TRANSPORT(
        displayName = "Transportation",
        icon = Icons.Default.DirectionsCar,
        color = Color(0xFF38BDF8),
        keywords = listOf("transport", "transportation", "uber", "lyft", "taxi", "cab", "gas", "petrol", "fuel", "train", "bus", "subway", "metro", "parking", "toll", "flight", "ticket", "transit")
    ),
    SHOPPING(
        displayName = "Shopping",
        icon = Icons.Default.ShoppingCart,
        color = Color(0xFFEC4899),
        keywords = listOf("shopping", "clothes", "shoes", "amazon", "mall", "gadget", "electronics", "book", "books", "store", "purchase", "outfit", "zara", "nike")
    ),
    ENTERTAINMENT(
        displayName = "Entertainment",
        icon = Icons.Default.Movie,
        color = Color(0xFFA855F7),
        keywords = listOf("entertainment", "movie", "cinema", "film", "concert", "game", "gaming", "steam", "playstation", "xbox", "theater", "show", "club", "party", "bowling")
    ),
    SUBSCRIPTIONS(
        displayName = "Subscriptions",
        icon = Icons.Default.Subscriptions,
        color = Color(0xFF6366F1),
        keywords = listOf("subscription", "netflix", "spotify", "youtube", "hulu", "disney", "prime", "apple", "icloud", "chatgpt", "membership", "gym", "recurring", "monthly")
    ),
    UTILITIES(
        displayName = "Utilities & Bills",
        icon = Icons.Default.Bolt,
        color = Color(0xFFF97316),
        keywords = listOf("utilities", "utility", "bill", "electricity", "electric", "water", "internet", "wifi", "phone", "cell", "gas bill", "power", "energy", "trash")
    ),
    HEALTH(
        displayName = "Health & Wellness",
        icon = Icons.Default.LocalHospital,
        color = Color(0xFFEF4444),
        keywords = listOf("health", "medical", "doctor", "dentist", "medicine", "pharmacy", "clinic", "hospital", "vitamins", "pills", "therapy", "dental", "optometry")
    ),
    HOUSING(
        displayName = "Housing",
        icon = Icons.Default.Home,
        color = Color(0xFF14B8A6),
        keywords = listOf("housing", "rent", "mortgage", "apartment", "house", "furniture", "repair", "maintenance", "decor", "home")
    ),
    OTHER(
        displayName = "Other",
        icon = Icons.Default.MoreHoriz,
        color = Color(0xFF94A3B8),
        keywords = listOf("other", "misc", "miscellaneous", "cash", "fee", "transfer")
    );

    companion object {
        fun fromDisplayName(name: String): ExpenseCategory {
            return entries.find { it.displayName.equals(name, ignoreCase = true) } ?: OTHER
        }

        fun inferFromText(text: String): ExpenseCategory {
            val lower = text.lowercase()
            for (category in entries) {
                if (category == OTHER) continue
                for (keyword in category.keywords) {
                    // Match whole word or contained word
                    if (lower.contains(Regex("\\b${Regex.escape(keyword)}\\b")) || lower.contains(keyword)) {
                        return category
                    }
                }
            }
            return OTHER
        }
    }
}
