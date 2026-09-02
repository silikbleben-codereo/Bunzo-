package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.BunzoAccent
import com.example.ui.theme.BunzoDark
import com.example.ui.theme.BunzoPrimary
import com.example.ui.theme.BunzoSecondaryYellow
import com.example.utils.SoundHelper

sealed class BottomNavItem(
    val route: String,
    val titleAr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    object Home : BottomNavItem(
        route = Screen.Home.route,
        titleAr = "الرئيسية",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_home"
    )

    object Categories : BottomNavItem(
        route = Screen.Categories.route,
        titleAr = "القائمة",
        selectedIcon = Icons.Filled.Category,
        unselectedIcon = Icons.Outlined.Category,
        testTag = "nav_categories"
    )

    object Cart : BottomNavItem(
        route = Screen.Cart.route,
        titleAr = "السلة",
        selectedIcon = Icons.Filled.ShoppingBag,
        unselectedIcon = Icons.Outlined.ShoppingBag,
        testTag = "nav_cart"
    )

    object Favorites : BottomNavItem(
        route = Screen.Favorites.route,
        titleAr = "المفضلة",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder,
        testTag = "nav_favorites"
    )

    object Profile : BottomNavItem(
        route = Screen.Profile.route,
        titleAr = "حسابي",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "nav_profile"
    )
}

@Composable
fun BunzoBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    cartItemCount: Int = 0,
    modifier: Modifier = Modifier
) {
    BunzoBottomNavigation(
        currentRoute = currentRoute,
        cartItemCount = cartItemCount,
        onNavigate = onNavigate,
        modifier = modifier
    )
}

@Composable
fun BunzoBottomNavigation(
    currentRoute: String?,
    cartCount: Int = 0,
    cartItemCount: Int = 0,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val count = if (cartItemCount > 0) cartItemCount else cartCount
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Categories,
        BottomNavItem.Cart,
        BottomNavItem.Favorites,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = BunzoDark,
        tonalElevation = 8.dp,
        modifier = modifier
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    SoundHelper.playClickSound()
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                },
                icon = {
                    if (item == BottomNavItem.Cart && count > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = BunzoPrimary,
                                    contentColor = BunzoDark
                                ) {
                                    Text(
                                        text = "$count",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.titleAr
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.titleAr
                        )
                    }
                },
                label = {
                    Text(
                        text = item.titleAr,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BunzoDark,
                    selectedTextColor = BunzoDark,
                    indicatorColor = BunzoPrimary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
