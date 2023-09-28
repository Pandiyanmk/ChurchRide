package com.app.chruchridedriver.view

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.View.VISIBLE
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apachat.loadingbutton.core.customViews.CircularProgressButton
import com.app.chruchridedriver.R
import com.app.chruchridedriver.adapter.UploadedDocumentAdapter
import com.app.chruchridedriver.data.model.UploadedDocumentX
import com.app.chruchridedriver.data.model.driverProfileX
import com.app.chruchridedriver.interfaces.ClickedAdapterInterface
import com.app.chruchridedriver.repository.MainRepository
import com.app.chruchridedriver.util.CommonUtil
import com.app.chruchridedriver.viewModel.DocumentUploadStatusViewModel
import com.app.chruchridedriver.viewModel.DocumentUploadViewModelFactory
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import dev.jahidhasanco.pulldownanimaterefresh.PullDownAnimateRefreshLayout
import id.zelory.compressor.Compressor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.Locale


class DocumentUploadStatus : AppCompatActivity(), ClickedAdapterInterface {
    private lateinit var documentUploadStatusViewModel: DocumentUploadStatusViewModel
    private val cu = CommonUtil()
    private var loader: MaterialProgressBar? = null
    private val CAMERA_PERMISSION_CODE = 1000
    private val READ_PERMISSION_CODE = 1001
    private val IMAGE_CHOOSE = 1000
    private val IMAGE_CAPTURE = 1001
    private var imageUri: Uri? = null
    private var documentList: ArrayList<UploadedDocumentX>? = null
    private var driverProfile: ArrayList<driverProfileX>? = null
    private var adapter: UploadedDocumentAdapter? = null
    private var selectedPosition: Int = 0
    private var readPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private lateinit var imageLoader: Dialog
    private var storage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var loadingValue: TextView? = null
    var driverId = ""
    private var documentDialog: BottomSheetDialog? = null
    private var profileDialog: BottomSheetDialog? = null
    lateinit var pullDownAnimateRefreshLayout: PullDownAnimateRefreshLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.documentuploadstatus)

        /* Hiding ToolBar */
        supportActionBar?.hide()
        driverId = intent.getStringExtra("driverId").toString()
        imageLoader = Dialog(this)
        imageLoader.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = layoutInflater.inflate(R.layout.progressbarforimageuload, null)
        imageLoader.setContentView(view)

        documentDialog = BottomSheetDialog(this)
        documentDialog!!.setCancelable(true)

        profileDialog = BottomSheetDialog(this)
        profileDialog!!.setCancelable(true)

        loadingValue = view.findViewById(R.id.loadingValue)
        imageLoader.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        imageLoader.setCanceledOnTouchOutside(false)
        imageLoader.setCancelable(false)

        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readPermission = Manifest.permission.READ_MEDIA_IMAGES
        }

        /* ViewModel Initialization */
        documentUploadStatusViewModel = ViewModelProvider(
            this, DocumentUploadViewModelFactory(MainRepository())
        )[DocumentUploadStatusViewModel::class.java]
        loader = findViewById(R.id.loader)
        val backtap = findViewById<ImageView>(R.id.backtap)
        val documentView = findViewById<RecyclerView>(R.id.documentView)
        val head = findViewById<LinearLayout>(R.id.head)
        val name = findViewById<TextView>(R.id.name)
        val mobileno = findViewById<TextView>(R.id.mobileno)
        val profileview = findViewById<FloatingActionButton>(R.id.profileview)
        val email = findViewById<TextView>(R.id.email)
        val contactus = findViewById<TextView>(R.id.contactus)
        val logout = findViewById<ImageView>(R.id.logout)
        pullDownAnimateRefreshLayout = findViewById(R.id.pullDownAnimateRefreshLayout)


        val type = findViewById<EditText>(R.id.type)
        val make = findViewById<EditText>(R.id.make)
        val model = findViewById<EditText>(R.id.model)
        val year = findViewById<EditText>(R.id.year)
        val vcolor = findViewById<EditText>(R.id.vcolor)
        val doors = findViewById<EditText>(R.id.doors)

        type.isEnabled = false
        make.isEnabled = false
        model.isEnabled = false
        year.isEnabled = false
        vcolor.isEnabled = false
        doors.isEnabled = false


        val profile_picture = findViewById<CircleImageView>(R.id.profile_picture)
        documentView.layoutManager = LinearLayoutManager(this)
        backtap.setOnClickListener {
            closeKeyboard()
            finish()
        }
        profileview.setOnClickListener {
            if (driverProfile!!.isNotEmpty()) {
                openProfileBottomSheetDialog(driverProfile!!)
            }
        }
        logout.setOnClickListener {
            MaterialAlertDialogBuilder(
                this, R.style.AlertDialogTheme
            ).setTitle(getString(R.string.are_you_sure_want_to_logout))
                .setNeutralButton(getString(R.string.cancel)) { _, _ ->
                    // Respond to neutral button press
                }.setPositiveButton(getString(R.string.logout)) { _, _ ->
                    cu.moveToLoginpageWithDataClear(this)
                    val moveToLoginPage = Intent(this, LoginPage::class.java)
                    startActivity(moveToLoginPage)
                    finish()
                }.show()

        }

        pullDownAnimateRefreshLayout.setOnRefreshListener(object :
            PullDownAnimateRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                if (cu.isNetworkAvailable(this@DocumentUploadStatus)) {
                    startLoader()
                    pullDownAnimateRefreshLayout.setRefreshing(true)
                    documentUploadStatusViewModel.getUploadedDocument(driverId)
                } else {
                    pullDownAnimateRefreshLayout.setRefreshing(false)
                    displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
                }
            }
        })
        if (cu.isNetworkAvailable(this)) {
            startLoader()
            documentUploadStatusViewModel.getUploadedDocument(driverId)
        } else {
            displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
        }

        documentUploadStatusViewModel.errorMessage.observe(this) { errorMessage ->
            pullDownAnimateRefreshLayout.setRefreshing(false)
            displayMessageInAlert(errorMessage)
            stopLoader()
        }

        documentUploadStatusViewModel.registerContent.observe(this) { response ->
            stopLoader()
            if (response.uploadedDocumentImage.isNotEmpty()) {
                val getData = documentList!![selectedPosition]
                getData.approvedstatus = "0"
                getData.documentimage = response.uploadedDocumentImage[0].imageUrl
                documentList!![selectedPosition] = getData
                adapter!!.notifyDataSetChanged()
            }
        }

        documentUploadStatusViewModel.responseContent.observe(this) { result ->
            head.visibility = VISIBLE
            stopLoader()
            pullDownAnimateRefreshLayout.setRefreshing(false)
            if (result.driverProfile.isNotEmpty()) {
                driverProfile = result.driverProfile as ArrayList<driverProfileX>
                if (result.driverProfile[0].verified == "1") {
                    updateLogin(driverId, "driver")
                    val driverDocPage = Intent(this, DriverHomePage::class.java)
                    driverDocPage.putExtra("driverId", driverId)
                    driverDocPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(driverDocPage)
                    finish()
                } else {
                    name.text = result.driverProfile[0].name
                    mobileno.text = result.driverProfile[0].mobileno
                    email.text = result.driverProfile[0].email
                    contactus.text = result.driverProfile[0].contactus
                    Glide.with(this).load(result.driverProfile[0].profilePic)
                        .placeholder(R.drawable.uploadprofile).into(profile_picture)
                }
                type.setText(result.vehicleData[0].type.uppercase())
                make.setText(result.vehicleData[0].make.uppercase())
                model.setText(result.vehicleData[0].model.uppercase())
                year.setText(result.vehicleData[0].year.uppercase())
                vcolor.setText(result.vehicleData[0].color.uppercase())
                doors.setText(result.vehicleData[0].doors + " & " + result.vehicleData[0].seats)
            }

            documentList = result.uploadedDocuments as ArrayList<UploadedDocumentX>
            adapter = UploadedDocumentAdapter(documentList!!, this, this)
            documentView.adapter = adapter
        }

    }

    private fun displayMessageInAlert(message: String) {
        cu.showAlert(message, this)
    }

    private fun startLoader() {
        loader!!.visibility = VISIBLE
    }

    private fun stopLoader() {
        loader!!.visibility = View.INVISIBLE
    }

    private fun closeKeyboard() {
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun requestCameraPermission(): Boolean {
        var permissionGranted = false

        // If system os is Marshmallow or Above, we need to request runtime permission
        val cameraPermissionNotGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_DENIED
        if (cameraPermissionNotGranted) {
            val permission = arrayOf(Manifest.permission.CAMERA)

            // Display permission dialog
            requestPermissions(permission, CAMERA_PERMISSION_CODE)
        } else {
            // Permission already granted
            permissionGranted = true
        }

        return permissionGranted
    }

    private fun openCameraInterface() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "")
        imageUri = contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        // Create camera intent
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        // Launch intent
        startActivityForResult(intent, IMAGE_CAPTURE)
    }

    // Handle Allow or Deny response from the permission dialog
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                openCameraInterface()
            } else {
                // Permission was denied
                displayMessageInAlert(getString(R.string.camera_permission_was_denied_unable_to_take_a_picture))
            }
        } else if (requestCode == READ_PERMISSION_CODE) {
            val storagePermissionGranted = ContextCompat.checkSelfPermission(
                this, readPermission
            ) == PackageManager.PERMISSION_GRANTED
            if (storagePermissionGranted) {
                chooseImageGallery()
            } else {
                displayMessageInAlert(getString(R.string.storage_permission_was_denied))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_CAPTURE) {
            uploadImage()
        } else if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_CHOOSE) {
            imageUri = data?.data
            uploadImage()
        }
    }

    private fun chooseCameraOrGallery() {
        MaterialAlertDialogBuilder(
            this, R.style.AlertDialogTheme
        ).setTitle(getString(R.string.choose_image_to_upload))
            .setNeutralButton(getString(R.string.cancel)) { _, _ ->
                // Respond to neutral button press
            }.setNegativeButton(getString(R.string.camera)) { _, _ ->
                val permissionGranted = requestCameraPermission()
                if (permissionGranted) {
                    openCameraInterface()
                }
            }.setPositiveButton(getString(R.string.gallery)) { _, _ ->
                val permissionGranted = requestStoragePermission()
                if (permissionGranted) {
                    chooseImageGallery()
                }
            }.show()
    }

    private fun chooseImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_CHOOSE)
    }

    private fun requestStoragePermission(): Boolean {
        var permissionGranted = false
        val storagePermissionNotGranted = ContextCompat.checkSelfPermission(
            this, readPermission
        ) == PackageManager.PERMISSION_DENIED
        if (storagePermissionNotGranted) {
            val permission = arrayOf(readPermission)
            requestPermissions(permission, READ_PERMISSION_CODE)
        } else {
            permissionGranted = true
        }
        return permissionGranted
    }

    override fun selectedValue(position: String?) {
        if (loader!!.visibility != VISIBLE) {
            val pos = position!!.toInt()
            selectedPosition = pos
            documentDialog()
        }
    }

    private fun updateImage(imagePath: String) {
        startLoader()
        documentUploadStatusViewModel.updateDocument(documentList!![selectedPosition].id, imagePath)
    }

    private fun uploadImage() {
        imageUri?.let { imageUri ->
            storageReference?.let {
                imageLoader.show()
                GlobalScope.launch {
                    val file = File(getPath(imageUri))
                    if (file.exists()) {
                        val compressedImageFile =
                            Compressor.compress(this@DocumentUploadStatus, file)
                        documentUploadStatusViewModel.uploadImageToFirebase(
                            it, Uri.fromFile(compressedImageFile)
                        )
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: String?) {
        event?.let {
            if (it.startsWith("isLoading")) {
                if (imageLoader.isShowing) {
                    loadingValue?.let { loadView ->
                        loadView.text = event.replace("isLoading", "")
                    }
                }
            } else if (it.startsWith("Failed")) {
                imageLoader.dismiss()
                hideImageLoader()
                displayMessageInAlert(getString(R.string.failed_to_upload_image_try_again))
            } else if (it == "doc_status") {
                if (cu.isNetworkAvailable(this@DocumentUploadStatus)) {
                    startLoader()
                    pullDownAnimateRefreshLayout.setRefreshing(true)
                    documentUploadStatusViewModel.getUploadedDocument(driverId)
                }
            } else {
                hideImageLoader()
                updateImage(event)
            }
        }
    }

    private fun hideImageLoader() {
        if (imageLoader.isShowing) {
            imageLoader.dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null) ?: return null
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val s = cursor.getString(column_index)
        cursor.close()
        return s
    }

    private fun openProfileBottomSheetDialog(driverProfile: ArrayList<driverProfileX>) {
        val view = layoutInflater.inflate(R.layout.driverdetaildialog, null)
        profileDialog!!.setContentView(view)
        val name = view.findViewById<View>(R.id.name) as EditText
        val dob = view.findViewById<View>(R.id.dob) as EditText
        val gender = view.findViewById<View>(R.id.gender) as EditText
        val email_address = view.findViewById<View>(R.id.email_address) as EditText
        val address = view.findViewById<View>(R.id.address) as EditText
        val city = view.findViewById<View>(R.id.city) as EditText
        val state = view.findViewById<View>(R.id.state) as EditText
        val choosechruch = view.findViewById<View>(R.id.choosechruch) as EditText
        val zipcode = view.findViewById<View>(R.id.zipcode) as EditText
        val closebutton = view.findViewById<View>(R.id.closebutton) as CircularProgressButton

        name.isEnabled = false
        dob.isEnabled = false
        gender.isEnabled = false
        email_address.isEnabled = false
        address.isEnabled = false
        city.isEnabled = false
        state.isEnabled = false
        choosechruch.isEnabled = false
        zipcode.isEnabled = false

        name.setText(driverProfile[0].name)
        dob.setText(driverProfile[0].dob)
        gender.setText(driverProfile[0].gender)
        email_address.setText(driverProfile[0].email)
        address.setText(driverProfile[0].address)
        city.setText(driverProfile[0].city)
        state.setText(driverProfile[0].state)
        choosechruch.setText(driverProfile[0].church)
        zipcode.setText(driverProfile[0].zipcode)


        if (!profileDialog!!.isShowing) {
            profileDialog!!.show()
        }

        closebutton.setOnClickListener {
            profileDialog!!.dismiss()
        }

    }

    private fun documentDialog() {
        documentList?.let { documentList ->
            val headingData = documentList[selectedPosition].documentName
            val approvedstatus = documentList[selectedPosition].approvedstatus

            val view = layoutInflater.inflate(R.layout.document_dialog, null)
            documentDialog!!.setContentView(view)
            val close = view.findViewById<CircularProgressButton>(R.id.close)
            val reupload = view.findViewById<FloatingActionButton>(R.id.reupload)
            val viewupload = view.findViewById<FloatingActionButton>(R.id.viewupload)
            val reuploadlay = view.findViewById<LinearLayout>(R.id.reuploadlay)
            val heading = view.findViewById<TextView>(R.id.heading)
            heading.text = headingData

            if (approvedstatus == "1") {
                reuploadlay.visibility = VISIBLE
            } else {
                reuploadlay.visibility = View.GONE
            }

            viewupload.setOnClickListener {
                documentDialog!!.dismiss()
                val image = documentList[selectedPosition].documentimage
                val viewImagePage = Intent(this, ViewImage::class.java)
                viewImagePage.putExtra("image", image)
                viewImagePage.putExtra("heading", headingData)
                startActivity(viewImagePage)
            }
            reupload.setOnClickListener {
                documentDialog!!.dismiss()
                chooseCameraOrGallery()
            }

            close.setOnClickListener {
                documentDialog!!.dismiss()
            }
            documentDialog!!.show()
        }
    }

    private fun updateLogin(driverId: String, type: String) {
        val sharedPreference = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("savedId", driverId)
        editor.putString("isLoggedInType", type)
        editor.putInt("isLoggedIn", 1)
        editor.putInt("isDoc", 2)
        editor.commit()
    }
}