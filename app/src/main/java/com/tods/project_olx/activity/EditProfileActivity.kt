package com.tods.project_olx.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.tods.project_olx.databinding.ActivityEditProfileBinding
import com.tods.project_olx.model.User

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var userRef: DatabaseReference
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit profile"

        val currentUser = User().configCurrentUser()
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userId = currentUser.uid
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId!!)

        loadUser()
        configSaveButton()
    }

    private fun loadUser() {
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    binding.editName.setText(user.name)
                    binding.editEmail.setText(user.email)
                    binding.editPhotoUrl.setText(user.photoUrl)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun configSaveButton() {
        binding.buttonSaveProfile.setOnClickListener(View.OnClickListener {
            val name = binding.editName.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val photoUrl = binding.editPhotoUrl.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and email cannot be empty", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            val updates = HashMap<String, Any>()
            updates["name"] = name
            updates["email"] = email
            updates["photoUrl"] = photoUrl

            userRef.updateChildren(updates).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
