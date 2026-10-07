package com.example.prog_poe_final

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.prog_poe_final.models.User
import com.example.prog_poe_final.ui.HomeFragment
import com.example.prog_poe_final.ui.LoginActivity
import com.example.prog_poe_final.ui.NotificationFragment
import com.example.prog_poe_final.ui.SettingsFragment
import com.example.prog_poe_final.utils.CurrentUserHolder
import com.example.prog_poe_final.utils.LocaleHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val lang = prefs.getString("app_language", "en") ?: "en"
        val updated = LocaleHelper.applyLocale(newBase, lang)
        super.attachBaseContext(updated)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // Restore user session
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = prefs.getString("userId", null)
        val name = prefs.getString("name", null)
        val email = prefs.getString("email", null)

        if (CurrentUserHolder.user == null && userId != null && name != null && email != null) {
            CurrentUserHolder.user = User(userId.toInt(), name, email, "")
        }

        if (CurrentUserHolder.user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NotificationFragment())
                .commit()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_notification -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, NotificationFragment())
                        .commit()
                    true
                }

                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    true
                }

                R.id.nav_settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SettingsFragment())
                        .commit()
                    true
                }

                else -> false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }
}
