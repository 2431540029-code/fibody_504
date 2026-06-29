package com.example.fitbody.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.model.Review

class ReviewAdapter(private val list: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUser: TextView = view.findViewById(R.id.txtReviewUser)
        val txtRating: TextView = view.findViewById(R.id.txtReviewRating)
        val txtComment: TextView = view.findViewById(R.id.txtReviewComment)
        val txtDate: TextView = view.findViewById(R.id.txtReviewDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = list[position]
        holder.txtUser.text = review.username
        holder.txtRating.text = "⭐".repeat(review.rating)
        holder.txtComment.text = review.comment
        holder.txtDate.text = review.date
    }

    override fun getItemCount(): Int = list.size
}