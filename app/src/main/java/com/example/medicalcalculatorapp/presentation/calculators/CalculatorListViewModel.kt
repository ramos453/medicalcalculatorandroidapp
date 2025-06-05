package com.example.medicalcalculatorapp.presentation.calculators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medicalcalculatorapp.data.user.UserManager
import com.example.medicalcalculatorapp.domain.model.MedicalCalculator
import com.example.medicalcalculatorapp.domain.repository.ICalculatorRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.medicalcalculatorapp.domain.model.CategoryWithCount
import com.example.medicalcalculatorapp.domain.repository.ICategoryRepository

@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorListViewModel(
    private val calculatorRepository: ICalculatorRepository,
    private val userManager: UserManager,
    private val categoryRepository: ICategoryRepository // Add this line
) : ViewModel() {

    // UI state
    private val _calculators = MutableStateFlow<List<MedicalCalculator>>(emptyList())
    val calculators: StateFlow<List<MedicalCalculator>> = _calculators

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Current filter mode
    private val _currentFilterMode = MutableStateFlow(FilterMode.ALL)
    val currentFilterMode: StateFlow<FilterMode> = _currentFilterMode

    // Current search query
    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery: StateFlow<String?> = _searchQuery

    // Categories state
    private val _categories = MutableStateFlow<List<CategoryWithCount>>(emptyList())
    val categories: StateFlow<List<CategoryWithCount>> = _categories

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId

    init {
        loadCalculators()
        loadCategories()
    }

    fun setFilterMode(mode: FilterMode) {
        //if (_currentFilterMode.value != mode) {
            _currentFilterMode.value = mode
            loadCalculators()
        //}
    }

    fun searchCalculators(query: String?) {
        _searchQuery.value = query
        loadCalculators()
    }

    fun toggleFavorite(calculatorId: String) {
        viewModelScope.launch {
            try {
                val userId = userManager.getCurrentUserId()
                calculatorRepository.toggleFavorite(calculatorId, userId)
                // The list will update automatically due to Flow collection
            } catch (e: Exception) {
                _error.value = "Failed to update favorite status: ${e.message}"
            }
        }
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
        loadCalculators()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                println("🔍 DEBUG: Starting to load categories...")
                categoryRepository.getAllCategoriesWithCounts().collectLatest { categories ->
                    println("🔍 DEBUG: Loaded ${categories.size} categories: ${categories.map { it.category.name }}")
                    _categories.value = categories
                }
            } catch (e: Exception) {
                println("❌ DEBUG: Failed to load categories: ${e.message}")
                e.printStackTrace()
                _error.value = "Failed to load categories: ${e.message}"
            }
        }
    }

//    private fun loadCategories() {
//        viewModelScope.launch {
//            try {
//                categoryRepository.getAllCategoriesWithCounts().collectLatest { categories ->
//                    _categories.value = categories
//                }
//            } catch (e: Exception) {
//                _error.value = "Failed to load categories: ${e.message}"
//            }
//        }
//    }

//    private fun loadCalculators() {
//        viewModelScope.launch {
//            _isLoading.value = true
//            _error.value = null
//
//            try {
//                // Handle search query first
//                val query = _searchQuery.value
//                if (!query.isNullOrBlank()) {
//                    calculatorRepository.searchCalculators(query).collectLatest {
//                        _calculators.value = it
//                        _isLoading.value = false
//                    }
//                    return@launch
//                }
//
//                // Then handle filter mode
//                when (_currentFilterMode.value) {
//                    FilterMode.ALL -> {
//                        calculatorRepository.getAllCalculators().collectLatest {
//                            _calculators.value = it
//                            _isLoading.value = false
//                        }
//                    }
//                    FilterMode.FAVORITES -> {
//                        val userId = userManager.getCurrentUserId()
//                        calculatorRepository.getFavoriteCalculators(userId).collectLatest {
//                            _calculators.value = it
//                            _isLoading.value = false
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                _error.value = "Failed to load calculators: ${e.message}"
//                _isLoading.value = false
//            }
//        }
//    }

    private fun loadCalculators() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val selectedCategory = _selectedCategoryId.value
                val query = _searchQuery.value

                // Handle search query first
                if (!query.isNullOrBlank()) {
                    calculatorRepository.searchCalculators(query).collectLatest { allCalculators ->
                        val filteredCalculators = if (selectedCategory != null) {
                            allCalculators.filter { it.category == selectedCategory }
                        } else {
                            allCalculators
                        }
                        _calculators.value = filteredCalculators
                        _isLoading.value = false
                    }
                    return@launch
                }

                // Handle category + filter mode combination
                when (_currentFilterMode.value) {
                    FilterMode.ALL -> {
                        if (selectedCategory != null) {
                            calculatorRepository.getCalculatorsByCategory(selectedCategory).collectLatest {
                                _calculators.value = it
                                _isLoading.value = false
                            }
                        } else {
                            calculatorRepository.getAllCalculators().collectLatest {
                                _calculators.value = it
                                _isLoading.value = false
                            }
                        }
                    }
                    FilterMode.FAVORITES -> {
                        val userId = userManager.getCurrentUserId()
                        calculatorRepository.getFavoriteCalculators(userId).collectLatest { favorites ->
                            val filteredFavorites = if (selectedCategory != null) {
                                favorites.filter { it.category == selectedCategory }
                            } else {
                                favorites
                            }
                            _calculators.value = filteredFavorites
                            _isLoading.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to load calculators: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    enum class FilterMode {
        ALL, FAVORITES
    }

    /**
     * Factory for creating CalculatorListViewModel with dependencies
     */
    class Factory(
        private val calculatorRepository: ICalculatorRepository,
        private val userManager: UserManager,
        private val categoryRepository: ICategoryRepository // Add this parameter
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalculatorListViewModel::class.java)) {
                return CalculatorListViewModel(calculatorRepository, userManager, categoryRepository) as T // Add categoryRepository
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}