package com.example.medicalcalculatorapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.medicalcalculatorapp.data.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import com.example.medicalcalculatorapp.data.db.entity.CategoryWithCalculatorCount


@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY displayOrder")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: String)

    // Add these methods to existing CategoryDao interface
    @Query("""
    SELECT c.*, COUNT(calc.id) as calculator_count 
    FROM categories c 
    LEFT JOIN calculators calc ON c.id = calc.categoryId 
    GROUP BY c.id 
    ORDER BY c.displayOrder
""")
    fun getAllCategoriesWithCalculatorCounts(): Flow<List<CategoryWithCalculatorCount>>

    @Query("""
    SELECT c.*, COUNT(calc.id) as calculator_count,
    COUNT(CASE WHEN f.calculatorId IS NOT NULL THEN 1 END) as favorite_count
    FROM categories c 
    LEFT JOIN calculators calc ON c.id = calc.categoryId 
    LEFT JOIN favorites f ON calc.id = f.calculatorId AND f.userId = :userId
    WHERE c.id = :categoryId
    GROUP BY c.id
""")
    suspend fun getCategoryWithCounts(categoryId: String, userId: String): CategoryWithCalculatorCount?

}