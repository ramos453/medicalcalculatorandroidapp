package com.example.medicalcalculatorapp.domain.repository

import com.example.medicalcalculatorapp.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface ICategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(categoryId: String): Category?
}