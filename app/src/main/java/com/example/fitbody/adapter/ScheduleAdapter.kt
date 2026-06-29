package com.example.fitbody.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbody.R
import com.example.fitbody.model.Schedule

class ScheduleAdapter(
    private val list: List<Schedule>,
    private val onCompleteClick: (Schedule) -> Unit,
    private val onDeleteClick: (Schedule) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtDay: TextView = view.findViewById(R.id.txtDay)
        val txtWorkout: TextView = view.findViewById(R.id.txtWorkout)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val txtComplete: TextView = view.findViewById(R.id.txtComplete)
        val txtDelete: TextView = view.findViewById(R.id.txtDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_schedule, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.txtDay.text = item.day_name
        holder.txtWorkout.text = item.workout_plan
        holder.txtStatus.text = item.status

        holder.txtComplete.setOnClickListener {
            onCompleteClick(item)
        }

        holder.txtDelete.setOnClickListener {
            onDeleteClick(item)
        }
    }
}