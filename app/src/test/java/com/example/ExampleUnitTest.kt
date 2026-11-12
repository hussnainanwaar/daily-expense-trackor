package com.example

import com.example.model.ExpenseCategory
import com.example.nlp.ExpenseNaturalLanguageParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testParseLunchExpense() {
        val parsed = ExpenseNaturalLanguageParser.parse("Spent $12 on lunch today")
        assertEquals(12.0, parsed.amount ?: 0.0, 0.001)
        assertEquals("$", parsed.currency)
        assertEquals(ExpenseCategory.FOOD, parsed.category)
        assertTrue(parsed.isConfident)
    }

    @Test
    fun testParseCoffeeWithDecimal() {
        val parsed = ExpenseNaturalLanguageParser.parse("Coffee $4.50 at Starbucks")
        assertEquals(4.50, parsed.amount ?: 0.0, 0.001)
        assertEquals(ExpenseCategory.FOOD, parsed.category)
    }

    @Test
    fun testParseGroceriesYesterday() {
        val parsed = ExpenseNaturalLanguageParser.parse("45 for groceries yesterday")
        assertEquals(45.0, parsed.amount ?: 0.0, 0.001)
        assertEquals(ExpenseCategory.GROCERIES, parsed.category)
    }

    @Test
    fun testParseUberTransport() {
        val parsed = ExpenseNaturalLanguageParser.parse("Uber $18.20 downtown")
        assertEquals(18.20, parsed.amount ?: 0.0, 0.001)
        assertEquals(ExpenseCategory.TRANSPORT, parsed.category)
    }

    @Test
    fun testParseNetflixSubscription() {
        val parsed = ExpenseNaturalLanguageParser.parse("Netflix $15.99 monthly")
        assertEquals(15.99, parsed.amount ?: 0.0, 0.001)
        assertEquals(ExpenseCategory.SUBSCRIPTIONS, parsed.category)
        assertTrue(parsed.isRecurring)
    }

    @Test
    fun testParseEuroCurrency() {
        val parsed = ExpenseNaturalLanguageParser.parse("Dinner 35 euros in Paris")
        assertEquals(35.0, parsed.amount ?: 0.0, 0.001)
        assertEquals("€", parsed.currency)
        assertEquals(ExpenseCategory.FOOD, parsed.category)
    }

    @Test
    fun testParseLastMonthExpense() {
        val parsed = ExpenseNaturalLanguageParser.parse("Groceries 85 dollars last month")
        assertEquals(85.0, parsed.amount ?: 0.0, 0.001)
        assertEquals(ExpenseCategory.GROCERIES, parsed.category)
        assertTrue(parsed.dateMillis < System.currentTimeMillis() - 20L * 24 * 3600 * 1000)
    }
}
