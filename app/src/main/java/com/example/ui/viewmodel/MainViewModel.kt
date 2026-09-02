package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BunzoApplication
import com.example.data.model.AppSetting
import com.example.data.model.BannerOffer
import com.example.data.model.Branch
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.data.repository.BunzoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: BunzoRepository = BunzoApplication.instance.repository
) : ViewModel() {

    val categories: StateFlow<List<Category>> = repository.getCategories()
    val allProducts: StateFlow<List<Product>> = repository.getAllProducts()
    val banners: StateFlow<List<BannerOffer>> = repository.getBanners()
    val branches: StateFlow<List<Branch>> = repository.getBranches()
    val settings: StateFlow<AppSetting> = repository.getSettings()

    val featuredProducts: StateFlow<List<Product>> = repository.getFeaturedProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val specialOffers: StateFlow<List<Product>> = repository.getSpecialOffers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val buyAgainProducts: StateFlow<List<Product>> = repository.getBuyAgainProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteProducts: StateFlow<List<Product>> = repository.getFavoriteProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItemCount: StateFlow<Int> = repository.getCartTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    val searchResults: StateFlow<List<Product>> = _searchResults.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _searchResults.value = repository.searchProducts(query)
    }

    fun findProductByBarcode(barcode: String): Product? {
        return repository.findProductByBarcode(barcode)
    }

    fun getProductById(productId: String): Product? {
        return repository.getProductById(productId)
    }

    fun getCategoryById(categoryId: String): Category? {
        return repository.getCategoryById(categoryId)
    }

    fun getBranchById(branchId: String): Branch? {
        return repository.getBranchById(branchId)
    }

    fun toggleFavorite(productId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(productId)
        }
    }
}
