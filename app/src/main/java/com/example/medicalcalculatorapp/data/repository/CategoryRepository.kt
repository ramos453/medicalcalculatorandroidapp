package com.example.medicalcalculatorapp.data.repository

import com.example.medicalcalculatorapp.data.db.MedicalCalculatorDatabase
import com.example.medicalcalculatorapp.data.db.mapper.CategoryMapper
import com.example.medicalcalculatorapp.domain.model.Category
import com.example.medicalcalculatorapp.domain.repository.ICategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.medicalcalculatorapp.domain.model.CategoryWithCount
import com.example.medicalcalculatorapp.domain.model.CategoryStatistics
import kotlinx.coroutines.flow.first
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

    // Add these methods to existing CategoryRepository class
    override fun getAllCategoriesWithCounts(): Flow<List<CategoryWithCount>> {
        return database.categoryDao().getAllCategoriesWithCalculatorCounts()
            .map { entities ->
                entities.map { entity ->
                    CategoryWithCount(
                        category = Category(
                            id = entity.id,
                            name = entity.name,
                            description = entity.description,
                            iconResId = entity.iconResId
                        ),
                        calculatorCount = entity.calculator_count
                    )
                }
            }
    }

    override suspend fun getCategoryWithCalculatorCount(categoryId: String): CategoryWithCount? {
        val userId = "default_user" // You might want to inject UserManager here
        val entity = database.categoryDao().getCategoryWithCounts(categoryId, userId)
        return entity?.let {
            CategoryWithCount(
                category = Category(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    iconResId = it.iconResId
                ),
                calculatorCount = it.calculator_count
            )
        }
    }

    override suspend fun getCategoryStatistics(): CategoryStatistics {
        val categoriesWithCounts = getAllCategoriesWithCounts().first()
        return CategoryStatistics(
            totalCategories = categoriesWithCounts.size,
            totalCalculators = categoriesWithCounts.sumOf { it.calculatorCount },
            mostUsedCategoryId = categoriesWithCounts.maxByOrNull { it.calculatorCount }?.category?.id,
            categoriesWithCounts = categoriesWithCounts
        )
    }

}