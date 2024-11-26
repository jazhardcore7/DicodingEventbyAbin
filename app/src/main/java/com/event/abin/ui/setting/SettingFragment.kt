package com.event.abin.ui.setting

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.event.abin.data.workers.EventWorker
import com.event.abin.databinding.FragmentSettingBinding
import com.event.abin.ui.EventViewModel
import com.event.abin.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventViewModel by viewModels { ViewModelFactory.getInstance(requireContext()) }
    private lateinit var workManager: WorkManager
    private lateinit var periodicWorkRequest: PeriodicWorkRequest

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(requireContext(), "Notifications permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Notifications permission rejected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        workManager = WorkManager.getInstance(requireContext())
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES

        viewModel.getThemeSettings(requireContext()).observe(viewLifecycleOwner) { isDarkModeActive ->
            val shouldUseDarkMode = isDarkModeActive || isSystemDarkMode
            AppCompatDelegate.setDefaultNightMode(
                if (shouldUseDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )

            binding.switchTheme.setOnCheckedChangeListener(null)
            binding.switchTheme.isChecked = shouldUseDarkMode
            binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                lifecycleScope.launch {
                    viewModel.saveThemeSetting(isChecked)
                }
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val isSystemDarkMode = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES

        viewModel.getThemeSettings(requireContext()).observe(viewLifecycleOwner) { isDarkModeActive ->
            val shouldUseDarkMode = isDarkModeActive || isSystemDarkMode
            AppCompatDelegate.setDefaultNightMode(
                if (shouldUseDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            binding.switchTheme.setOnCheckedChangeListener(null)
            binding.switchTheme.isChecked = shouldUseDarkMode
            binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                lifecycleScope.launch {
                    viewModel.saveThemeSetting(isChecked)
                }
            }
        }

        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                viewModel.saveThemeSetting(isChecked)
            }
        }
        viewModel.getDailyReminderSetting()
            .observe(viewLifecycleOwner) { isReminderActive: Boolean ->
                if (isReminderActive) {
                    binding.switchDailyReminder.isChecked = true
                    if (Build.VERSION.SDK_INT >= 33) {
                        if (ContextCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } else {
                    binding.switchDailyReminder.isChecked = false
                }
            }
        binding.switchDailyReminder.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                viewModel.saveDailyReminderSetting(isChecked)
                if (isChecked) {
                    scheduleNearestEventReminder()
                } else {
                    cancelEventReminder()
                }
            }
        }

        viewModel.getDailyReminderSetting().observe(viewLifecycleOwner) { isReminderActive ->
            binding.switchDailyReminder.isChecked = isReminderActive
        }
    }

    private fun scheduleNearestEventReminder() {
        viewModel.getUpcomingEvent()
        viewModel.upcomingEvent.observe(viewLifecycleOwner) { listItems ->
            listItems.filter { event ->
                event.beginTime.let { beginTime ->
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(beginTime)?.time
                        ?.let { it >= System.currentTimeMillis() } ?: false
                }
            }.minByOrNull { event ->
                event.beginTime.let {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it)?.time
                } ?: Long.MAX_VALUE
            }?.let { nearestEvent ->
                val data = Data.Builder()
                    .putString("event_name", nearestEvent.name)
                    .putString("event_time", nearestEvent.beginTime)
                    .build()

                periodicWorkRequest = PeriodicWorkRequestBuilder<EventWorker>(1, TimeUnit.DAYS)
                    .setInputData(data)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()

                workManager.enqueueUniquePeriodicWork(
                    "DailyReminder",
                    androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                    periodicWorkRequest
                )
            }
        }
    }

    private fun cancelEventReminder() {
        workManager.cancelUniqueWork("DailyReminder")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
