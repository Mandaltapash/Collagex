package com.tods.project_olx.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tods.project_olx.databinding.ActivityPrivacyPolicyBinding
import com.tods.project_olx.helper.ThemeManager

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(ThemeManager.getTheme(this))
        super.onCreate(savedInstanceState)
        
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}
