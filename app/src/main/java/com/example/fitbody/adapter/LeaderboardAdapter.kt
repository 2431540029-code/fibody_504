package com.example.fitbody.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.model.LeaderboardUser

class LeaderboardAdapter(private val list: List<LeaderboardUser>) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtRank: TextView = view.findViewById(R.id.txtRank)
        val txtUserName: TextView = view.findViewById(R.id.txtUserNameRank)
        val txtCount: TextView = view.findViewById(R.id.txtWorkoutCountRank)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leaderboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = list[position]
        holder.txtRank.text = (position + 1).toString()
        holder.txtUserName.text = user.username
        holder.txtCount.text = user.workoutCount.toString()
    }

    override fun getItemCount(): Int = list.size
}