package com.tods.project_olx.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tods.project_olx.databinding.ActivityNotificationsSettingsBinding
import com.tods.project_olx.helper.ThemeManager

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsSettingsBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(ThemeManager.getTheme(this))
        super.onCreate(savedInstanceState)
        
        binding = ActivityNotificationsSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

        setupToggles()
        
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun setupToggles() {
        // Common
        setupToggle("general_notification", binding.toggleGeneralNotification.root, "General Notification")
        setupToggle("sound", binding.toggleSound.root, "Sound")
        setupToggle("vibrate", binding.toggleVibrate.root, "Vibrate")
        
        // System & Services
        setupToggle("app_updates", binding.toggleAppUpdates.root, "App updates")
        setupToggle("bill_reminder", binding.toggleBillReminder.root, "Bill Reminder")
        setupToggle("promotion", binding.togglePromotion.root, "Promotion")
        setupToggle("discount_available", binding.toggleDiscountAvailable.root, "Discount Available")
        setupToggle("payment_request", binding.togglePaymentRequest.root, "Payment Request")
        
        // Others
        setupToggle("new_service", binding.toggleNewServiceAvailable.root, "New Service Available")
        setupToggle("new_tips", binding.toggleNewTipsAvailable.root, "New Tips Available")
    }

    private fun setupToggle(key: String, toggleView: android.view.View, label: String) {
        val textView = toggleView.findViewById<android.widget.TextView>(com.tods.project_olx.R.id.textNotificationLabel)
        val switchView = toggleView.findViewById<androidx.appcompat.widget.SwitchCompat>(com.tods.project_olx.R.id.switchNotification)
        
        textView.text = label
        switchView.isChecked = prefs.getBoolean(key, true)
        
        switchView.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(key, isChecked).apply()
        }
    }
}
