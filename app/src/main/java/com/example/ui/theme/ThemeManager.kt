package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val title: String, val description: String) {
    LIGHT("Light", "Clean white background with dark navy text"),
    DARK("Dark", "Deep dark navy background with crisp white text"),
    SYSTEM("System Default", "Automatically match your device's system appearance")
}

object ThemeManager {
    private const val PREFS_NAME = "sudhanihub_theme_preferences"
    private const val KEY_THEME_MODE = "sudhanihub_selected_theme_mode"

    private var sharedPreferences: SharedPreferences? = null
    private val _themeMode = MutableStateFlow(ThemeMode.LIGHT)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedMode = sharedPreferences?.getString(KEY_THEME_MODE, ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name
            _themeMode.value = try {
                ThemeMode.valueOf(savedMode)
            } catch (e: Exception) {
                ThemeMode.LIGHT
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        sharedPreferences?.edit()?.putString(KEY_THEME_MODE, mode.name)?.apply()
    }
}
