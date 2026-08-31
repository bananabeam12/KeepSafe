package com.example.keepsafe.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object AppPreferences {
    private const val PREF_NAME = "keepsafe_prefs"
    private const val KEY_ONBOARDING_SHOWN = "onboarding_shown"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isOnboardingShown(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ONBOARDING_SHOWN, false)
    }

    fun setOnboardingShown(context: Context, shown: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_ONBOARDING_SHOWN, shown) }
    }
}