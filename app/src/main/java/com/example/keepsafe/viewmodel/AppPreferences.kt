package com.example.keepsafe.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object AppPreferences {
    private const val PREF_NAME = "keepsafe_prefs"
    private const val KEY_ONBOARDING_SHOWN = "onboarding_shown"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_FIRST_NAME = "first_name"
    private const val KEY_LAST_NAME = "last_name"
    private const val KEY_EMAIL = "email"
    private const val KEY_REG_EMAIL = "reg_email"
    private const val KEY_REG_PASSWORD = "reg_password"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isOnboardingShown(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ONBOARDING_SHOWN, false)
    }

    fun setOnboardingShown(context: Context, shown: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_ONBOARDING_SHOWN, shown) }
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserFirstName(context: Context): String {
        return getPrefs(context).getString(KEY_FIRST_NAME, "User") ?: "User"
    }

    fun getUserLastName(context: Context): String {
        return getPrefs(context).getString(KEY_LAST_NAME, "") ?: ""
    }

    fun getUserEmail(context: Context): String {
        return getPrefs(context).getString(KEY_EMAIL, "user@keepsafe.app") ?: "user@keepsafe.app"
    }

    fun registerUser(context: Context, firstName: String, lastName: String, email: String, pass: String) {
        getPrefs(context).edit {
            putString(KEY_REG_EMAIL, email.lowercase().trim())
            putString(KEY_REG_PASSWORD, pass)
            putString(KEY_FIRST_NAME, firstName.ifBlank { "User" })
            putString(KEY_LAST_NAME, lastName)
            putString(KEY_EMAIL, email.lowercase().trim())
            putBoolean(KEY_IS_LOGGED_IN, true)
        }
    }

    fun validateLogin(context: Context, email: String, pass: String): LoginResult {
        val prefs = getPrefs(context)
        val savedEmail = prefs.getString(KEY_REG_EMAIL, "") ?: ""
        val savedPass = prefs.getString(KEY_REG_PASSWORD, "") ?: ""

        if (savedEmail.isBlank()) {
            return LoginResult.NotRegistered
        }

        if (email.lowercase().trim() != savedEmail || pass != savedPass) {
            return LoginResult.WrongCredentials
        }

        prefs.edit { putBoolean(KEY_IS_LOGGED_IN, true) }
        return LoginResult.Success
    }

    fun saveUserProfile(context: Context, firstName: String, lastName: String, email: String) {
        getPrefs(context).edit {
            putString(KEY_FIRST_NAME, firstName.ifBlank { "User" })
            putString(KEY_LAST_NAME, lastName)
            putString(KEY_EMAIL, email.ifBlank { "user@keepsafe.app" })
            putBoolean(KEY_IS_LOGGED_IN, true)
        }
    }

    fun clearUserSession(context: Context) {
        getPrefs(context).edit {
            remove(KEY_IS_LOGGED_IN)
        }
    }
}

enum class LoginResult {
    Success,
    NotRegistered,
    WrongCredentials
}
