package com.example.fitbody.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import java.io.File
import java.io.FileOutputStream

class CommunityFragment : Fragment() {

    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager
    private lateinit var adapter: CommunityAdapter
    private lateinit var rvPosts: RecyclerView
    private lateinit var btnOpenCreatePost: TextView
    private lateinit var imgCurrentUserAvatar: de.hdodenhof.circleimageview.CircleImageView

    private var selectedImagePath: String? = null

    // Picker for post images
    private val pickPostImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            onPostImageSelected?.invoke(it)
        }
    }
    private var onPostImageSelected: ((Uri) -> Unit)? = null

    // Picker for Profile Avatar
    private val pickAvatar = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            saveAvatarToStorage(it)
        }
    }

    private val takeAvatarPhoto = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                saveAvatarBitmapToStorage(it)
            }
        }
    }

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openCamera()
        else Toast.makeText(requireContext(), "Cần quyền camera để chụp ảnh", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)
        
        db = DatabaseHelper(requireContext())
        session = SessionManager(requireContext())
        
        rvPosts = view.findViewById(R.id.rvPosts)
        btnOpenCreatePost = view.findViewById(R.id.btnOpenCreatePost)
        imgCurrentUserAvatar = view.findViewById(R.id.imgCurrentUserAvatar)

        loadUserAvatar()
        
        setupRecyclerView()
        loadPosts()
        
        btnOpenCreatePost.setOnClickListener { showAddPostDialog() }
        view.findViewById<View>(R.id.btnPostPhoto).setOnClickListener { showAddPostDialog() }
        
        // Cập nhật ảnh đại diện khi nhấn vào avatar
        imgCurrentUserAvatar.setOnClickListener { showChangeAvatarDialog() }
        
        return view
    }

    private fun loadUserAvatar() {
        val user = db.getUserById(session.getUserId())
        if (user?.avatar.isNullOrEmpty()) {
            imgCurrentUserAvatar.setImageResource(android.R.drawable.ic_menu_gallery) // Placeholder "trống"
        } else {
            val avatar = user!!.avatar!!
            val resId = resources.getIdentifier(avatar, "drawable", requireContext().packageName)
            if (resId != 0) {
                imgCurrentUserAvatar.setImageResource(resId)
            } else {
                Glide.with(this).load(File(avatar)).into(imgCurrentUserAvatar)
            }
        }
    }

    private fun showChangeAvatarDialog() {
        val options = arrayOf("Chọn từ thư viện", "Chụp ảnh mới")
        AlertDialog.Builder(requireContext())
            .setTitle("Thay đổi ảnh đại diện")
            .setItems(options) { _, which ->
                if (which == 0) pickAvatar.launch("image/*")
                else checkCameraPermission()
            }
            .show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takeAvatarPhoto.launch(intent)
    }

    private fun saveAvatarToStorage(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().filesDir, "avatar_${session.getUserId()}_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            val path = file.absolutePath
            if (db.updateUserAvatar(session.getUserId(), path)) {
                loadUserAvatar()
                loadPosts() // Reload to update avatar in posts if any
            }
        } catch (e: Exception) {}
    }

    private fun saveAvatarBitmapToStorage(bitmap: Bitmap) {
        try {
            val file = File(requireContext().filesDir, "avatar_${session.getUserId()}_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val path = file.absolutePath
            if (db.updateUserAvatar(session.getUserId(), path)) {
                loadUserAvatar()
                loadPosts()
            }
        } catch (e: Exception) {}
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
            if (it.avatar.isNullOrEmpty()) {
                imgUser.setImageResource(android.R.drawable.ic_menu_gallery)
            } else {
                val resId = resources.getIdentifier(it.avatar, "drawable", requireContext().packageName)
                if (resId != 0) imgUser.setImageResource(resId)
                else Glide.with(this).load(File(it.avatar)).into(imgUser)
            }
        }

        btnClose.setOnClickListener { bottomSheet.dismiss() }

        onPostImageSelected = { uri ->
            selectedImagePath = uri.toString()
            layoutMediaPreview.visibility = View.VISIBLE
            btnAddPhoto.visibility = View.GONE
            Glide.with(this).load(uri).into(imgPreview)
        }

        btnAddPhoto.setOnClickListener { pickPostImage.launch("image/*") }

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
