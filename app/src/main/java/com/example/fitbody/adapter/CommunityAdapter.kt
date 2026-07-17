package com.example.fitbody.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.model.Post

class CommunityAdapter(
    private var posts: List<Post>,
    private val onLikeClick: (Post) -> Unit,
    private val onCommentClick: (Post) -> Unit
) : RecyclerView.Adapter<CommunityAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAvatar: ImageView = view.findViewById(R.id.imgAvatar)
        val txtUsername: TextView = view.findViewById(R.id.txtUsername)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val txtContent: TextView = view.findViewById(R.id.txtContent)
        val imgPost: ImageView = view.findViewById(R.id.imgPost)
        val btnLike: View = view.findViewById(R.id.btnLike)
        val icLike: ImageView = view.findViewById(R.id.icLike)
        val txtLikeCount: TextView = view.findViewById(R.id.txtLikeCount)
        val btnComment: View = view.findViewById(R.id.btnComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.txtUsername.text = post.username
        holder.txtDate.text = post.postDate
        holder.txtContent.text = post.content
        
        if (post.likeCount > 0) {
            holder.txtLikeCount.text = "${post.likeCount} Thích"
        } else {
            holder.txtLikeCount.text = "Thích"
        }

        if (post.isLiked) {
            holder.icLike.setImageResource(android.R.drawable.btn_star_big_on)
            holder.icLike.setColorFilter(android.graphics.Color.parseColor("#1877F2"))
            holder.txtLikeCount.setTextColor(android.graphics.Color.parseColor("#1877F2"))
        } else {
            holder.icLike.setImageResource(android.R.drawable.btn_star_big_off)
            holder.icLike.setColorFilter(android.graphics.Color.parseColor("#B0B3B8"))
            holder.txtLikeCount.setTextColor(android.graphics.Color.parseColor("#B0B3B8"))
        }

        if (!post.userAvatar.isNullOrEmpty()) {
            val resId = holder.itemView.context.resources.getIdentifier(post.userAvatar, "drawable", holder.itemView.context.packageName)
            if (resId != 0) {
                Glide.with(holder.itemView.context).load(resId).into(holder.imgAvatar)
            } else {
                Glide.with(holder.itemView.context).load(java.io.File(post.userAvatar)).into(holder.imgAvatar)
            }
        } else {
            holder.imgAvatar.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        if (!post.image.isNullOrEmpty()) {
            holder.imgPost.visibility = View.VISIBLE
            val resId = holder.itemView.context.resources.getIdentifier(post.image, "drawable", holder.itemView.context.packageName)
            Glide.with(holder.itemView.context).load(if (resId != 0) resId else post.image).into(holder.imgPost)
        } else {
            holder.imgPost.visibility = View.GONE
        }

        holder.btnLike.setOnClickListener { onLikeClick(post) }
        holder.btnComment.setOnClickListener { onCommentClick(post) }
    }

    override fun getItemCount() = posts.size

    fun updateData(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
