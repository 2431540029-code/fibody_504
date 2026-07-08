package com.example.fitbody.network

import com.example.fitbody.model.Product
import retrofit2.Call
import retrofit2.http.GET

interface ProductApiService {
    // Đây là đường link con (Endpoint) của Server
    @GET("v3/b/668a85f7e41b4d34e40f09a5?meta=false")
    fun getProductsFromServer(): Call<List<Product>>
}
