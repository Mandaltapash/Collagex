package com.tods.project_olx.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tods.project_olx.R
import com.tods.project_olx.databinding.ActivityLanguageBinding
import com.tods.project_olx.helper.LocaleManager
import com.tods.project_olx.helper.ThemeManager

class LanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageBinding

    override fun attachBaseContext(newBase: Context) {
        val language = LocaleManager.getLocale(newBase)
        super.attachBaseContext(LocaleManager.setLocale(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(ThemeManager.getTheme(this))
        super.onCreate(savedInstanceState)
        
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCurrentLanguage()
        setupLanguageSelection()
        
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun loadCurrentLanguage() {
        val currentLanguage = LocaleManager.getLocale(this)
        
        when (currentLanguage) {
            "en_GB" -> binding.radioEnglishUK.isChecked = true
            "en_IN" -> binding.radioEnglishIndia.isChecked = true
            "hi" -> binding.radioHindi.isChecked = true
            "as" -> binding.radioAssamese.isChecked = true
            else -> binding.radioEnglishIndia.isChecked = true
        }
    }

    private fun setupLanguageSelection() {
        binding.radioEnglishUK.setOnClickListener {
            if (!binding.radioEnglishUK.isChecked) {
                binding.radioEnglishUK.isChecked = true
            }
            changeLanguage("en_GB", false)
        }
        
        binding.radioEnglishIndia.setOnClickListener {
            if (!binding.radioEnglishIndia.isChecked) {
                binding.radioEnglishIndia.isChecked = true
            }
            changeLanguage("en_IN", false)
        }
        
        binding.radioHindi.setOnClickListener {
            // Don't change selection, just show coming soon
            Toast.makeText(this, "Coming Soon - This language is not available yet", Toast.LENGTH_LONG).show()
            // Keep current selection
            loadCurrentLanguage()
        }
        
        binding.radioAssamese.setOnClickListener {
            // Don't change selection, just show coming soon
            Toast.makeText(this, "Coming Soon - This language is not available yet", Toast.LENGTH_LONG).show()
            // Keep current selection
            loadCurrentLanguage()
        }
    }

    private fun changeLanguage(languageCode: String, isComingSoon: Boolean) {
        // Only change language if it's not "Coming Soon"
        LocaleManager.setLocale(this, languageCode)
        
        // Restart the app to apply language change
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
