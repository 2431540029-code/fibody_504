package com.example.fitbody.ui.pt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager
import java.io.File

class PTNotificationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pt_notifications)

        findViewById<TextView>(R.id.txtTitle).text = "Thông báo"
        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvPtNotifications)
        rv.layoutManager = LinearLayoutManager(this)

        loadNotifications(rv)
    }

    private fun loadNotifications(rv: RecyclerView) {
        val db = DatabaseHelper(this)
        val trainerId = db.getTrainerIdByUsername(SessionManager(this).getUsername())
        
        // Giả lập thông báo từ dữ liệu tương tác thật (Enroll, Like, Review, Favorite)
        val students = db.getStudentsForTrainer(trainerId)
        val notifications = students.map { user ->
            NotifItem(
                user.username,
                user.avatar,
                "đã đăng ký theo dõi bạn",
                "Hôm nay",
                R.drawable.ic_globe,
                "#7C4DFF"
            )
        }
        
        rv.adapter = NotifAdapter(notifications)
    }

    data class NotifItem(val name: String, val avatar: String?, val action: String, val date: String, val icon: Int, val color: String)

    class NotifAdapter(val list: List<NotifItem>) : RecyclerView.Adapter<NotifAdapter.VH>() {
        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val img = v.findViewById<ImageView>(R.id.imgNotifUserAvatar)
            val content = v.findViewById<TextView>(R.id.txtNotifContent)
            val date = v.findViewById<TextView>(R.id.txtNotifDate)
            val icon = v.findViewById<ImageView>(R.id.imgNotifType)
        }
        override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(R.layout.item_pt_notification, p, false))
        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = list[pos]
            h.content.text = "${item.name} ${item.action}"
            h.date.text = item.date
            h.icon.setImageResource(item.icon)
            h.icon.setColorFilter(android.graphics.Color.parseColor(item.color))
            
            if (!item.avatar.isNullOrEmpty()) {
                val resId = h.itemView.context.resources.getIdentifier(item.avatar, "drawable", h.itemView.context.packageName)
                if (resId != 0) h.img.setImageResource(resId)
                else Glide.with(h.itemView.context).load(File(item.avatar)).into(h.img)
            } else {
                h.img.setImageResource(R.drawable.male)
            }
        }
        override fun getItemCount() = list.size
    }
}
