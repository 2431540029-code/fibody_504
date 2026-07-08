package com.example.fitbody.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Int,
    @SerializedName("original_price") val originalPrice: Int,
    @SerializedName("image") val image: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("is_available") val isAvailable: Boolean = true,
    @SerializedName("has_gift") val hasGift: Boolean = false,
    @SerializedName("stock_quantity") val stockQuantity: Int = 0,
    @SerializedName("sold_quantity") val soldQuantity: Int = 0
)
