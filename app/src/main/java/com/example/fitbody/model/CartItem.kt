package com.example.fitbody.model

data class CartItem(
    val id: Int,
    val productId: Int,
    val name: String,
    val price: Int,
    val image: String,
    var quantity: Int,
    var isSelected: Boolean = true
)
