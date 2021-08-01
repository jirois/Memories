package com.example.helloapplication.activity

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.example.helloapplication.R
import com.example.helloapplication.database.DatabaseHandler
import com.example.helloapplication.databinding.ActivityAddHappyPlacesBinding
import com.example.helloapplication.model.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaces : AppCompatActivity() {
    private lateinit var binding: ActivityAddHappyPlacesBinding
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var mHappyPlaceDetail: HappyPlaceModel? = null

    //  Now as per our Data Model Class we need some of the values to
    //  be passed so let us create that global which will be used later on.
    // START
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_happy_places)

        setSupportActionBar(binding.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAddPlace.setNavigationOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetail = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel

        }


        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.DAY_OF_YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            updateDateInView()

        }
        updateDateInView()

        if (mHappyPlaceDetail != null) {
            supportActionBar?.title = "Edit Happy Memories"

            binding.etTitle.setText(mHappyPlaceDetail!!.title)
            binding.etDescription.setText(mHappyPlaceDetail!!.description)
            binding.etDate.setText(mHappyPlaceDetail!!.date)
            binding.etLocation.setText(mHappyPlaceDetail!!.location)
            mLatitude = mHappyPlaceDetail!!.latitude
            mLongitude = mHappyPlaceDetail!!.longitude

            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetail!!.image)

            binding.ivPlaceImage.setImageURI(saveImageToInternalStorage)

            binding.btnSave.text = "UPDATE"
        }


        binding.etDate.setOnClickListener {
                    DatePickerDialog(
                        this,
                        dateSetListener,
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
        }


        binding.tvAddImage.setOnClickListener {
            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle("Select Action")
            val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera" )
            pictureDialog.setItems( pictureDialogItems ){
                _, which ->
                when(which){
                    0 -> choosePhotoFromGallery()
                    1 -> takePhotoFromCamera()
                }
            }
            pictureDialog.show()
        }

        binding.btnSave.setOnClickListener {
            when{
                binding.etTitle.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()
                }
                binding.etDescription.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show()
            }
                binding.etLocation.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter location", Toast.LENGTH_SHORT).show()

                }
                saveImageToInternalStorage == null ->{

                        Toast.makeText(this, "Please enter title", Toast.LENGTH_SHORT).show()

                } else -> {
                val happyPlaceModel = HappyPlaceModel(
                    // Changing the id if it is for edit.
                    if (mHappyPlaceDetail == null) 0 else mHappyPlaceDetail!!.id,
                    binding.etTitle.text.toString(),
                    saveImageToInternalStorage.toString(),
                    binding.etDescription.text.toString(),
                    binding.etDate.text.toString(),
                    binding.etLocation.text.toString(),
                    mLatitude,
                    mLongitude
                )

                //Initialize the database handler
                val dbHandler = DatabaseHandler(this)

                if (mHappyPlaceDetail == null) {
                    val addHappyPlaces = dbHandler.addHappyPlace(happyPlaceModel)

                    if (addHappyPlaces > 0) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }else {
                    val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)

                    if (updateHappyPlace > 0) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
            }

        }

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent? ){
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK ) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val selectedImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Save Image: ", "Path :: $saveImageToInternalStorage")

                        binding.ivPlaceImage!!.setImageBitmap(selectedImageBitmap)

                    } catch (e: IOException) {
                        e.printStackTrace()

                        Toast.makeText(this@AddHappyPlaces, "Fail", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (requestCode == CAMERA) {
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap

                saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                Log.e("Save Image: ", "Path :: $saveImageToInternalStorage")

                binding.ivPlaceImage!!.setImageBitmap(thumbnail)
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Cancelled", "Cancelled")
        }

    }

    private fun updateDateInView(){
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDate.setText(sdf.format(cal.time).toString())
    }

    private fun choosePhotoFromGallery(){
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object: MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()){
                       val galleryIntent =  Intent(
                           Intent.ACTION_PICK,
                           MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                       )
                        startActivityForResult(galleryIntent, GALLERY)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }

            }).onSameThread()
            .check()
    }

    private fun takePhotoFromCamera(){
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object: MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()){
                        val galleryIntent =  Intent(MediaStore.ACTION_IMAGE_CAPTURE
                        )
                        startActivityForResult(galleryIntent, CAMERA)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }

            }).onSameThread()
            .check()
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton("GO TO SETTINGS"){
                _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap):Uri {

        val wrapper = ContextWrapper(applicationContext)

        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)

        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)

            //Compress bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG,100, stream)

            stream.flush()

            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

    companion object {
        private const val GALLERY = 1

        private const val CAMERA = 2

        private const val IMAGE_DIRECTORY = "HappyPlaceImages"
    }
}