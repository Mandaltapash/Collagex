package com.tods.project_olx.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
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
            Category("Furniture", R.drawable.ic_menu_gallery),
            Category("Electronics", R.drawable.ic_menu_camera),
            Category("Fashion", R.drawable.ic_menu_compass),
            Category("Books", R.drawable.ic_menu_agenda),
            Category("Sports", R.drawable.ic_menu_manage),
            Category("Free Zone", R.drawable.ic_menu_send),
            Category("Other", R.drawable.ic_menu_help),
            Category("Pets", android.R.drawable.btn_star)
        )

        binding.recyclerCategories.layoutManager = GridLayoutManager(this, 2)
        val adapter = CategoryAdapter(this, categories) { category ->
            val intent = Intent()
            intent.putExtra("selectedCategory", category.name)
            setResult(RESULT_OK, intent)
            finish()
        }
        binding.recyclerCategories.adapter = adapter
    }
}
