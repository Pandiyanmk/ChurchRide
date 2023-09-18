package com.app.chruchridedriver.view

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.util.Locale


class AdminDocumentUploadStatus : AppCompatActivity(), ClickedAdapterInterface {
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admindocumentuploadstatus)

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
        val logout = findViewById<ImageView>(R.id.logout)
        val pullDownAnimateRefreshLayout =
            findViewById<PullDownAnimateRefreshLayout>(R.id.pullDownAnimateRefreshLayout)


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
                    moveToLoginpageWithDataClear()
                    val moveToLoginPage = Intent(this, LoginPage::class.java)
                    startActivity(moveToLoginPage)
                    finish()
                }.show()

        }

        pullDownAnimateRefreshLayout.setOnRefreshListener(object :
            PullDownAnimateRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                if (cu.isNetworkAvailable(this@AdminDocumentUploadStatus)) {
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

        documentUploadStatusViewModel.docStatus.observe(this) { response ->
            stopLoader()
            if (response.uploadedDocStatus.isNotEmpty()) {
                val getData = documentList!![selectedPosition]
                getData.approvedstatus = response.uploadedDocStatus[0].docstatus
                getData.comment = response.uploadedDocStatus[0].comment
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
                if (result.driverProfile[0].verified == "2") {
                    Toast.makeText(this, "Home Page", Toast.LENGTH_SHORT).show()
                } else {
                    name.text = result.driverProfile[0].name
                    mobileno.text = result.driverProfile[0].mobileno
                    email.text = result.driverProfile[0].email
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

    override fun selectedValue(position: String?) {
        if (loader!!.visibility != VISIBLE) {
            val pos = position!!.toInt()
            selectedPosition = pos
            documentDialog()
        }
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

            val view = layoutInflater.inflate(R.layout.admindocumentdialog, null)
            documentDialog!!.setContentView(view)
            val close = view.findViewById<ImageView>(R.id.close)
            val comment = view.findViewById<EditText>(R.id.comment)
            val viewImage = view.findViewById<CircularProgressButton>(R.id.viewImage)
            val accept = view.findViewById<CircularProgressButton>(R.id.accept)
            val reject = view.findViewById<CircularProgressButton>(R.id.reject)
            val heading = view.findViewById<TextView>(R.id.heading)
            heading.text = headingData

            viewImage.setOnClickListener {
                documentDialog!!.dismiss()
                val image = documentList[selectedPosition].documentimage
                val viewImagePage = Intent(this, ViewImage::class.java)
                viewImagePage.putExtra("image", image)
                viewImagePage.putExtra("heading", headingData)
                startActivity(viewImagePage)
            }

            accept.setOnClickListener {
                documentDialog!!.dismiss()
                if (cu.isNetworkAvailable(this)) {
                    startLoader()
                    documentUploadStatusViewModel.updateDocStatus(
                        documentList!![selectedPosition].id, "2", comment.text.toString()
                    )
                    documentUploadStatusViewModel.getUploadedDocument(driverId)
                } else {
                    displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
                }
            }
            reject.setOnClickListener {
                documentDialog!!.dismiss()
                if (cu.isNetworkAvailable(this)) {
                    startLoader()
                    documentUploadStatusViewModel.updateDocStatus(
                        documentList!![selectedPosition].id, "1", comment.text.toString()
                    )
                    documentUploadStatusViewModel.getUploadedDocument(driverId)
                } else {
                    displayMessageInAlert(getString(R.string.no_internet).uppercase(Locale.getDefault()))
                }
            }
            close.setOnClickListener {
                documentDialog!!.dismiss()
            }
            documentDialog!!.show()
        }
    }

    private fun moveToLoginpageWithDataClear() {
        val sharedPreference = getSharedPreferences("LOGIN", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.putString("savedId", "")
        editor.putString("isLoggedInType", "")
        editor.putInt("isLoggedIn", 0)
        editor.putInt("isDoc", 0)
        editor.commit()
    }
}