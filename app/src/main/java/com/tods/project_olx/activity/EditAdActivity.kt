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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import com.tods.project_olx.R
import com.tods.project_olx.databinding.ActivityEditAdBinding
import com.tods.project_olx.model.Ad
import com.tods.project_olx.model.User
import dmax.dialog.SpotsDialog
import java.util.*

private const val RC_PERMISSIONS = 123

class EditAdActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditAdBinding
    private lateinit var ad: Ad
    private lateinit var dialog: android.app.AlertDialog
    private var listImages: MutableList<String> = mutableListOf()
    private var listUrlImages: MutableList<String> = mutableListOf()
    private var storage: FirebaseStorage = Firebase.storage
    private var loggedUser = User().configCurrentUser()
    private lateinit var pickImageAd1: ActivityResultLauncher<String>
    private lateinit var pickImageAd2: ActivityResultLauncher<String>
    private lateinit var pickImageAd3: ActivityResultLauncher<String>
    private var imagesChanged = false

    private var selectedDistrict: String = ""
    private var selectedCollege: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configToolbar()
        configViewBinding()
        configLocaleCurrencyMask()
        loadAdData()

        pickImageAd1 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.imageAd1.setImageURI(it)
                updateImageInList(0, it.toString())
                imagesChanged = true
                Log.d("EditAdActivity", "Image Ad1 picked: $it")
            }
        }
        pickImageAd2 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.imageAd2.setImageURI(it)
                updateImageInList(1, it.toString())
                imagesChanged = true
                Log.d("EditAdActivity", "Image Ad2 picked: $it")
            }
        }
        pickImageAd3 = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.imageAd3.setImageURI(it)
                updateImageInList(2, it.toString())
                imagesChanged = true
                Log.d("EditAdActivity", "Image Ad3 picked: $it")
            }
        }

        requestPermission()
        configClickListenerAd1()
        configClickListenerAd2()
        configClickListenerAd3()
        configClickListenerSaveChanges()
        configSpinners()
    }

    private fun updateImageInList(index: Int, url: String) {
        while (listImages.size <= index) {
            listImages.add("")
        }
        listImages[index] = url
    }

    private fun loadAdData() {
        try {
            ad = intent.getSerializableExtra("adToEdit") as? Ad ?: run {
                Toast.makeText(this, "Error: Ad data not found", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            
            // Populate form with existing ad data
            binding.editTitle.setText(ad.title)
            binding.editDescription.setText(ad.description)
            binding.editValue.setText(ad.price.toString())
            
            // Load existing images
            listImages = ad.adImages.toMutableList()
            if (ad.adImages.size > 0) {
                Picasso.get().load(ad.adImages[0]).into(binding.imageAd1)
            }
            if (ad.adImages.size > 1) {
                Picasso.get().load(ad.adImages[1]).into(binding.imageAd2)
            }
            if (ad.adImages.size > 2) {
                Picasso.get().load(ad.adImages[2]).into(binding.imageAd3)
            }
            
            selectedDistrict = ad.district
            selectedCollege = ad.collegeName
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading ad data: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("EditAdActivity", "Error loading ad", e)
            finish()
        }
    }

    private fun configClickListenerSaveChanges() {
        binding.buttonSaveChanges.setOnClickListener(View.OnClickListener {
            validateAndUpdateAd()
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
        val locale = Locale("en", "IN")
        binding.editValue.locale = locale
    }

    private fun configToolbar() {
        supportActionBar!!.title = "Edit Ad"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun configViewBinding() {
        binding = ActivityEditAdBinding.inflate(layoutInflater)
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
            Log.d("EditAdActivity", "Requesting permissions: ${permissionsToRequest.joinToString()}")
            ActivityCompat.requestPermissions(this, permissionsToRequest, RC_PERMISSIONS)
        }
    }

    private fun updateAd(){
        if (imagesChanged) {
            // Upload new images
            configDialog()
            listUrlImages.clear()
            for (i in listImages.indices){
                val urlImage: String = listImages[i]
                if (urlImage.startsWith("http")) {
                    // Existing image, keep the URL
                    listUrlImages.add(urlImage)
                    if (listImages.size == listUrlImages.size) {
                        saveAdToDatabase()
                    }
                } else {
                    // New image, upload it
                    saveImagesStorage(urlImage, listImages.size, i)
                }
            }
        } else {
            // No image changes, just update the ad data
            saveAdToDatabase()
        }
    }

    private fun saveImagesStorage(url: String, totalImages: Int, i: Int){
        val imageRef: StorageReference = storage.getReference("images")
            .child("ads")
            .child(ad.id)
            .child("image$i.JPEG")
        val uploadTask: UploadTask = imageRef.putFile(Uri.parse(url))
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnCompleteListener {
                val firebaseUrl = it.result
                val convertedUrl: String = firebaseUrl.toString()
                listUrlImages.add(convertedUrl)
                if (totalImages == listUrlImages.size){
                    saveAdToDatabase()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    private fun saveAdToDatabase() {
        val title = binding.editTitle.text.toString()
        val description = binding.editDescription.text.toString()
        val rawValue = binding.editValue.rawValue.toString()
        val category = binding.categoriesSpinner.selectedItem.toString()
        selectedDistrict = binding.districtSpinner.selectedItem.toString()
        selectedCollege = binding.collegeSpinner.selectedItem.toString()

        // Update ad properties
        ad.title = title
        ad.description = description
        ad.value = "₹$rawValue"
        ad.category = category
        ad.price = rawValue.toLongOrNull() ?: 0L
        ad.district = selectedDistrict
        ad.collegeName = selectedCollege
        
        if (listUrlImages.isNotEmpty()) {
            ad.adImages = listUrlImages
        }

        // Use Ad.update() method to properly save to all Firebase locations
        try {
            ad.update()
            if (imagesChanged) {
                dialog.dismiss()
            }
            Toast.makeText(this, "Ad updated successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            if (imagesChanged) {
                dialog.dismiss()
            }
            Toast.makeText(this, "Failed to update ad: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("EditAdActivity", "Error updating ad", e)
        }
    }

    private fun configDialog() {
        dialog = SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Updating ad...")
            .setCancelable(false)
            .build()
        dialog.show()
    }

    private fun validateAndUpdateAd(){
        val title = binding.editTitle.text.toString()
        val description = binding.editDescription.text.toString()
        val rawValue = binding.editValue.rawValue.toString()
        val category = binding.categoriesSpinner.selectedItem.toString()
        selectedDistrict = binding.districtSpinner.selectedItem.toString()
        selectedCollege = binding.collegeSpinner.selectedItem.toString()

        if (listImages.size >= 3){
            if (title.isNotEmpty()){
                if (description.isNotEmpty()){
                    if (rawValue.isNotEmpty() && rawValue != "0"){
                        if (category.isNotEmpty()){
                            if (selectedDistrict.isNotEmpty() && selectedDistrict != "Select your district"){
                                if (selectedCollege.isNotEmpty() && selectedCollege != "Select your college"){
                                    updateAd()
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
            Toast.makeText(this, "Please keep at least 3 images for your ad", Toast.LENGTH_SHORT).show()
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

        //  Initialize college spinner with all Assam colleges
        updateCollegeSpinner("")

        binding.districtSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedDistrict = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedDistrict = ""
            }
        }
        
        // Set current values
        val categoryIndex = categories.indexOf(ad.category)
        if (categoryIndex >= 0) {
            binding.categoriesSpinner.setSelection(categoryIndex)
        }
        
        val districtIndex = districts.indexOf(ad.district)
        if (districtIndex >= 0) {
            binding.districtSpinner.setSelection(districtIndex)
        }
    }

    private fun updateCollegeSpinner(district: String) {
        val colleges: Array<out String> = resources.getStringArray(R.array.assam_colleges)
        val adapterColleges: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, colleges)
        adapterColleges.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.collegeSpinner.adapter = adapterColleges
        
        // Set current college
        val collegeIndex = colleges.indexOf(ad.collegeName)
        if (collegeIndex >= 0) {
            binding.collegeSpinner.setSelection(collegeIndex)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_PERMISSIONS) {
            var allPermissionsGranted = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allPermissionsGranted = false
                    Log.d("EditAdActivity", "Permission ${permissions[i]} denied.")
                }
            }
            if (!allPermissionsGranted) {
                Toast.makeText(this, "Permissions required to change images", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
