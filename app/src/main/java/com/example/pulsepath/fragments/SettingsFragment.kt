package com.example.pulsepath.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.pulsepath.R
import com.example.pulsepath.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSettings()
        setupClickListeners()
        setupUserProfile()
    }

    private fun loadSettings() {
        // Load settings from SharedPreferences
        val sharedPrefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        binding.notificationsSwitch.isChecked = sharedPrefs.getBoolean("notifications_enabled", true)
        binding.darkModeSwitch.isChecked = sharedPrefs.getBoolean("dark_mode_enabled", false)
    }

    private fun setupUserProfile() {
        val dataManager = com.example.pulsepath.utils.DataManager(requireContext())
        
        // Load user profile data
        binding.settingsUserName.text = dataManager.getUserName()
        val streak = dataManager.getStreakDays()
        binding.settingsUserStreak.text = "🔥 $streak ${getString(com.example.pulsepath.R.string.day_streak)}"
        
        // Set up edit profile button
        binding.settingsEditProfileButton.setOnClickListener {
            showEditProfileDialog()
        }
    }

    private fun setupClickListeners() {
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveSetting("notifications_enabled", isChecked)
        }

        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveSetting("dark_mode_enabled", isChecked)
            // Show message that app restart is needed
            showRestartMessage()
        }

        binding.shareAppButton.setOnClickListener {
            shareApp()
        }

        binding.rateAppButton.setOnClickListener {
            // Open play store or show rating dialog
            showRatingDialog()
        }

        binding.feedbackButton.setOnClickListener {
            sendFeedback()
        }

        binding.aboutButton.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showEditProfileDialog() {
        val dataManager = com.example.pulsepath.utils.DataManager(requireContext())
        val currentName = dataManager.getUserName()
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(com.example.pulsepath.R.string.edit_profile))
            .setView(R.layout.dialog_edit_profile)
            .setPositiveButton("Save") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
        
        // Handle the dialog content
        val nameEditText = dialog.findViewById<android.widget.EditText>(R.id.editTextName)
        nameEditText?.setText(currentName)
        
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            val newName = nameEditText?.text?.toString()?.trim()
            if (!newName.isNullOrEmpty()) {
                dataManager.saveUserName(newName)
                binding.settingsUserName.text = newName
                dialog.dismiss()
            }
        }
    }

    private fun saveSetting(key: String, value: Boolean) {
        val sharedPrefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(key, value).apply()
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.check_out_dailybloom))
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text))
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }

    private fun showRatingDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.rate_app))
            .setMessage(getString(R.string.rate_app_message))
            .setPositiveButton(getString(R.string.rate_now)) { dialog, _ ->
                // Open play store or implement rating logic
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.later)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun sendFeedback() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("support@dailybloom.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.dailybloom_feedback))
        startActivity(Intent.createChooser(emailIntent, getString(R.string.send_feedback)))
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.about))
            .setMessage(getString(R.string.about_message))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showRestartMessage() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.restart_required))
            .setMessage(getString(R.string.restart_message))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}