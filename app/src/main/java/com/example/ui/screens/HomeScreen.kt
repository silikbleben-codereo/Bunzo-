package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryCircleItem
import com.example.ui.components.ProductGridCard
import com.example.ui.components.ProductHorizontalCard
import com.example.ui.components.PromoBannerCard
import com.example.ui.components.WelcomeDialog
import com.example.ui.theme.BunzoAccent
import com.example.ui.theme.BunzoDark
import com.example.ui.theme.BunzoPrimary
import com.example.ui.theme.BunzoSecondaryYellow
import com.example.ui.viewmodel.CartViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.utils.SoundHelper

@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    cartViewModel: CartViewModel,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToBranches: () -> Unit,
    onNavigateToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by mainViewModel.categories.collectAsState()
    val banners by mainViewModel.banners.collectAsState()
    val featuredProducts by mainViewModel.featuredProducts.collectAsState()
    val specialOffers by mainViewModel.specialOffers.collectAsState()
    val buyAgainProducts by mainViewModel.buyAgainProducts.collectAsState()
    val favoriteProducts by mainViewModel.favoriteProducts.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    val branches by mainViewModel.branches.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val isSearching = searchQuery.isNotBlank()
    var showWelcomeDialog by rememberSaveable { mutableStateOf(true) }

    // Welcome Dialog on launch
    WelcomeDialog(
        isOpen = showWelcomeDialog,
        onDismiss = { showWelcomeDialog = false },
        onNavigateToMenu = {
            showWelcomeDialog = false
            onNavigateToCategory("all")
        },
        onNavigateToBranches = {
            showWelcomeDialog = false
            onNavigateToBranches()
        }
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Search & Branch Bar Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BunzoDark)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Branch selector pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable {
                            SoundHelper.playClickSound()
                            onNavigateToBranches()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("branch_pill")
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = BunzoPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "التوصيل من: ${branches.firstOrNull()?.nameAr ?: "فرع دمشق المزة"}",
                        color = Color(0xFFFFFDF7),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = BunzoPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "ابحث عن برغر، شاورما، بروستد، مقبلات...",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = BunzoPrimary
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = BunzoPrimary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )
            }
        }

        if (isSearching) {
            // Live Search Results
            val results = mainViewModel.allProducts.value.filter {
                it.nameAr.contains(searchQuery, ignoreCase = true) ||
                it.nameEn.contains(searchQuery, ignoreCase = true) ||
                it.descriptionAr.contains(searchQuery, ignoreCase = true) ||
                it.categoryNameAr.contains(searchQuery, ignoreCase = true)
            }

            item {
                Text(
                    text = "نتائج البحث (${results.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            items(results) { product ->
                val cartItem = cartItems.find { it.productId == product.id }
                ProductHorizontalCard(
                    product = product,
                    onProductClick = { onNavigateToProduct(product.id) },
                    onAddToCartClick = { cartViewModel.addToCart(product) },
                    currentQuantity = cartItem?.quantity ?: 0,
                    onIncrementQuantity = { cartItem?.let { cartViewModel.incrementQuantity(it) } ?: cartViewModel.addToCart(product) },
                    onDecrementQuantity = { cartItem?.let { cartViewModel.decrementQuantity(it) } },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        } else {
            // Normal Home View

            // 1. Promotional Banners Slider
            item {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(banners) { banner ->
                        PromoBannerCard(
                            banner = banner,
                            onClick = {
                                banner.targetProductId?.let { onNavigateToProduct(it) }
                            },
                            modifier = Modifier.width(300.dp)
                        )
                    }
                }
            }

            // 2. Categories Horizontal Scroll
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "أقسام المنيو 🍔",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "عرض الكل",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BunzoPrimary,
                        modifier = Modifier.clickable { onNavigateToCategory("all") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { category ->
                        CategoryCircleItem(
                            category = category,
                            isSelected = false,
                            onClick = { onNavigateToCategory(category.id) }
                        )
                    }
                }
            }

            // 3. Special Offers Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(BunzoAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = BunzoPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "عروض التوفير الحصرية 🔥",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(specialOffers) { product ->
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
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
            }

            // 4. Buy Again / Quick Reorder (if available)
            if (buyAgainProducts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "اطلب مجدداً المفضلة لديك 🔁",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(buyAgainProducts) { product ->
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
                                modifier = Modifier.width(180.dp)
                            )
                        }
                    }
                }
            }

            // 5. Featured Gourmet Burgers & Meals Grid
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "وجبات بونزوا المميزة ⭐",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Two columns grid in list
            items(featuredProducts.chunked(2)) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
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

            // 6. Damascus & Syria Branches Banner Card
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BunzoDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable(onClick = onNavigateToBranches)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(BunzoPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = BunzoDark,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "فروع مطعم بونزوا في سوريا",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFFFFFDF7)
                            )
                            Text(
                                text = "دمشق (المزة، الشعلان، المالكي) - حلب - اللاذقية",
                                fontSize = 11.sp,
                                color = Color(0xFFFFFDF7).copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = BunzoPrimary
                        )
                    }
                }
            }
        }
    }
}
