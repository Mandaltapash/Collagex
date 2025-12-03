package com.tods.project_olx.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import com.tods.project_olx.adapter.AdapterAd
import com.tods.project_olx.databinding.ActivityProfileBinding
import com.tods.project_olx.helper.RecyclerItemClickListener
import com.tods.project_olx.model.Ad
import com.tods.project_olx.model.User

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var userRef: DatabaseReference

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"

        userId = intent.getStringExtra("userId") ?: User().configCurrentUser()?.uid

        if (userId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val currentUserId = User().configCurrentUser()?.uid
        if (currentUserId == userId) {
            binding.buttonEditProfile.visibility = View.VISIBLE
            binding.buttonPurchaseHistory.visibility = View.VISIBLE
            binding.buttonLogout.visibility = View.VISIBLE // Make Logout button visible

            binding.buttonEditProfile.setOnClickListener {
                val intent = android.content.Intent(this, EditProfileActivity::class.java)
                startActivity(intent)
            }
            binding.buttonPurchaseHistory.setOnClickListener {
                val intent = Intent(this, PurchaseHistoryActivity::class.java)
                startActivity(intent)
            }
            binding.buttonLogout.setOnClickListener {
                FirebaseAuth.getInstance().signOut() // Logout from Firebase
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK) // Clear back stack
                startActivity(intent)
                finish()
            }
        } else {
            binding.buttonEditProfile.visibility = View.GONE
            binding.buttonPurchaseHistory.visibility = View.GONE
            binding.buttonLogout.visibility = View.GONE // Hide Logout button
        }

        loadUser()
    }

    private fun loadUser() {
        userRef = FirebaseDatabase.getInstance().getReference("users")
            .child(userId!!)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    binding.textProfileName.text = user.name
                    binding.textProfileEmail.text = user.email

                    val ratingText = if (user.ratingCount > 0) {
                        String.format("%.1f (%d reviews)", user.ratingAverage, user.ratingCount)
                    } else {
                        "No reviews yet"
                    }
                    binding.textProfileRating.text = ratingText

                    if (user.photoUrl.isNotEmpty()) {
                        Picasso.get().load(user.photoUrl).into(binding.imageProfileAvatar)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Failed to load user", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
