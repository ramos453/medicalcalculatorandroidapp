package com.example.medicalcalculatorapp.domain.model

data class CategoryWithCount(
    val category: Category,
    val calculatorCount: Int,
    val favoriteCount: Int = 0
)

data class CategoryStatistics(
    val totalCategories: Int,
    val totalCalculators: Int,
    val mostUsedCategoryId: String?,
    val categoriesWithCounts: List<CategoryWithCount>
)