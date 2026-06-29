package com.example.fitbody.model

data class Product(
    val id: Int,
    val name: String,
    val price: Int,
    val originalPrice: Int,
    val image: String,
    val description: String,
    val category: String,
    val isAvailable: Boolean = true,
    val hasGift: Boolean = false
)