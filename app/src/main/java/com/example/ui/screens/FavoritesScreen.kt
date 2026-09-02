package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ProductGridCard
import com.example.ui.viewmodel.CartViewModel
import com.example.ui.viewmodel.MainViewModel

@Composable
fun FavoritesScreen(
    mainViewModel: MainViewModel,
    cartViewModel: CartViewModel,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteProducts by mainViewModel.favoriteProducts.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()

    if (favoriteProducts.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Favorite,
            title = "قائمة المفضلة فارغة",
            subtitle = "اضغط على أيقونة القلب ❤️ عند تصفح وجبات بونزوا لحفظ أكلاتك المفضلة هنا",
            buttonText = "استكشف أشهى الوجبات",
            onButtonClick = onNavigateToMenu,
            modifier = modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("favorites_screen")
        ) {
            items(favoriteProducts.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (product in pair) {
                        val cartItem = cartItems.find { it.productId == product.id }
                        ProductGridCard(
                            product = product,
                            isFavorite = true,
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
