package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteItem(
    @PrimaryKey
    val productId: String,
    val addedAtTimestamp: Long = System.currentTimeMillis()
)
