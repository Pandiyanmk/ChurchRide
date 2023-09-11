package com.app.chruchridedriver.view

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apachat.loadingbutton.core.customViews.CircularProgressButton
import com.app.chruchridedriver.R
import com.app.chruchridedriver.adapter.ChurchAdapter
import com.app.chruchridedriver.data.model.Church
import com.app.chruchridedriver.data.model.DriverDetailsData
import com.app.chruchridedriver.interfaces.ClickedAdapterInterface
import com.app.chruchridedriver.repository.MainRepository
import com.app.chruchridedriver.util.CommonUtil
import com.app.chruchridedriver.viewModel.DriverDetailPageViewModel
import com.app.chruchridedriver.viewModel.DriverDetailsViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import id.zelory.compressor.Compressor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class DriverDetails : AppCompatActivity(), ClickedAdapterInterface {
    private lateinit var driverDetailPageViewModel: DriverDetailPageViewModel
    private val cu = CommonUtil()
    private var currentMobileNumber = ""
    private var loader: MaterialProgressBar? = null
    private var dob: EditText? = null
    private var gender: EditText? = null
    private var cal = Calendar.getInstance()
    private var churchList: List<Church>? = emptyList()
    private var choosechruch: EditText? = null
    private var profilePicture: CircleImageView? = null
    private val CAMERA_PERMISSION_CODE = 1000
    private val READ_PERMISSION_CODE = 1001
    private val IMAGE_CHOOSE = 1000
    private val IMAGE_CAPTURE = 1001
    private var imageUri: Uri? = null
    private var dialog: BottomSheetDialog? = null
    private var isProfileImage = ""
    var readPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    lateinit var imageLoader: Dialog
    var loadingValue: TextView? = null
    var storage: FirebaseStorage? = null
    var storageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.driver_details_page)

        /* Hiding ToolBar */
        supportActionBar?.hide()

        imageLoader = Dialog(this)
        imageLoader.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = layoutInflater.inflate(R.layout.progressbarforimageuload, null)
        imageLoader.setContentView(view)

        loadingValue = view.findViewById(R.id.loadingValue)
        imageLoader.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        imageLoader.setCanceledOnTouchOutside(false)
        imageLoader.setCancelable(false)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readPermission = Manifest.permission.READ_MEDIA_IMAGES
        }

        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        /* ViewModel Initialization */
        driverDetailPageViewModel = ViewModelProvider(
            this, DriverDetailsViewModelFactory(MainRepository())
        )[DriverDetailPageViewModel::class.java]
        dialog = BottomSheetDialog(this)
        dialog!!.setCancelable(true)


        currentMobileNumber = "" + intent.getStringExtra("mobileNumber")
        loader = findViewById(R.id.loader)
        val backtap = findViewById<ImageView>(R.id.backtap)
        val name = findViewById<EditText>(R.id.name)
        val emailAddress = findViewById<EditText>(R.id.email_address)
        val address = findViewById<EditText>(R.id.address)
        val city = findViewById<EditText>(R.id.city)
        val zipcode = findViewById<EditText>(R.id.zipcode)
        choosechruch = findViewById(R.id.choosechruch)
        profilePicture = findViewById(R.id.profile_picture)
        dob = findViewById(R.id.dob)
        gender = findViewById(R.id.gender)

        dob!!.isClickable = true
        dob!!.isFocusable = false
        dob!!.inputType = InputType.TYPE_NULL

        gender!!.isClickable = true
        gender!!.isFocusable = false
        gender!!.inputType = InputType.TYPE_NULL

        choosechruch!!.isClickable = true
        choosechruch!!.isFocusable = false
        choosechruch!!.inputType = InputType.TYPE_NULL

        val drivernext = findViewById<CircularProgressButton>(R.id.drivernext)
        drivernext.setOnClickListener {
            if (name.text.toString() == "" || dob!!.text.toString() == "" || emailAddress.text.toString() == "" || address.text.toString() == "" || city.text.toString() == "" || choosechruch!!.text.toString() == "" || zipcode.text.toString() == "" || gender!!.text.toString() == "" || isProfileImage.isEmpty()) {
                displayMessageInAlert(getString(R.string.all_fields_need_to_be_filled))
            } else {
                if (isValidEmail(emailAddress.text.toString())) {
                    val driverDetailsData = DriverDetailsData(
                        imageUrl = isProfileImage,
                        name = name.text.toString(),
                        dob = dob!!.text.toString(),
                        emailAddress = emailAddress.text.toString(),
                        address = address.text.toString(),
                        city = city.text.toString(),
                        churchName = choosechruch!!.text.toString(),
                        zipCode = zipcode!!.text.toString(),
                        mobileNumber = currentMobileNumber,
                        gender = gender!!.text.toString()
                    )
                    val driverDetailsIntent = Intent(this, VehicleDetails::class.java)
                    driverDetailsIntent.putExtra("driverDetails", driverDetailsData)
                    startActivity(driverDetailsIntent)
                } else {
                    displayMessageInAlert(getString(R.string.please_enter_valid_email))
                }
            }
        }
        backtap.setOnClickListener {
            closeKeyboard()
            finish()
        }

        profilePicture!!.setOnClickListener {
            chooseCameraOrGallery()
        }

        dob!!.setOnClickListener {
            displayDatePicker()
        }
        gender!!.setOnClickListener {
            genderDialog()
        }
        if (cu.isNetworkAvailable(this)) {
            startLoader()
            driverDetailPageViewModel.getChurchResponse()
        } else {
            displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
        }

        choosechruch!!.setOnClickListener {
            if (churchList!!.isNotEmpty()) {
                churchDialog(churchList!!)
            } else {
                displayMessageInAlert(getString(R.string.no_church_has_been_added_contact_admin))
            }
        }

        driverDetailPageViewModel.errorMessage.observe(this) { errorMessage ->
            displayMessageInAlert(errorMessage)
            stopLoader()
        }

        driverDetailPageViewModel.responseContent.observe(this) { result ->
            stopLoader()
            if (result.church.isNotEmpty()) {
                churchList = result.church
            }
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

    private fun displayDatePicker() {
        val date = DatePickerDialog(
            this,
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        date.datePicker.maxDate = System.currentTimeMillis() - 568025136000L
        date.show()
    }

    private val dateSetListener =
        DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val myFormat = "dd MMM yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            dob!!.setText(sdf.format(cal.time).uppercase(Locale.getDefault()))

        }

    private fun churchDialog(churchList: List<Church>) {
        val view = layoutInflater.inflate(R.layout.dialog_list, null)
        dialog!!.setContentView(view)
        val churchRecylerView = view.findViewById<RecyclerView>(R.id.rvList)
        val close = view.findViewById<CircularProgressButton>(R.id.close)
        churchRecylerView.layoutManager = LinearLayoutManager(this)
        val adapter = ChurchAdapter(this, churchList as ArrayList<Church>, this)
        churchRecylerView.adapter = adapter
        dialog!!.show()

        close.setOnClickListener {
            dialog!!.dismiss()
        }
    }

    private fun genderDialog() {
        val view = layoutInflater.inflate(R.layout.gender_dialog, null)
        dialog!!.setContentView(view)
        val close = view.findViewById<CircularProgressButton>(R.id.close)
        val male = view.findViewById<TextView>(R.id.male)
        val female = view.findViewById<TextView>(R.id.female)
        val transgender = view.findViewById<TextView>(R.id.transgender)

        male.setOnClickListener {
            gender!!.setText(male.text.toString())
            dialog!!.dismiss()
        }
        female.setOnClickListener {
            gender!!.setText(female.text.toString())
            dialog!!.dismiss()
        }
        transgender.setOnClickListener {
            gender!!.setText(transgender.text.toString())
            dialog!!.dismiss()
        }
        close.setOnClickListener {
            dialog!!.dismiss()
        }
        dialog!!.show()
    }

    override fun selectedValue(name: String?) {
        dialog?.dismiss()
        choosechruch!!.setText(name)
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
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
        MaterialAlertDialogBuilder(this).setTitle(getString(R.string.choose_profile_image))
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


    private fun uploadImage() {
        imageUri?.let { imageUri ->
            imageLoader.show()
            storageReference?.let {
                GlobalScope.launch {
                    val file = File(getPath(imageUri))
                    if (file.exists()) {
                        val compressedImageFile = Compressor.compress(this@DriverDetails, file)
                        driverDetailPageViewModel.uploadImageToFirebase(
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
            } else {
                hideImageLoader()
                profilePicture?.setImageURI(imageUri)
                isProfileImage = event
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
}

