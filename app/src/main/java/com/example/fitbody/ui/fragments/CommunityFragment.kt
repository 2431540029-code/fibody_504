package com.example.fitbody.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitbody.R
import com.example.fitbody.adapter.CommunityAdapter
import com.example.fitbody.database.DatabaseHelper
import com.example.fitbody.utils.SessionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class CommunityFragment : Fragment() {

    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager
    private lateinit var adapter: CommunityAdapter
    private lateinit var rvPosts: RecyclerView
    private lateinit var btnOpenCreatePost: TextView
    private lateinit var imgCurrentUserAvatar: de.hdodenhof.circleimageview.CircleImageView

    private var selectedImagePath: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            onImageSelected?.invoke(it)
        }
    }
    
    private var onImageSelected: ((Uri) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)
        
        db = DatabaseHelper(requireContext())
        session = SessionManager(requireContext())
        
        rvPosts = view.findViewById(R.id.rvPosts)
        btnOpenCreatePost = view.findViewById(R.id.btnOpenCreatePost)
        imgCurrentUserAvatar = view.findViewById(R.id.imgCurrentUserAvatar)

        val user = db.getUserById(session.getUserId())
        user?.avatar?.let { avatar ->
            val resId = resources.getIdentifier(avatar, "drawable", requireContext().packageName)
            if (resId != 0) imgCurrentUserAvatar.setImageResource(resId)
        }
        
        setupRecyclerView()
        loadPosts()
        
        btnOpenCreatePost.setOnClickListener { showAddPostDialog() }
        view.findViewById<View>(R.id.btnPostPhoto).setOnClickListener { showAddPostDialog() }
        
        return view
    }

    private fun setupRecyclerView() {
        adapter = CommunityAdapter(emptyList(), 
            onLikeClick = { post ->
                if (db.togglePostLike(session.getUserId(), post.id)) {
                    loadPosts()
                }
            },
            onCommentClick = { Toast.makeText(requireContext(), "Tính năng bình luận đang phát triển", Toast.LENGTH_SHORT).show() }
        )
        rvPosts.layoutManager = LinearLayoutManager(requireContext())
        rvPosts.adapter = adapter
    }

    private fun loadPosts() {
        val posts = db.getAllPosts(session.getUserId())
        adapter.updateData(posts)
    }

    private fun showAddPostDialog() {
        val bottomSheet = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_create_post, null)
        bottomSheet.setContentView(view)

        val etContent = view.findViewById<EditText>(R.id.etContentCreate)
        val btnSubmit = view.findViewById<Button>(R.id.btnPostSubmit)
        val btnClose = view.findViewById<View>(R.id.btnClose)
        val imgUser = view.findViewById<ImageView>(R.id.imgUserCreate)
        val txtName = view.findViewById<TextView>(R.id.txtNameCreate)
        val btnAddPhoto = view.findViewById<View>(R.id.btnAddPhoto)
        val layoutMediaPreview = view.findViewById<View>(R.id.layoutMediaPreview)
        val imgPreview = view.findViewById<ImageView>(R.id.imgPreview)
        val btnRemoveMedia = view.findViewById<View>(R.id.btnRemoveMedia)
        val btnPostCall = view.findViewById<View>(R.id.btnPostCall)
        val btnPostGif = view.findViewById<View>(R.id.btnPostGif)
        val chipFeeling = view.findViewById<View>(R.id.chipFeeling)
        val chipLocation = view.findViewById<View>(R.id.chipLocation)
        val chipCollaborator = view.findViewById<View>(R.id.chipCollaborator)

        selectedImagePath = null
        val user = db.getUserById(session.getUserId())
        user?.let {
            txtName.text = it.username
            it.avatar?.let { avatar ->
                val resId = resources.getIdentifier(avatar, "drawable", requireContext().packageName)
                if (resId != 0) imgUser.setImageResource(resId)
            }
        }

        btnClose.setOnClickListener { bottomSheet.dismiss() }

        onImageSelected = { uri ->
            selectedImagePath = uri.toString()
            layoutMediaPreview.visibility = View.VISIBLE
            btnAddPhoto.visibility = View.GONE
            Glide.with(this).load(uri).into(imgPreview)
        }

        btnAddPhoto.setOnClickListener { pickImage.launch("image/*") }

        btnRemoveMedia.setOnClickListener {
            selectedImagePath = null
            layoutMediaPreview.visibility = View.GONE
            btnAddPhoto.visibility = View.VISIBLE
        }

        btnPostCall.setOnClickListener { showPhoneDialog() }
        btnPostGif.setOnClickListener { 
            showGifDialog { gifRes ->
                selectedImagePath = gifRes
                layoutMediaPreview.visibility = View.VISIBLE
                btnAddPhoto.visibility = View.GONE
                val resId = resources.getIdentifier(gifRes, "raw", requireContext().packageName)
                Glide.with(this).asGif().load(resId).into(imgPreview)
            }
        }

        chipFeeling.setOnClickListener { showFeelingDialog() }
        chipLocation.setOnClickListener { showLocationDialog() }
        chipCollaborator.setOnClickListener { showCollaboratorDialog() }

        btnSubmit.setOnClickListener {
            val content = etContent.text.toString()
            if (content.isNotEmpty() || selectedImagePath != null) {
                if (db.addPost(session.getUserId(), content, selectedImagePath) != -1L) {
                    loadPosts()
                    bottomSheet.dismiss()
                    Toast.makeText(requireContext(), "Đã đăng bài thành công!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập nội dung hoặc chọn ảnh", Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheet.setOnShowListener {
            val dialog = it as BottomSheetDialog
            val bs = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bs?.let { b ->
                BottomSheetBehavior.from(b).state = BottomSheetBehavior.STATE_EXPANDED
                b.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            }
        }
        bottomSheet.show()
    }

    private fun showPhoneDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input_phone, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhoneNumber)
        dialogView.findViewById<View>(R.id.btnClosePhone).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnContinuePhone).setOnClickListener {
            if (etPhone.text.isNotEmpty()) {
                Toast.makeText(requireContext(), "Đã gắn số điện thoại: ${etPhone.text}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showGifDialog(onGifSelected: (String) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_select_gif, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val rvGifs = dialogView.findViewById<RecyclerView>(R.id.rvGifList)
        val gifs = listOf("bat_nhay", "da_chan_sau", "bat_nhay_jack", "banh_nguc_ta_doi")
        
        rvGifs.layoutManager = GridLayoutManager(requireContext(), 2)
        rvGifs.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(p: ViewGroup, t: Int) = object : RecyclerView.ViewHolder(ImageView(p.context).apply { 
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }) {}
            override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
                val gif = gifs[pos]
                val resId = resources.getIdentifier(gif, "raw", requireContext().packageName)
                Glide.with(this@CommunityFragment).asGif().load(resId).into(h.itemView as ImageView)
                h.itemView.setOnClickListener {
                    onGifSelected(gif)
                    dialog.dismiss()
                }
            }
            override fun getItemCount() = gifs.size
        }
        
        dialogView.findViewById<View>(R.id.btnCloseGif).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showFeelingDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_select_feeling, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        view.findViewById<View>(R.id.btnCloseFeeling).setOnClickListener { dialog.dismiss() }
        
        val rv = view.findViewById<RecyclerView>(R.id.rvFeelingList)
        val feelings = listOf("😊 hạnh phúc", "😇 có phúc", "😘 được yêu", "😔 buồn", "😍 đáng yêu", "🙏 biết ơn", "🤩 hào hứng", "🥰 đang yêu", "🤪 điên", "🤗 cảm kích", "😌 sung sướng", "🥳 tuyệt vời")
        
        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(p: ViewGroup, t: Int) = object : RecyclerView.ViewHolder(TextView(p.context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(32, 32, 32, 32)
                setTextColor(android.graphics.Color.WHITE)
                textSize = 15f
            }) {}
            override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
                (h.itemView as TextView).text = feelings[pos]
                h.itemView.setOnClickListener {
                    Toast.makeText(requireContext(), "Đang cảm thấy ${feelings[pos]}", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
            override fun getItemCount() = feelings.size
        }
        dialog.show()
    }

    private fun showLocationDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_select_location, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        view.findViewById<View>(R.id.btnCloseLocation).setOnClickListener { dialog.dismiss() }
        
        val rv = view.findViewById<RecyclerView>(R.id.rvLocationList)
        val locations = listOf("HANA LASH Studio", "No Where....", "Bánh Mì Hòa Mã", "Hà Nội", "Thành phố Hồ Chí Minh")
        
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(p: ViewGroup, t: Int) = object : RecyclerView.ViewHolder(TextView(p.context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(32, 32, 32, 32)
                setTextColor(android.graphics.Color.WHITE)
                textSize = 15f
                setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_mylocation, 0, 0, 0)
                compoundDrawablePadding = 24
            }) {}
            override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
                (h.itemView as TextView).text = locations[pos]
                h.itemView.setOnClickListener {
                    Toast.makeText(requireContext(), "Tại ${locations[pos]}", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
            override fun getItemCount() = locations.size
        }
        dialog.show()
    }

    private fun showCollaboratorDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_select_collaborator, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        view.findViewById<View>(R.id.btnCloseCollaborator).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.btnDoneCollab).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
