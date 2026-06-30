package com.example.fitbody.model

data class CartItem(
    val id: Int,
    val product_id: Int,
    val name: String,
    val price: Int,
    val image: String,
    var quantity: Int, // Change to var to allow update
    var isSelected: Boolean = true
)
