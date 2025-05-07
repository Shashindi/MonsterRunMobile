package com.example.budgetbuddy.data.models

import java.io.Serializable

data class CategoryModel(
    val type: Category,
    val icon: String, // You can use resource ID or icon name
    val color: String, // You can use color resource ID or hex color
    val budgetLimit: Float = 0f
) : Serializable {
    val name: String
        get() = type.displayName

    enum class Category(val displayName: String) {
        // Expense Categories
        FOOD("Food & Dining"),
        TRANSPORTATION("Transportation"),
        HOUSING("Housing"),
        UTILITIES("Utilities"),
        EDUCATION("Education"),
        ENTERTAINMENT("Entertainment"),
        HEALTH("Health & Medical"),
        SHOPPING("Shopping"),
        TRAVEL("Travel"),
        PERSONAL_CARE("Personal Care"),
        
        // Income Categories
        SALARY("Salary"),
        BUSINESS("Business"),
        INVESTMENTS("Investments"),
        GIFTS("Gifts"),
        FREELANCE("Freelance"),
        RENTAL_INCOME("Rental Income"),
        
        OTHER("Other");

        companion object {
            fun getDefaultCategories(): List<Category> {
                return values().toList()
            }
        }
    }
} 