package com.tods.project_olx.activity

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.tods.project_olx.databinding.ActivityEditProfileBinding
import com.tods.project_olx.helper.ThemeManager
import com.tods.project_olx.model.User

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private var currentUser: User? = null
    private var selectedImageUri: Uri? = null
    private var currentDistrict: String = ""

    // Assam Districts
    private val assamDistricts = arrayOf(
        "Baksa", "Barpeta", "Biswanath", "Bongaigaon", "Cachar",
        "Charaideo", "Chirang", "Darrang", "Dhemaji", "Dhubri",
        "Dibrugarh", "Dima Hasao", "Goalpara", "Golaghat", "Hailakandi",
        "Hojai", "Jorhat", "Kamrup", "Kamrup Metropolitan", "Karbi Anglong",
        "Karimganj", "Kokrajhar", "Lakhimpur", "Majuli", "Morigaon",
        "Nagaon", "Nalbari", "Ridang", "Sonitpur", "South Salmara-Mankachar",
        "Tinsukia", "Udalguri", "West Karbi Anglong"
    )

    // District-wise Colleges Map
    private val districtColleges = mapOf(
        "Kamrup Metropolitan" to arrayOf(
            "Indian Institute of Technology Guwahati (IIT Guwahati)",
            "Cotton University",
            "Gauhati University",
            "Assam Engineering College",
            "Assam Don Bosco University",
            "Royal Global University",
            "Girijananda Chowdhury Institute of Management and Technology",
            "B. Borooah College",
            "Cotton College",
            "Handique Girls' College",
            "Pragjyotish College",
            "Arya Vidyapeeth College",
            "Pandu College",
            "Guwahati Commerce College"
        ),
        "Sonitpur" to arrayOf(
            "Tezpur University",
            "Tezpur College",
            "Darrang College"
        ),
        "Dibrugarh" to arrayOf(
            "Dibrugarh University",
            "Dibrugarh University Institute of Engineering and Technology",
            "Assam Medical College"
        ),
        "Jorhat" to arrayOf(
            "Jorhat Engineering College",
            "Assam Agricultural University",
            "J.B. College, Jorhat",
            "Jorhat Medical College and Hospital",
            "Mariani College",
            "J.M. College, Golaghat"
        ),
        "Cachar" to arrayOf(
            "National Institute of Technology Silchar (NIT Silchar)",
            "Assam University, Silchar",
            "Gurucharan College, Silchar"
        ),
        "Nagaon" to arrayOf(
            "Nowgong College",
            "Nagaon Commerce College",
            "Dhing College",
            "Haibargaon College"
        ),
        "Barpeta" to arrayOf(
            "Barpeta College",
            "B.H. College, Howly"
        ),
        "Nalbari" to arrayOf(
            "Nalbari College",
            "Nalbari Commerce College"
        ),
        "Golaghat" to arrayOf(
            "Golaghat Commerce College",
            "J.M. College, Golaghat"
        ),
        "Kokrajhar" to arrayOf(
            "Kokrajhar Government College",
            "Bodoland University"
        ),
        "Dhemaji" to arrayOf(
            "Dhemaji College",
            "Dhakuakhana College"
        ),
        "Lakhimpur" to arrayOf(
            "North Lakhimpur College",
            "Lakhimpur Commerce College",
            "Lakhimpur Girls' College"
        ),
        "Majuli" to arrayOf(
            "Majuli College"
        ),
        "Sibsagar" to arrayOf(
            "Sibsagar College",
            "Sivasagar Girls' College"
        ),
        "Tinsukia" to arrayOf(
            "Tinsukia College",
            "Tingkhong College",
            "Margherita College"
        ),
        "Kamrup" to arrayOf(
            "Rangia College",
            "Boko College",
            "Pub Kamrup College"
        ),
        "Morigaon" to arrayOf(
            "Morigaon College",
            "Jagiroad College"
        ),
        "Darrang" to arrayOf(
            "Mangaldai College",
            "Sipajhar College"
        ),
        "Goalpara" to arrayOf(
            "Goalpara College",
            "Dudhnoi College"
        ),
        "Bongaigaon" to arrayOf(
            "Bongaigaon College",
            "Abhayapuri College"
        ),
        "Chirang" to arrayOf(
            "Basugaon College",
            "Bijni College"
        ),
        "Baksa" to arrayOf(
            "Tamulpur College",
            "Barama College"
        ),
        "Udalguri" to arrayOf(
            "Udalguri College",
            "Tangla College"
        ),
        "Dhubri" to arrayOf(
            "Dhubri College",
            "Bilasipara College",
            "Gauripur College"
        ),
        "Karbi Anglong" to arrayOf(
            "Diphu Government College",
            "Diphu Medical College",
            "Donbosco College, Diphu"
        ),
        "Dima Hasao" to arrayOf(
            "Haflong Government College"
        ),
        "Hailakandi" to arrayOf(
            "Hailakandi College"
        ),
        "Karimganj" to arrayOf(
            "Karimganj College",
            "Guru Charan College, Karimganj"
        ),
        "Hojai" to arrayOf(
            "Hojai Polytechnic",
            "Lanka Mahavidyalaya"
        ),
        "Charaideo" to arrayOf(
            "Sonari College"
        ),
        "Biswanath" to arrayOf(
            "Biswanath College"
        ),
        "West Karbi Anglong" to arrayOf(
            "Hamren College"
        ),
        "South Salmara-Mankachar" to arrayOf(
            "Mankachar College"
        ),
        "Ridang" to arrayOf(
            "Marigaon College"
        )
    )

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imageProfilePicture.setImageURI(it)
            autoSaveProfile()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(ThemeManager.getTheme(this))
        super.onCreate(savedInstanceState)
        
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupDistrictDropdown()
        setupValidation()
        setupAutoSave()
        setupClickListeners()
        loadCurrentUserData()
    }

    private fun setupDistrictDropdown() {
        val districtAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, assamDistricts)
        binding.spinnerDistrict.setAdapter(districtAdapter)
        
        binding.spinnerDistrict.setOnItemClickListener { _, _, position, _ ->
            currentDistrict = assamDistricts[position]
            updateCollegeList(currentDistrict)
            autoSaveProfile()
        }
    }

    private fun updateCollegeList(district: String) {
        val colleges = districtColleges[district] ?: arrayOf()
        val collegeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, colleges)
        binding.autoCompleteCollege.setAdapter(collegeAdapter)
        binding.autoCompleteCollege.setText("", false)
    }

    private fun setupValidation() {
        // Email validation
        binding.editTextEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                when {
                    email.isEmpty() -> {
                        binding.emailInputLayout.error = null
                    }
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        binding.emailInputLayout.error = "Invalid email"
                    }
                    else -> {
                        binding.emailInputLayout.error = null
                    }
                }
            }
        })

        // Phone validation
        binding.editTextPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString().trim()
                when {
                    phone.isEmpty() -> {
                        binding.phoneInputLayout.error = null
                    }
                    phone.length < 10 -> {
                        binding.phoneInputLayout.error = "Invalid phone number"
                    }
                    phone.length == 10 -> {
                        binding.phoneInputLayout.error = null
                    }
                }
            }
        })
    }

    private fun setupAutoSave() {
        val autoSaveWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                autoSaveProfile()
            }
        }

        binding.editTextName.addTextChangedListener(autoSaveWatcher)
        binding.editTextEmail.addTextChangedListener(autoSaveWatcher)
        binding.editTextPhone.addTextChangedListener(autoSaveWatcher)
        
        binding.autoCompleteCollege.setOnItemClickListener { _, _, _, _ ->
            autoSaveProfile()
        }
    }

    private fun setupClickListeners() {
        binding.buttonEditPhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun loadCurrentUserData() {
        val userId = auth.currentUser?.uid ?: return

        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                currentUser?.let { user ->
                    binding.editTextName.setText(user.name)
                    binding.editTextEmail.setText(user.email)
                    binding.editTextPhone.setText(user.phone)
                    
                    if (user.state.isNotEmpty()) {
                        binding.spinnerDistrict.setText(user.state, false)
                        currentDistrict = user.state
                        updateCollegeList(currentDistrict)
                    }
                    
                    if (user.collegeName.isNotEmpty()) {
                        binding.autoCompleteCollege.setText(user.collegeName, false)
                    }
                    
                    if (user.photoUrl.isNotEmpty()) {
                        Picasso.get().load(user.photoUrl).into(binding.imageProfilePicture)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun autoSaveProfile() {
        val userId = auth.currentUser?.uid ?: return

        val name = binding.editTextName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()
        val district = binding.spinnerDistrict.text.toString().trim()
        val collegeName = binding.autoCompleteCollege.text.toString().trim()

        // Only auto-save if all required fields are valid
        if (name.isEmpty() || email.isEmpty() || phone.length != 10) {
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return
        }

        if (selectedImageUri != null) {
            uploadImageAndSaveProfile(userId, name, email, phone, district, collegeName)
        } else {
            saveProfileToDatabase(userId, name, email, phone, district, collegeName, currentUser?.photoUrl ?: "")
        }
    }

    private fun uploadImageAndSaveProfile(userId: String, name: String, email: String, phone: String, district: String, collegeName: String) {
        val storageRef = FirebaseStorage.getInstance().reference
            .child("profile_images")
            .child("$userId.jpg")

        selectedImageUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveProfileToDatabase(userId, name, email, phone, district, collegeName, downloadUri.toString())
                        selectedImageUri = null
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfileToDatabase(userId: String, name: String, email: String, phone: String, district: String, collegeName: String, photoUrl: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        val updates = hashMapOf<String, Any>(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "state" to district,
            "collegeName" to collegeName,
            "photoUrl" to photoUrl
        )

        userRef.updateChildren(updates)
            .addOnSuccessListener {
                // Auto-saved silently - no toast notification
            }
            .addOnFailureListener { exception ->
                // Silent failure - could add logging here if needed
            }
    }
}
