package com.example.fitbody.model

data class Post(
    val id: Int,
    val userId: Int,
    val username: String,
    val userAvatar: String?,
    val content: String,
    val image: String?,
    val postDate: String,
    var likeCount: Int,
    var isLiked: Boolean
)

data class Comment(
    val id: Int,
    val postId: Int,
    val userId: Int,
    val username: String,
    val userAvatar: String?,
    val text: String,
    val date: String
)
