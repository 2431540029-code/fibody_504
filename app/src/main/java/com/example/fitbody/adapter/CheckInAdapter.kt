package com.example.fitbody.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R

class CheckInAdapter(
    private val checkInList: List<String>
) : RecyclerView.Adapter<CheckInAdapter.CheckInViewHolder>() {

    class CheckInViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val txtTitle: TextView =
            itemView.findViewById(R.id.txtCheckInTitle)

        val txtTime: TextView =
            itemView.findViewById(R.id.txtCheckInTime)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CheckInViewHolder {

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_checkin,
                    parent,
                    false
                )

        return CheckInViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CheckInViewHolder,
        position: Int
    ) {
        holder.txtTitle.text = "Check-in phòng gym"
        holder.txtTime.text = checkInList[position]
    }

    override fun getItemCount(): Int {
        return checkInList.size
    }
}