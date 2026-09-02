package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: String,
    val productNameAr: String,
    val productNameEn: String,
    val productImageUrl: String,
    val basePrice: Double,
    val selectedSizeId: String? = null,
    val selectedSizeNameAr: String? = null,
    val selectedSizePriceDelta: Double = 0.0,
    val selectedExtrasJson: String = "", // comma-separated or formatted extra names
    val extrasPriceTotal: Double = 0.0,
    val quantity: Int = 1,
    val notes: String = ""
) {
    val unitPrice: Double
        get() = basePrice + selectedSizePriceDelta + extrasPriceTotal

    val totalPrice: Double
        get() = unitPrice * quantity
}
