package com.example.imilipocket.ui.settings

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.imilipocket.R
import com.example.imilipocket.data.PreferenceManager
import com.example.imilipocket.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val preferenceManager = PreferenceManager(requireContext())
        viewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(preferenceManager)
        )[SettingsViewModel::class.java]

        setupCurrencyDropdown()
        setupThemeSelection()
        setupButtons()
        observeViewModel()
    }

    private fun setupCurrencyDropdown() {
        val currencies = resources.getStringArray(R.array.currencies)
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, currencies)
        (binding.spinnerCurrency as? AutoCompleteTextView)?.setAdapter(adapter)

        viewModel.selectedCurrency.observe(viewLifecycleOwner) { currency ->
            if (currency != binding.spinnerCurrency.text.toString()) {
                binding.spinnerCurrency.setText(currency, false)
            }
        }

        binding.spinnerCurrency.setOnItemClickListener { _, _, position, _ ->
            val selectedCurrency = currencies[position]
            if (selectedCurrency != viewModel.selectedCurrency.value) {
                viewModel.updateCurrency(selectedCurrency)
            }
        }
    }

    private fun setupThemeSelection() {
        binding.cardTheme.setOnClickListener {
            showThemeSelectionDialog()
        }

        viewModel.themeMode.observe(viewLifecycleOwner) { themeMode ->
            binding.tvCurrentTheme.text = when (themeMode) {
                ThemeMode.LIGHT -> getString(R.string.theme_light)
                ThemeMode.DARK -> getString(R.string.theme_dark)
                ThemeMode.SYSTEM -> getString(R.string.theme_system)
            }
        }
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system)
        )
        
        val currentTheme = viewModel.themeMode.value ?: ThemeMode.SYSTEM
        val currentSelection = when (currentTheme) {
            ThemeMode.LIGHT -> 0
            ThemeMode.DARK -> 1
            ThemeMode.SYSTEM -> 2
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.theme))
            .setSingleChoiceItems(themes, currentSelection) { dialog, which ->
                val selectedTheme = when (which) {
                    0 -> ThemeMode.LIGHT
                    1 -> ThemeMode.DARK
                    else -> ThemeMode.SYSTEM
                }
                viewModel.setThemeMode(selectedTheme)
                dialog.dismiss()
            }
            .show()
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            val selectedCurrency = binding.spinnerCurrency.text.toString()
            viewModel.updateCurrency(selectedCurrency)
            Toast.makeText(context, getString(R.string.msg_settings_saved), Toast.LENGTH_SHORT).show()
        }

        binding.btnBackup.setOnClickListener {
            if (checkStoragePermissions()) {
                showBackupConfirmationDialog()
            } else {
                requestStoragePermissions()
            }
        }

        binding.btnRestore.setOnClickListener {
            if (checkStoragePermissions()) {
                showRestoreConfirmationDialog()
            } else {
                requestStoragePermissions()
            }
        }
    }

    private fun checkStoragePermissions(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestStoragePermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with backup/restore
                    Toast.makeText(context, "Storage permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Storage permission required for backup/restore", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showBackupConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.settings_backup))
            .setMessage(getString(R.string.backup_confirmation_message))
            .setPositiveButton(getString(R.string.backup)) { dialog, _ ->
                viewModel.createBackup()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showRestoreConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.settings_restore))
            .setMessage(getString(R.string.restore_confirmation_message))
            .setPositiveButton(getString(R.string.restore)) { dialog, _ ->
                viewModel.restoreFromLatestBackup()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun observeViewModel() {
        viewModel.themeMode.observe(viewLifecycleOwner) { themeMode ->
            binding.tvCurrentTheme.text = when (themeMode) {
                ThemeMode.LIGHT -> getString(R.string.theme_light)
                ThemeMode.DARK -> getString(R.string.theme_dark)
                ThemeMode.SYSTEM -> getString(R.string.theme_system)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnBackup.isEnabled = !isLoading
            binding.btnRestore.isEnabled = !isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.backupStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
        }

        viewModel.restoreStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
            if (status.startsWith("Data restored successfully")) {
                // Refresh the activity to reflect restored data
                requireActivity().recreate()
            }
        }

        viewModel.themeChanged.observe(viewLifecycleOwner) { changed ->
            if (changed) {
                requireActivity().recreate()
            }
        }
    }

    companion object {
        private const val STORAGE_PERMISSION_CODE = 100
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 