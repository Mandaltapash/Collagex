package com.tods.project_olx.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.tods.project_olx.R
import com.tods.project_olx.databinding.ActivityRegisterAdBinding
import com.tods.project_olx.model.Ad
import com.tods.project_olx.model.User
import dmax.dialog.SpotsDialog
import java.util.*


private const val RC_PERMISSIONS = 123

class RegisterAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterAdBinding
    private lateinit var ad: Ad
    private lateinit var dialog: android.app.AlertDialog
    private var listImages: MutableList<String> = mutableListOf()
    private var listUrlImages: MutableList<String> = mutableListOf()
    private var storage: FirebaseStorage = Firebase.storage
    private var loggedUser: FirebaseUser? = User().configCurrentUser()
    private lateinit var pickImageAd1: ActivityResultLauncher<String>
    private lateinit var pickImageAd2: ActivityResultLauncher<String>
    private lateinit var pickImageAd3: ActivityResultLauncher<String>

    private var selectedDistrict: String = ""
    private var selectedCollege: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configToolbar()
        configViewBinding()
        configLocaleCurrencyMask()

        pickImageAd1 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.imageAd1.setImageURI(it)
                listImages.add(it.toString())
                Log.d("RegisterAdActivity", "Image Ad1 picked: $it")
            } ?: run {
                Log.d("RegisterAdActivity", "Image Ad1 pick cancelled or uri is null.")
            }
        }
        pickImageAd2 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.imageAd2.setImageURI(it)
                listImages.add(it.toString())
                Log.d("RegisterAdActivity", "Image Ad2 picked: $it")
            } ?: run {
                Log.d("RegisterAdActivity", "Image Ad2 pick cancelled or uri is null.")
            }
        }
        pickImageAd3 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.imageAd3.setImageURI(it)
                listImages.add(it.toString())
                Log.d("RegisterAdActivity", "Image Ad3 picked: $it")
            } ?: run {
                Log.d("RegisterAdActivity", "Image Ad3 pick cancelled or uri is null.")
            }
        }

        requestPermission()
        configClickListenerAd1()
        configClickListenerAd2()
        configClickListenerAd3()
        configClickListenerRegisterAd()
        configSpinners()
    }

    private fun configClickListenerRegisterAd() {
        binding.buttonRegisterAd.setOnClickListener(View.OnClickListener {
            validateAdFields()
        })
    }

    private fun configClickListenerAd1() {
        binding.imageAd1.setOnClickListener(View.OnClickListener {
            pickImageAd1.launch("image/*")
        })
    }

    private fun configClickListenerAd2() {
        binding.imageAd2.setOnClickListener(View.OnClickListener {
            pickImageAd2.launch("image/*")
        })
    }

    private fun configClickListenerAd3() {
        binding.imageAd3.setOnClickListener(View.OnClickListener {
            pickImageAd3.launch("image/*")
        })
    }

    private fun configLocaleCurrencyMask() {
        val locale = Locale("en", "IN") // Changed to Indian Rupees
        binding.editValue.locale = locale
    }

    private fun configToolbar() {
        supportActionBar!!.title = "New Ad"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun configViewBinding() {
        binding = ActivityRegisterAdBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun requestPermission(){
        val permissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            Log.d("RegisterAdActivity", "Requesting permissions: ${permissionsToRequest.joinToString()}")
            ActivityCompat.requestPermissions(this, permissionsToRequest, RC_PERMISSIONS)
        } else {
            Log.d("RegisterAdActivity", "All permissions already granted.")
        }
    }

    private fun alertDialogPermission(){
        Log.d("RegisterAdActivity", "Showing alertDialogPermission: PERMISSION DENIED")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("PERMISSION DENIED")
        builder.setMessage("In order to set images for the ad, it is necessary to accept the permission")
        builder.setCancelable(false)
        builder.setPositiveButton("CONFIRM") { _, _ ->
            finish()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Image selection now handled by ActivityResultLauncher
    }

    private fun saveImagesStorage(url: String, totalImages: Int, i: Int){
        val imageRef: StorageReference = storage.getReference("images")
            .child("ads")
            .child(ad.id)
            .child("image$i.JPEG")
        val uploadTask: UploadTask = imageRef.putFile(Uri.parse(url))
        uploadTask.addOnSuccessListener(OnSuccessListener(){
            imageRef.downloadUrl.addOnCompleteListener(OnCompleteListener(){
                val firebaseUrl = it.result
                val convertedUrl: String = firebaseUrl.toString()
                listUrlImages.add(convertedUrl)
                if (totalImages == listUrlImages.size){
                    ad.adImages = listUrlImages
                    ad.save()
                    dialog.dismiss()
                    finish()
                }
            })
        }).addOnFailureListener(OnFailureListener(){
            Toast.makeText(this, "Error to upload image", Toast.LENGTH_SHORT).show()
        })
    }

    private fun saveAd(){
        configDialog()
        for (i in listImages.indices){
            val urlImage: String = listImages[i]
            val sizeList = listImages.size
            saveImagesStorage(urlImage, sizeList, i)
        }
    }

    private fun configDialog() {
        dialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Saving ad")
            .setCancelable(false)
            .build()
        dialog.show()
    }

    private fun configAd(): Ad{
        val title = binding.editTitle.text.toString()
        val description = binding.editDescription.text.toString()
        val value = binding.editValue.text.toString()
        val rawValue = binding.editValue.rawValue.toString()
        val category = binding.categoriesSpinner.selectedItem.toString()
        selectedDistrict = binding.districtSpinner.selectedItem.toString()
        selectedCollege = binding.collegeSpinner.selectedItem.toString()
        val reshapeTitle = title.replace(" ", "_")
        ad = Ad()
        ad.title = title
        ad.description = description
        ad.value = value
        ad.category = category
        ad.price = rawValue.toLongOrNull() ?: 0L
        ad.id = "${loggedUser!!.uid}_${rawValue}_${reshapeTitle}"
        ad.sellerName = loggedUser?.displayName ?: ""
        ad.district = selectedDistrict
        ad.collegeName = selectedCollege
        return ad
    }

    private fun validateAdFields(){
        ad = configAd()
        val rawValue = binding.editValue.rawValue.toString()
        if (listImages.size >= 3){ // Minimum 3 photos validation
            if (ad.title.isNotEmpty()){
                if (ad.description.isNotEmpty()){
                    if (rawValue.isNotEmpty() && rawValue != "0"){
                        if (ad.category.isNotEmpty()){
                            if (ad.district.isNotEmpty() && ad.district != "Select your district"){
                                if (ad.collegeName.isNotEmpty() && ad.collegeName != "Select your college"){
                                    saveAd()
                                } else {
                                    Toast.makeText(this, "Please select your college!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "Please select your district!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Please choose the ad category", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Value cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please choose at least 3 images for your ad", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configSpinners(){
        val categories: Array<out String> = resources.getStringArray(R.array.category)
        val adapterCategories: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categoriesSpinner.adapter = adapterCategories

        val districts: Array<out String> = resources.getStringArray(R.array.guwahati_districts)
        val adapterDistricts: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, districts)
        adapterDistricts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.districtSpinner.adapter = adapterDistricts

        binding.districtSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedDistrict = parent?.getItemAtPosition(position).toString()
                updateCollegeSpinner(selectedDistrict)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedDistrict = ""
                updateCollegeSpinner(selectedDistrict)
            }
        }
    }

    private fun updateCollegeSpinner(district: String) {
        val collegesArrayResId = when (district) {
            "Kamrup Metropolitan" -> R.array.colleges_kamrup_metropolitan
            "Kamrup Rural" -> R.array.colleges_kamrup_rural
            "Other" -> R.array.colleges_other
            else -> R.array.colleges_other // Default to colleges_other if district not found
        }
        val colleges: Array<out String> = resources.getStringArray(collegesArrayResId)
        val adapterColleges: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, colleges)
        adapterColleges.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.collegeSpinner.adapter = adapterColleges
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_PERMISSIONS) {
            var allPermissionsGranted = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allPermissionsGranted = false
                    Log.d("RegisterAdActivity", "Permission ${permissions[i]} denied.")
                } else {
                    Log.d("RegisterAdActivity", "Permission ${permissions[i]} granted.")
                }
            }
            if (!allPermissionsGranted) {
                Log.d("RegisterAdActivity", "Not all permissions granted. Showing alert dialog.")
                alertDialogPermission()
            } else {
                Log.d("RegisterAdActivity", "All permissions granted.")
            }
        }
    }
}