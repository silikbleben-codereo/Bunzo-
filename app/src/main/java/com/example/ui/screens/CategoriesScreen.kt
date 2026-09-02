package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProductGridCard
import com.example.ui.theme.BunzoAccent
import com.example.ui.theme.BunzoPrimary
import com.example.ui.viewmodel.CartViewModel
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CategoriesScreen(
    initialCategoryId: String = "all",
    mainViewModel: MainViewModel,
    cartViewModel: CartViewModel,
    onNavigateToProduct: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by mainViewModel.categories.collectAsState()
    val allProducts by mainViewModel.allProducts.collectAsState()
    val favoriteProducts by mainViewModel.favoriteProducts.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()

    var selectedCategoryId by remember { mutableStateOf(initialCategoryId) }

    val filteredProducts = if (selectedCategoryId == "all") {
        allProducts.filter { it.isAvailable }
    } else {
        allProducts.filter { it.categoryId == selectedCategoryId && it.isAvailable }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("categories_screen")
    ) {
        // Horizontal Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryId == "all",
                    onClick = { selectedCategoryId = "all" },
                    label = { Text("الكل (${allProducts.count { it.isAvailable }})") },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BunzoPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_all")
                )
            }

            items(categories) { cat ->
                val count = allProducts.count { it.categoryId == cat.id && it.isAvailable }
                FilterChip(
                    selected = selectedCategoryId == cat.id,
                    onClick = { selectedCategoryId = cat.id },
                    label = { Text("${cat.nameAr} ($count)") },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BunzoPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_${cat.id}")
                )
            }
        }

        if (filteredProducts.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.RestaurantMenu,
                title = "لا توجد وجبات في هذا القسم حالياً",
                subtitle = "جرب اختيار قسم آخر للاستمتاع بأشهى وجبات بونزوا",
                buttonText = "عرض كل الأقسام",
                onButtonClick = { selectedCategoryId = "all" }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (product in pair) {
                            val isFav = favoriteProducts.any { it.id == product.id }
                            val cartItem = cartItems.find { it.productId == product.id }
                            ProductGridCard(
                                product = product,
                                isFavorite = isFav,
                                onProductClick = { onNavigateToProduct(product.id) },
                                onAddToCartClick = { cartViewModel.addToCart(product) },
                                onFavoriteClick = { mainViewModel.toggleFavorite(product.id) },
                                currentQuantity = cartItem?.quantity ?: 0,
                                onIncrementQuantity = { cartItem?.let { cartViewModel.incrementQuantity(it) } ?: cartViewModel.addToCart(product) },
                                onDecrementQuantity = { cartItem?.let { cartViewModel.decrementQuantity(it) } },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
