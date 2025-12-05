package com.tods.project_olx.activity

import android.content.Intent
import android.content.Context
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
import com.tods.project_olx.helper.LocaleManager
import com.tods.project_olx.model.User

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null

    override fun attachBaseContext(newBase: Context) {
        val language = LocaleManager.getLocale(newBase)
        super.attachBaseContext(LocaleManager.setLocale(newBase, language))
    }

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
        // Edit button on profile picture (pencil icon)
        binding.buttonEditAvatar.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.menuEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.menuNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        binding.menuLanguage.setOnClickListener {
            startActivity(Intent(this, LanguageActivity::class.java))
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

        binding.buttonLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.menuPrivacyPolicy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }
    }

    private fun showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, _ ->
                logout()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun logout() {
        // Sign out from Firebase
        auth.signOut()
        
        // Clear any saved preferences if needed
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        
        // Redirect to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
