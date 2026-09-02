package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.BunzoBottomNav
import com.example.ui.components.BunzoTopBar
import com.example.ui.components.TopToastNotification
import com.example.ui.navigation.Screen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthRequiredScreen
import com.example.ui.screens.BranchesScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.DeliveryPanelScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KitchenPanelScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.OrderHistoryScreen
import com.example.ui.screens.OrderSuccessScreen
import com.example.ui.screens.OrderTrackingScreen
import com.example.ui.screens.OtpVerificationScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.theme.BunzoTheme
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.CartViewModel
import com.example.ui.viewmodel.KitchenViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.OrderViewModel
import com.example.data.model.UserRole

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BunzoTheme {
                // RTL Support for Arabic Experience
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val navController = rememberNavController()

                    // Instantiate ViewModels
                    val authViewModel: AuthViewModel = viewModel()
                    val mainViewModel: MainViewModel = viewModel()
                    val cartViewModel: CartViewModel = viewModel()
                    val orderViewModel: OrderViewModel = viewModel()
                    val kitchenViewModel: KitchenViewModel = viewModel()
                    val adminViewModel: AdminViewModel = viewModel()

                    BunzoAppRoot(
                        navController = navController,
                        authViewModel = authViewModel,
                        mainViewModel = mainViewModel,
                        cartViewModel = cartViewModel,
                        orderViewModel = orderViewModel,
                        kitchenViewModel = kitchenViewModel,
                        adminViewModel = adminViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun BunzoAppRoot(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    kitchenViewModel: KitchenViewModel,
    adminViewModel: AdminViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val cartItemCount by cartViewModel.totalItemCount.collectAsState()
    val toastMessage by cartViewModel.toastMessage.collectAsState()

    // Determine whether to show TopBar and BottomNav
    val showBottomNav = currentRoute in listOf(
        Screen.Home.route,
        Screen.Categories.route,
        Screen.Cart.route,
        Screen.Favorites.route,
        Screen.Profile.route
    )

    val showTopBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Categories.route,
        Screen.Cart.route,
        Screen.Favorites.route,
        Screen.Profile.route,
        Screen.Branches.route,
        Screen.OrderHistory.route
    )

    val topBarTitle = when (currentRoute) {
        Screen.Home.route -> "مطعم بونزوا 🍔"
        Screen.Categories.route -> "أقسام المنيو"
        Screen.Cart.route -> "سلة الوجبات"
        Screen.Favorites.route -> "المفضلة ❤️"
        Screen.Profile.route -> "حسابي والخدمات"
        Screen.Branches.route -> "فروع بونزوا في سوريا"
        Screen.OrderHistory.route -> "سجل طلباتي السابقة"
        else -> "بونزوا"
    }

    val canNavigateBack = currentRoute !in listOf(
        Screen.Home.route,
        Screen.Categories.route,
        Screen.Cart.route,
        Screen.Favorites.route,
        Screen.Profile.route
    )

    Scaffold(
        topBar = {
            if (showTopBar) {
                BunzoTopBar(
                    title = topBarTitle,
                    canNavigateBack = canNavigateBack,
                    onNavigateBack = { navController.popBackStack() },
                    cartItemCount = cartItemCount,
                    onCartClick = { navController.navigate(Screen.Cart.route) }
                )
            }
        },
        bottomBar = {
            if (showBottomNav) {
                BunzoBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    cartItemCount = cartItemCount
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Navigation Host
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Home
                composable(Screen.Home.route) {
                    HomeScreen(
                        mainViewModel = mainViewModel,
                        cartViewModel = cartViewModel,
                        onNavigateToProduct = { productId ->
                            navController.navigate(Screen.ProductDetail.createRoute(productId))
                        },
                        onNavigateToCategory = { catId ->
                            navController.navigate(Screen.CategoryProducts.createRoute(catId))
                        },
                        onNavigateToBranches = {
                            navController.navigate(Screen.Branches.route)
                        },
                        onNavigateToCart = {
                            navController.navigate(Screen.Cart.route)
                        }
                    )
                }

                // 2. Categories
                composable(Screen.Categories.route) {
                    CategoriesScreen(
                        initialCategoryId = "all",
                        mainViewModel = mainViewModel,
                        cartViewModel = cartViewModel,
                        onNavigateToProduct = { productId ->
                            navController.navigate(Screen.ProductDetail.createRoute(productId))
                        }
                    )
                }

                // 2b. Category with specific ID
                composable(
                    route = Screen.CategoryProducts.route,
                    arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val categoryId = backStackEntry.arguments?.getString("categoryId") ?: "all"
                    CategoriesScreen(
                        initialCategoryId = categoryId,
                        mainViewModel = mainViewModel,
                        cartViewModel = cartViewModel,
                        onNavigateToProduct = { productId ->
                            navController.navigate(Screen.ProductDetail.createRoute(productId))
                        }
                    )
                }

                // 3. Product Detail
                composable(
                    route = Screen.ProductDetail.route,
                    arguments = listOf(navArgument("productId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId") ?: ""
                    ProductDetailScreen(
                        productId = productId,
                        mainViewModel = mainViewModel,
                        cartViewModel = cartViewModel,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToCart = { navController.navigate(Screen.Cart.route) }
                    )
                }

                // 4. Cart
                composable(Screen.Cart.route) {
                    CartScreen(
                        cartViewModel = cartViewModel,
                        onNavigateToCheckout = {
                            if (authViewModel.currentUser.value != null) {
                                navController.navigate(Screen.Checkout.route)
                            } else {
                                navController.navigate(Screen.AuthRequired.route)
                            }
                        },
                        onNavigateToMenu = { navController.navigate(Screen.Categories.route) }
                    )
                }

                // 4b. Auth Required Prompt
                composable(Screen.AuthRequired.route) {
                    AuthRequiredScreen(
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onNavigateToMenu = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }

                // 5. Checkout
                composable(Screen.Checkout.route) {
                    CheckoutScreen(
                        mainViewModel = mainViewModel,
                        cartViewModel = cartViewModel,
                        orderViewModel = orderViewModel,
                        authViewModel = authViewModel,
                        onOrderPlaced = { placedOrderId ->
                            cartViewModel.clearCart()
                            navController.navigate(Screen.OrderSuccess.createRoute(placedOrderId)) {
                                popUpTo(Screen.Home.route)
                            }
                        },
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onNavigateToMenu = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }

                // 6. Order Success
                composable(
                    route = Screen.OrderSuccess.route,
                    arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                    OrderSuccessScreen(
                        orderId = orderId,
                        orderViewModel = orderViewModel,
                        onTrackOrder = { id ->
                            navController.navigate(Screen.OrderTracking.createRoute(id))
                        },
                        onBackToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }

                // 7. Order Tracking
                composable(
                    route = Screen.OrderTracking.route,
                    arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                    OrderTrackingScreen(
                        orderId = orderId,
                        orderViewModel = orderViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                // 8. Order History
                composable(Screen.OrderHistory.route) {
                    OrderHistoryScreen(
                        orderViewModel = orderViewModel,
                        mainViewModel = mainViewModel,
                        onNavigateToTracking = { id ->
                            navController.navigate(Screen.OrderTracking.createRoute(id))
                        },
                        onNavigateToMenu = {
                            navController.navigate(Screen.Categories.route)
                        }
                    )
                }

                // 9. Favorites
                composable(Screen.Favorites.route) {
                    FavoritesScreen(
                        mainViewModel = mainViewModel,
                        cartViewModel = cartViewModel,
                        onNavigateToProduct = { productId ->
                            navController.navigate(Screen.ProductDetail.createRoute(productId))
                        },
                        onNavigateToMenu = {
                            navController.navigate(Screen.Categories.route)
                        }
                    )
                }

                // 10. Profile
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        authViewModel = authViewModel,
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onNavigateToOrderHistory = { navController.navigate(Screen.OrderHistory.route) },
                        onNavigateToBranches = { navController.navigate(Screen.Branches.route) },
                        onNavigateToKitchenPanel = { navController.navigate(Screen.KitchenPanel.route) },
                        onNavigateToAdminPanel = { navController.navigate(Screen.AdminPanel.route) },
                        onNavigateToDeliveryPanel = { navController.navigate(Screen.DeliveryPanel.route) }
                    )
                }

                // 11. Login
                composable(Screen.Login.route) {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = { loggedInUser ->
                            if (loggedInUser.role.isAdmin) {
                                navController.navigate(Screen.AdminPanel.route) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            } else if (loggedInUser.role.isKitchen) {
                                navController.navigate(Screen.KitchenPanel.route) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            } else if (loggedInUser.role == UserRole.DELIVERY) {
                                navController.navigate(Screen.DeliveryPanel.route) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        },
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onNavigateToOtp = { phone ->
                            navController.navigate(Screen.OtpVerification.createRoute(phone))
                        }
                    )
                }

                // 12. Register
                composable(Screen.Register.route) {
                    RegisterScreen(
                        authViewModel = authViewModel,
                        onRegisterSuccess = { phone ->
                            navController.navigate(Screen.OtpVerification.createRoute(phone))
                        },
                        onNavigateToLogin = { navController.popBackStack() }
                    )
                }

                // 13. OTP Verification
                composable(
                    route = Screen.OtpVerification.route,
                    arguments = listOf(navArgument("phone") { type = NavType.StringType })
                ) { backStackEntry ->
                    val phone = backStackEntry.arguments?.getString("phone") ?: ""
                    OtpVerificationScreen(
                        phone = phone,
                        authViewModel = authViewModel,
                        onVerificationSuccess = { verifiedUser ->
                            if (verifiedUser.role.isAdmin) {
                                navController.navigate(Screen.AdminPanel.route) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            } else if (verifiedUser.role.isKitchen) {
                                navController.navigate(Screen.KitchenPanel.route) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            } else if (verifiedUser.role == UserRole.DELIVERY) {
                                navController.navigate(Screen.DeliveryPanel.route) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }

                // 14. Branches
                composable(Screen.Branches.route) {
                    BranchesScreen(mainViewModel = mainViewModel)
                }

                // 15. Kitchen Display Panel (KDS)
                composable(Screen.KitchenPanel.route) {
                    KitchenPanelScreen(
                        kitchenViewModel = kitchenViewModel,
                        authViewModel = authViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 16. Admin Control Panel
                composable(Screen.AdminPanel.route) {
                    AdminPanelScreen(
                        adminViewModel = adminViewModel,
                        mainViewModel = mainViewModel,
                        authViewModel = authViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 17. Delivery Driver Panel
                composable(Screen.DeliveryPanel.route) {
                    DeliveryPanelScreen(
                        adminViewModel = adminViewModel,
                        authViewModel = authViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            // Top Floating Toast Notification
            TopToastNotification(
                message = toastMessage
            )
        }
    }
}
