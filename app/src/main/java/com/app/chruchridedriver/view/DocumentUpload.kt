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
import android.view.Window
import android.view.inputmethod.InputMethodManager
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
import com.app.chruchridedriver.adapter.DocumentAdapter
import com.app.chruchridedriver.data.model.Document
import com.app.chruchridedriver.data.model.DriverDetailsData
import com.app.chruchridedriver.data.model.VehiclesDetailsData
import com.app.chruchridedriver.interfaces.ClickedAdapterInterface
import com.app.chruchridedriver.repository.MainRepository
import com.app.chruchridedriver.util.CommonUtil
import com.app.chruchridedriver.viewModel.DocumentPageViewModel
import com.app.chruchridedriver.viewModel.DocumentViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import id.zelory.compressor.Compressor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.Locale


class DocumentUpload : AppCompatActivity(), ClickedAdapterInterface {
    private lateinit var documentPageViewModel: DocumentPageViewModel
    private val cu = CommonUtil()
    private var loader: MaterialProgressBar? = null
    private val CAMERA_PERMISSION_CODE = 1000
    private val READ_PERMISSION_CODE = 1001
    private val IMAGE_CHOOSE = 1000
    private val IMAGE_CAPTURE = 1001
    private var imageUri: Uri? = null
    private var isProfileImage = false
    private var documentList: ArrayList<Document>? = null
    private var adapter: DocumentAdapter? = null
    private var selectedPosition: Int = 0
    private var readPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private var driverDetailsData: DriverDetailsData? = null
    private lateinit var vehicleDetailsData: VehiclesDetailsData
    lateinit var imageLoader: Dialog
    var storage: FirebaseStorage? = null
    var storageReference: StorageReference? = null
    var loadingValue: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.documentupload)

        /* Hiding ToolBar */
        supportActionBar?.hide()

        driverDetailsData = intent.getSerializableExtra("driverDetails") as DriverDetailsData?

        imageLoader = Dialog(this)
        imageLoader.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = layoutInflater.inflate(R.layout.progressbarforimageuload, null)
        imageLoader.setContentView(view)

        loadingValue = view.findViewById(R.id.loadingValue)
        imageLoader.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        imageLoader.setCanceledOnTouchOutside(false)
        imageLoader.setCancelable(false)

        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        vehicleDetailsData =
            (intent.getSerializableExtra("vehicleDetails") as VehiclesDetailsData?)!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readPermission = Manifest.permission.READ_MEDIA_IMAGES
        }

        /* ViewModel Initialization */
        documentPageViewModel = ViewModelProvider(
            this, DocumentViewModelFactory(MainRepository())
        )[DocumentPageViewModel::class.java]
        loader = findViewById(R.id.loader)
        val backtap = findViewById<ImageView>(R.id.backtap)
        val documentnext = findViewById<CircularProgressButton>(R.id.documentnext)
        val documentView = findViewById<RecyclerView>(R.id.documentView)
        val head = findViewById<LinearLayout>(R.id.head)
        documentView.layoutManager = LinearLayoutManager(this)
        backtap.setOnClickListener {
            closeKeyboard()
            finish()
        }

        documentnext.setOnClickListener {
            if (cu.isNetworkAvailable(this)) {
                var isValid = true
                for (i in 0 until documentList!!.size) {
                    if (documentList!![i].uploaded == 0) {
                        isValid = false
                    }
                }
                if (isValid) {
                    startLoader()
                    val driverDetails = getDriverDataInMap()
                    documentPageViewModel.registerDriverDetails(driverDetails)
                } else {
                    displayMessageInAlert(getString(R.string.all_images_need_to_be_uploaded).uppercase())
                }
            } else {
                displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
            }
        }


        if (cu.isNetworkAvailable(this)) {
            startLoader()
            documentPageViewModel.getDocumentResponse()
        } else {
            displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
        }

        documentPageViewModel.errorMessage.observe(this) { errorMessage ->
            displayMessageInAlert(errorMessage)
            stopLoader()
        }

        documentPageViewModel.responseContent.observe(this) { result ->
            documentnext.visibility = View.VISIBLE
            head.visibility = View.VISIBLE
            stopLoader()
            documentList = result.documents as ArrayList<Document>
            adapter = DocumentAdapter(documentList!!, this, this)
            documentView.adapter = adapter
        }

        documentPageViewModel.registerContent.observe(this) { result ->
            if (result.data.isNotEmpty()) {
                updateLogin(result.data[0].driverId)
                val driverDocPage = Intent(this, DocumentUploadStatus::class.java)
                driverDocPage.putExtra("driverId", result.data[0].driverId)
                driverDocPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(driverDocPage)
                finish()
            }
            stopLoader()
        }

    }

    private fun displayMessageInAlert(message: String) {
        cu.showAlert(message, this)
    }

    private fun startLoader() {
        loader!!.visibility = View.VISIBLE
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
        val pos = position!!.toInt()
        selectedPosition = pos
        chooseCameraOrGallery()
    }

    private fun updateImage(uri: Uri?, imagePath: String) {
        uri?.let {
            val getData = documentList!![selectedPosition]
            getData.uploaded = 1
            getData.pathOfImage = uri
            getData.httpImage = imagePath
            documentList!![selectedPosition] = getData
            adapter!!.notifyDataSetChanged()
        }
    }

    private fun getDriverDataInMap(): MutableMap<String, String> {
        val driverDataInHashMap = driverDetailsData?.let {
            val map: MutableMap<String, String> = HashMap()
            map["profilepic"] = it.imageUrl
            map["name"] = it.name
            map["dob"] = it.dob
            map["address"] = it.address
            map["city"] = it.city
            map["churuch"] = it.churchName
            map["zipcode"] = it.zipCode
            map["mobileNumber"] = it.mobileNumber
            map["gender"] = it.gender
            map["emailaddress"] = it.emailAddress
            map["state"] = it.state
            map
        }

        driverDataInHashMap?.let {
            it["type"] = vehicleDetailsData.type
            it["make"] = vehicleDetailsData.make
            it["model"] = vehicleDetailsData.model
            it["year"] = vehicleDetailsData.year
            it["color"] = vehicleDetailsData.color
            it["doors"] = vehicleDetailsData.doors
            it["seats"] = vehicleDetailsData.seats
        }

        val sharedPreference = getSharedPreferences("FCMID", Context.MODE_PRIVATE)
        val token = sharedPreference.getString("Token", "")
        driverDataInHashMap?.let {
            it["fcmid"] = token!!
        }

        val documentId = StringBuilder()
        val documentImages = StringBuilder()
        for (i in 0 until documentList!!.size) {
            documentId.append(documentList!![i].id + ",")
            documentImages.append(documentList!![i].httpImage + ",")
        }
        var ids = documentId.toString()
        if (ids.endsWith(",")) {
            ids = ids.substring(0, ids.length - 1)
        }

        var documentimages = documentImages.toString()
        if (documentimages.endsWith(",")) {
            documentimages = documentimages.substring(0, documentimages.length - 1)
        }
        driverDataInHashMap?.let {
            it["documentIds"] = ids
            it["documentImages"] = documentimages
        }

        return driverDataInHashMap!!
    }

    private fun uploadImage() {
        imageUri?.let { imageUri ->
            storageReference?.let {
                imageLoader.show()
                GlobalScope.launch {
                    val file = File(getPath(imageUri))
                    if (file.exists()) {
                        val compressedImageFile = Compressor.compress(this@DocumentUpload, file)
                        documentPageViewModel.uploadImageToFirebase(
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
            } else {
                hideImageLoader()
                updateImage(imageUri, event)
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

    private fun updateLogin(driverId: String) {
        val sharedPreference = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("savedId", driverId)
        editor.putString("isLoggedInType", "driver")
        editor.putInt("isLoggedIn", 1)
        editor.putInt("isDoc", 1)
        editor.commit()
    }
}