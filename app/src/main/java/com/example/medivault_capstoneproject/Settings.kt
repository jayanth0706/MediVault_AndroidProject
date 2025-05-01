package com.example.medivault_capstoneproject

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class Settings : AppCompatActivity() {

    private lateinit var themeButton: Button
    private lateinit var logoutButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        themeButton = findViewById(R.id.settingstheme)
        logoutButton = findViewById(R.id.settingslogout)

        // Apply saved theme
        when (sharedPreferences.getString("theme", "light")) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        // Theme Button Click
        themeButton.setOnClickListener { view ->
            showThemeMenu(view)
        }

        // Logout Button Click
        logoutButton.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showThemeMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.theme_menu, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.light_theme -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    saveTheme("light")
                    recreate() // Optional: to apply theme immediately
                    true
                }
                R.id.dark_theme -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    saveTheme("dark")
                    recreate() // Optional: to apply theme immediately
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun saveTheme(theme: String) {
        with(sharedPreferences.edit()) {
            putString("theme", theme)
            apply()
        }
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")

        builder.setPositiveButton("Yes") { _, _ ->
            sharedPreferences.edit().clear().apply()
            val intent = Intent(this, LogInPage::class.java)
            startActivity(intent)
            finishAffinity()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
}
