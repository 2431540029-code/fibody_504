package com.example.fitbody.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.model.User
import java.io.File

class StudentAdapter(private val list: List<User>) : RecyclerView.Adapter<StudentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAvatar: ImageView = view.findViewById(R.id.imgStudentAvatar)
        val txtName: TextView = view.findViewById(R.id.txtStudentName)
        val txtEmail: TextView = view.findViewById(R.id.txtStudentEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = list[position]
        holder.txtName.text = student.username
        holder.txtEmail.text = student.email

        if (!student.avatar.isNullOrEmpty()) {
            val resId = holder.itemView.context.resources.getIdentifier(student.avatar, "drawable", holder.itemView.context.packageName)
            if (resId != 0) {
                holder.imgAvatar.setImageResource(resId)
            } else {
                Glide.with(holder.itemView.context).load(File(student.avatar)).into(holder.imgAvatar)
            }
        } else {
            holder.imgAvatar.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    override fun getItemCount(): Int = list.size
}