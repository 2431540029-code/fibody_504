package com.example.fitbody.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.model.Comment
import java.io.File

class CommentAdapter(private var list: List<Comment>) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgCommentAvatar)
        val name: TextView = view.findViewById(R.id.txtCommentUser)
        val text: TextView = view.findViewById(R.id.txtCommentText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(h: ViewHolder, pos: Int) {
        val c = list[pos]
        h.name.text = c.username
        h.text.text = c.text
        
        if (!c.userAvatar.isNullOrEmpty()) {
            val resId = h.itemView.context.resources.getIdentifier(c.userAvatar, "drawable", h.itemView.context.packageName)
            if (resId != 0) h.img.setImageResource(resId)
            else Glide.with(h.itemView.context).load(File(c.userAvatar)).into(h.img)
        } else {
            h.img.setImageResource(R.drawable.male)
        }
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<Comment>) {
        list = newList
        notifyDataSetChanged()
    }
}
