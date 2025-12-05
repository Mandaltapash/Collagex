package com.tods.project_olx.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.tods.project_olx.R
import com.tods.project_olx.adapter.CategoryAdapter
import com.tods.project_olx.databinding.ActivityCategoriesBinding

class CategoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Categories"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val categories = listOf(
            Category("Furniture", R.drawable.furniture),
            Category("Electronics", R.drawable.electronics),
            Category("Fashion", R.drawable.fashion),
            Category("Books", R.drawable.books),
            Category("Others", R.drawable.others),
            Category("Pets", R.drawable.pets),
            Category("Freezone", R.drawable.freezone),
            Category("Sports", R.drawable.sports)
        )

        binding.recyclerCategories.layoutManager = GridLayoutManager(this, 2)
        val adapter = CategoryAdapter(this, categories) { category ->
            val intent = Intent()
            intent.putExtra("selectedCategory", category.name)
            setResult(RESULT_OK, intent)
            finish()
        }
        binding.recyclerCategories.adapter = adapter
        configBottomNav()
    }

    private fun configBottomNav() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Just finish to go back to MainActivity instead of starting a new one
                    finish()
                    true
                }
                R.id.nav_chats -> {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                    } else {
                        startActivity(Intent(applicationContext, ChatListActivity::class.java))
                    }
                    true
                }
                R.id.nav_sell -> {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                    } else {
                        startActivity(Intent(applicationContext, RegisterAddActivity::class.java))
                    }
                    true
                }
                R.id.nav_my_ads -> {
                    startActivity(Intent(applicationContext, MyAdsActivity::class.java))
                    true
                }
                R.id.nav_account -> {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                    } else {
                        startActivity(Intent(applicationContext, ProfileActivity::class.java))
                    }
                    true
                }
                else -> false
            }
        }
    }
}
