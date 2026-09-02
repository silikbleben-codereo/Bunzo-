package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Categories : Screen("categories")
    object Cart : Screen("cart")
    object Favorites : Screen("favorites")
    object Profile : Screen("profile")
    object Branches : Screen("branches")
    object Login : Screen("login")
    object Register : Screen("register")
    object OrderHistory : Screen("order_history")
    object KitchenPanel : Screen("kitchen_panel")
    object AdminPanel : Screen("admin_panel")
    object DeliveryPanel : Screen("delivery_panel")

    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }

    object CategoryProducts : Screen("category_products/{categoryId}") {
        fun createRoute(categoryId: String) = "category_products/$categoryId"
    }

    object Checkout : Screen("checkout")
    object AuthRequired : Screen("auth_required")

    object OrderSuccess : Screen("order_success/{orderId}") {
        fun createRoute(orderId: String) = "order_success/$orderId"
    }

    object OrderTracking : Screen("order_tracking/{orderId}") {
        fun createRoute(orderId: String) = "order_tracking/$orderId"
    }

    object OtpVerification : Screen("otp_verification/{phone}") {
        fun createRoute(phone: String) = "otp_verification/$phone"
    }
}
