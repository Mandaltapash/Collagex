package com.tods.project_olx.helper

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleManager {
    
    private const val PREF_LANGUAGE = "app_language"
    
    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        return updateResources(context, language)
    }
    
    fun getLocale(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString(PREF_LANGUAGE, "en") ?: "en"
    }
    
    private fun persist(context: Context, language: String) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_LANGUAGE, language).apply()
    }
    
    private fun updateResources(context: Context, language: String): Context {
        val locale = getLocaleFromLanguageCode(language)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
    
    private fun getLocaleFromLanguageCode(language: String): Locale {
        return when (language) {
            "en_GB" -> Locale("en", "GB")      // UK English
            "en_IN" -> Locale("en", "IN")      // Indian English
            "hi" -> Locale("hi", "IN")         // Hindi
            "as" -> Locale("as", "IN")         // Assamese
            else -> Locale.ENGLISH             // Default English
        }
    }
    
    fun getLanguageName(context: Context, languageCode: String): String {
        return when (languageCode) {
            "en_GB" -> "English (UK)"
            "en_IN" -> "English (India)"
            "hi" -> "हिन्दी (Hindi)"
            "as" -> "অসমীয়া (Assamese)"
            else -> "English"
        }
    }
}
