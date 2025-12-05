package com.tods.project_olx.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.tods.project_olx.R
import com.tods.project_olx.databinding.ActivityProfileBinding
import com.tods.project_olx.helper.ThemeManager
import com.tods.project_olx.model.User

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme before super.onCreate
        ThemeManager.applyTheme(ThemeManager.getTheme(this))
        super.onCreate(savedInstanceState)
        
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Please login to view profile", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        loadUserData()
        setupClickListeners()
        updateThemeDisplay()
        configBottomNav()
    }

    private fun loadUserData() {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId!!)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.let {
                    binding.textProfileName.text = it.name
                    binding.textProfileEmail.text = it.email
                    
                    if (it.photoUrl.isNotEmpty()) {
                        Picasso.get().load(it.photoUrl).into(binding.imageProfileAvatar)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupClickListeners() {
        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.menuEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.menuNotifications.setOnClickListener {
            // Notifications activity - coming soon
            Toast.makeText(this, "Notifications settings - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.menuLanguage.setOnClickListener {
            // Language activity - coming soon
            Toast.makeText(this, "Language settings - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.menuSecurity.setOnClickListener {
            // Security settings - coming soon
            Toast.makeText(this, "Security settings - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.menuTheme.setOnClickListener {
            toggleTheme()
        }

        binding.menuHelpSupport.setOnClickListener {
            Toast.makeText(this, "Help & Support - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.menuContactUs.setOnClickListener {
            Toast.makeText(this, "Contact Us - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        binding.menuPrivacyPolicy.setOnClickListener {
            // Privacy Policy activity - coming soon
            Toast.makeText(this, "Privacy Policy - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleTheme() {
        ThemeManager.toggleTheme(this)
        recreate() // Recreate activity to apply new theme
    }

    private fun updateThemeDisplay() {
        val isDark = ThemeManager.isDarkMode(this)
        binding.textCurrentTheme.text = if (isDark) {
            getString(R.string.dark_mode)
        } else {
            getString(R.string.light_mode)
        }
    }

    private fun configBottomNav() {
        binding.bottomNavigation.selectedItemId = R.id.nav_account
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_chats -> {
                    if (auth.currentUser == null) {
                        startActivity(Intent(this, LoginActivity::class.java))
                    } else {
                        startActivity(Intent(this, ChatListActivity::class.java))
                    }
                    true
                }
                R.id.nav_sell -> {
                    if (auth.currentUser == null) {
                        startActivity(Intent(this, LoginActivity::class.java))
                    } else {
                        startActivity(Intent(this, RegisterAddActivity::class.java))
                    }
                    true
                }
                R.id.nav_my_ads -> {
                    startActivity(Intent(this, MyAdsActivity::class.java))
                    true
                }
                R.id.nav_account -> true // Already here
                else -> false
            }
        }
    }
}
