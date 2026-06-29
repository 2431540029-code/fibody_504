package com.example.fitbody.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.model.Workout
import com.example.fitbody.ui.detail.WorkoutDetailActivity

class WorkoutAdapter(
    private val list: List<Workout>,
    private val onLongClick: ((Workout) -> Unit)? = null // Chapter 4.1: Events (Long Click)
) : RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtWorkoutName: TextView = view.findViewById(R.id.txtWorkoutName)
        val txtDuration: TextView = view.findViewById(R.id.txtDuration)
        val imgWorkoutThumb: ImageView = view.findViewById(R.id.imgWorkoutThumb)
        val btnVideoLink: ImageView = view.findViewById(R.id.btnVideoLink)
        
        val txtSets: TextView = view.findViewById(R.id.txtSets)
        val txtReps: TextView = view.findViewById(R.id.txtReps)
        val txtMuscleGroup: TextView = view.findViewById(R.id.txtMuscleGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = list[position]
        holder.txtWorkoutName.text = workout.workout_name
        
        if (workout.sets_count.contains("00:", true)) {
            holder.txtDuration.text = workout.sets_count
        } else {
            holder.txtDuration.text = "x${workout.reps_count}"
        }

        // Tự động tìm icon theo tên bài tập (không dấu, thay khoảng trắng bằng gạch dưới)
        val cleanName = workout.workout_name.lowercase()
            .replace(" ", "_")
            .replace("á|à|ả|ã|ạ|ă|ắ|ằ|ẳ|ẵ|ặ|â|ấ|ầ|ẩ|ẫ|ậ".toRegex(), "a")
            .replace("é|è|ẻ|ẽ|ẹ|ê|ế|ề|ể|ễ|ệ".toRegex(), "e")
            .replace("í|ì|ỉ|ĩ|ị".toRegex(), "i")
            .replace("ó|ò|ỏ|õ|ọ|ô|ố|ồ|ổ|ỗ|ộ|ơ|ớ|ờ|ở|ỡ|ợ".toRegex(), "o")
            .replace("ú|ù|ủ|ũ|ụ|ư|ứ|ừ|ử|ữ|ự".toRegex(), "u")
            .replace("ý|ỳ|ỷ|ỹ|ỵ".toRegex(), "y")
            .replace("đ".toRegex(), "d")

        val resId = holder.itemView.context.resources.getIdentifier(
            cleanName,
            "raw",
            holder.itemView.context.packageName
        )
        
        Glide.with(holder.itemView.context)
            .asGif()
            .load(if (resId != 0) resId else R.drawable.ic_launcher_background)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imgWorkoutThumb)

        // Hiển thị nút video nếu có link
        if (!workout.video_url.isNullOrEmpty() && workout.video_url.startsWith("http")) {
            holder.btnVideoLink.visibility = View.VISIBLE
            holder.btnVideoLink.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(workout.video_url))
                holder.itemView.context.startActivity(intent)
            }
        } else {
            holder.btnVideoLink.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, WorkoutDetailActivity::class.java)
            intent.putExtra("workout_id", workout.id) // Cần ID để chỉnh sửa
            intent.putExtra("trainer_id", workout.trainer_id)
            intent.putExtra("workout_name", workout.workout_name)
            intent.putExtra("sets", workout.sets_count)
            intent.putExtra("reps", workout.reps_count)
            intent.putExtra("muscle", workout.muscle_group)
            intent.putExtra("video_url", workout.video_url)
            holder.itemView.context.startActivity(intent)
        }

        // Handle Long Click (Chapter 4.1)
        holder.itemView.setOnLongClickListener {
            onLongClick?.invoke(workout)
            true
        }
    }
}
