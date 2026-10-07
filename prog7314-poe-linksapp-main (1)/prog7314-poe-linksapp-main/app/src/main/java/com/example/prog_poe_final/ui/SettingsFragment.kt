package com.example.prog_poe_final.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.prog_poe_final.R
import com.example.prog_poe_final.api.ApiClient
import com.example.prog_poe_final.models.BioUpdateRequest
import com.example.prog_poe_final.utils.CurrentUserHolder
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsFragment : Fragment() {

    private lateinit var logoutButton: Button
    private lateinit var nameTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var languageSpinner: Spinner
    private lateinit var themeSwitch: SwitchCompat
    private var lastSelectedLang: String = "en"


    private var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Views
        logoutButton = view.findViewById(R.id.logout_button)
        nameTextView = view.findViewById(R.id.name)
        bioTextView = view.findViewById(R.id.bioTextView)
        languageSpinner = view.findViewById(R.id.languageSpinner)
        themeSwitch = view.findViewById(R.id.themeSwitch)

        val editButton = view.findViewById<MaterialButton>(R.id.editButton)
        editButton.setOnClickListener { showEditProfilePopup() }

        // Load user info
        val user = CurrentUserHolder.getCurrentUser()
        nameTextView.text = user?.name ?: "Unknown User"
        bioTextView.text = user?.bio ?: "No bio set"

        setupLanguageSpinner()
        setupThemeSwitch()

        logoutButton.setOnClickListener { logoutUser() }

        return view
    }

    // ------------------ EDIT PROFILE POPUP ------------------
    @SuppressLint("SetTextI18n")
    private fun showEditProfilePopup() {
        val dialogView = layoutInflater.inflate(R.layout.popup_edit_profile, null)

        val etBio = dialogView.findViewById<EditText>(R.id.etEditBio)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        val user = CurrentUserHolder.getCurrentUser()
        etBio.setText(user?.bio ?: "")

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val newBio = etBio.text.toString().trim()

            if (user == null) {
                Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateBioOnBackend(user.user_id!!, newBio)

            // Update memory + UI
            user.bio = newBio
            CurrentUserHolder.user = user
            bioTextView.text = newBio

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateBioOnBackend(userId: Int, newBio: String) {
        val request = BioUpdateRequest(userId, newBio)

        ApiClient.api.updateBio(request).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Bio updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update bio", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ------------------ LANGUAGE LOGIC ------------------
    private fun setupLanguageSpinner() {
        val languages = resources.getStringArray(R.array.language_list)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        val prefs = requireActivity().getSharedPreferences("UserPrefs", AppCompatActivity.MODE_PRIVATE)
        val saved = prefs.getString("app_language", "en")!!

        lastSelectedLang = saved

        languageSpinner.setSelection(
            when (saved) {
                "xh" -> 1
                "af" -> 2
                else -> 0
            }
        )

        // Detect user touching spinner — prevents auto-trigger loops
        languageSpinner.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                isFirstLoad = false   // ✔ ensures next selection is real user input
                v.performClick()
            }
            false
        }

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {

                val lang = when (pos) {
                    1 -> "xh"
                    2 -> "af"
                    else -> "en"
                }

                if (isFirstLoad) return
                if (lang == lastSelectedLang) return

                lastSelectedLang = lang
                setLocale(lang)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun setupThemeSwitch() {
        val prefs = requireActivity().getSharedPreferences("UserPrefs", AppCompatActivity.MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", false)

        themeSwitch.isChecked = isDark

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun setLocale(language: String) {
        val prefs = requireActivity().getSharedPreferences("UserPrefs", AppCompatActivity.MODE_PRIVATE)
        prefs.edit().putString("app_language", language).apply()

        requireActivity().runOnUiThread {
            requireActivity().recreate()
        }
    }


    // ------------------ LOGOUT ------------------
    private fun logoutUser() {
        val prefs = requireActivity().getSharedPreferences("UserPrefs", AppCompatActivity.MODE_PRIVATE)
        val editor = prefs.edit()

        val email = prefs.getString("email", null)
        val password = prefs.getString("password", null)

        editor.putBoolean("biometricEnabled", true)

        if (email != null && password != null) {
            editor.putString("email", email)
            editor.putString("password", password)
        }

        editor.remove("userId")
        editor.remove("name")
        editor.apply()

        CurrentUserHolder.clear()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
