package com.example.medicalcalculatorapp.domain.repository

import com.example.medicalcalculatorapp.domain.model.Category
import kotlinx.coroutines.flow.Flow
import com.example.medicalcalculatorapp.domain.model.CategoryWithCount
import com.example.medicalcalculatorapp.domain.model.CategoryStatistics

interface ICategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(categoryId: String): Category?
    // Add these methods to the existing interface
    suspend fun getCategoryWithCalculatorCount(categoryId: String): CategoryWithCount?
    fun getAllCategoriesWithCounts(): Flow<List<CategoryWithCount>>
    suspend fun getCategoryStatistics(): CategoryStatistics
}