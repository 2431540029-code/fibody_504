package com.example.fitbody.model

data class Order(
    val id: Int,
    val userId: Int,
    val totalPrice: Int,
    val orderDate: String,
    val status: String,
    val items: List<OrderItem> = emptyList()
)

data class OrderItem(
    val id: Int,
    val orderId: Int,
    val productId: Int,
    val productName: String,
    val productImage: String,
    val quantity: Int,
    val price: Int
)
