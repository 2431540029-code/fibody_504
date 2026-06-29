package com.example.fitbody.model

data class CartItem(
    val id: Int,
    val product_id: Int,
    val name: String,
    val price: Int,
    val image: String,
    val quantity: Int,
    var isSelected: Boolean = true
)
