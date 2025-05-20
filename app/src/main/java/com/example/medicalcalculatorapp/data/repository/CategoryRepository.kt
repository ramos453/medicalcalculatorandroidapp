package com.example.medicalcalculatorapp.data.repository

import com.example.medicalcalculatorapp.data.db.MedicalCalculatorDatabase
import com.example.medicalcalculatorapp.data.db.mapper.CategoryMapper
import com.example.medicalcalculatorapp.domain.model.Category
import com.example.medicalcalculatorapp.domain.repository.ICategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(
    private val database: MedicalCalculatorDatabase,
    private val categoryMapper: CategoryMapper
) : ICategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return database.categoryDao().getAllCategories()
            .map { entities ->
                entities.map { categoryMapper.mapEntityToDomain(it) }
            }
    }

    override suspend fun getCategoryById(categoryId: String): Category? {
        val categoryEntity = database.categoryDao().getCategoryById(categoryId) ?: return null
        return categoryMapper.mapEntityToDomain(categoryEntity)
    }
}