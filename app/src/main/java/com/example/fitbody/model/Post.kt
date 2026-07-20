package com.example.fitbody.model

data class Post(
    val id: String = "",
    val userId: Int = 0,
    val username: String = "",
    val userAvatar: String? = null,
    val content: String = "",
    val image: String? = null,
    val postDate: String = "",
    var likeCount: Int = 0,
    var isLiked: Boolean = false,
    val likedBy: List<Int> = emptyList() // Lưu danh sách ID người dùng đã like
)

data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: Int = 0,
    val username: String = "",
    val userAvatar: String? = null,
    val text: String = "",
    val date: String = ""
)
