package com.example.settingsactivity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.settingsactivity.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")


class SettingsActivity : AppCompatActivity() {

    companion object {
        const val VALUE_VM = "value_vm"
        const val KEY_BLUETOOTH = "key_bluetooth"
        const val KEY_VIBRATION = "key_vibration"
        const val KEY_DARK_MODE = "key_dark_mode"
    }

    private lateinit var binding: ActivityMainBinding
    private var firstTime : Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        CoroutineScope(Dispatchers.IO).launch {
            getSettings().filter { firstTime }.collect{ settingsModel ->

                if(settingsModel != null) {

                    runOnUiThread {
                        binding.switchVibration.isChecked = settingsModel.vibration
                        binding.switchBluetooth.isChecked = settingsModel.bluetooth
                        binding.switchDarkMode.isChecked = settingsModel.darkMode
                        binding.rangeSliderVolume.setValues(settingsModel.volume.toFloat())
                        firstTime = !firstTime
                    }

                }

            }
        }


        initUi()
    }

    private fun initUi() {
        binding.rangeSliderVolume.addOnChangeListener { _, value, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                saveVolume(value.toInt())
            }

        }


        binding.switchBluetooth.setOnCheckedChangeListener {
                _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveOptions(KEY_BLUETOOTH, value)
            }
        }


        binding.switchVibration.setOnCheckedChangeListener {
                _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveOptions(KEY_VIBRATION, value)
            }
        }


        binding.switchDarkMode.setOnCheckedChangeListener {
                _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveOptions(KEY_DARK_MODE, value)

                runOnUiThread {
                    if(value) {
                        enableDarkMode()
                    } else {
                        disableDarkMode()
                    }
                }
            }
        }

    }

    private suspend fun saveVolume(value: Int) {
        dataStore.edit { volume ->
            volume[intPreferencesKey(VALUE_VM)]
        }
    }


    private suspend fun saveOptions(key: String, value: Boolean) {
        dataStore.edit { preferences -> preferences[booleanPreferencesKey(key)] = value }
    }

    private fun getSettings() : Flow<SettingsModel?> {
        return dataStore.data.map { preferences ->

            SettingsModel(
                volume = preferences[intPreferencesKey(VALUE_VM)] ?: 50,
                bluetooth = preferences[booleanPreferencesKey(KEY_BLUETOOTH)] ?: true,
                vibration = preferences[booleanPreferencesKey(KEY_VIBRATION)] ?: true,
                darkMode = preferences[booleanPreferencesKey(KEY_DARK_MODE)] ?: false
            )


        }
    }

    private fun enableDarkMode() {
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        delegate.applyDayNight()
    }

    private fun disableDarkMode() {
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        delegate.applyDayNight()
    }


}