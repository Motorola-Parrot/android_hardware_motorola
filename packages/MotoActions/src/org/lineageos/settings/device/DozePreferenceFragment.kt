/*
 * SPDX-FileCopyrightText: 2015 The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.device

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.widget.MainSwitchPreference
import com.android.settingslib.widget.SettingsBasePreferenceFragment

class DozePreferenceFragment :
    SettingsBasePreferenceFragment(), Preference.OnPreferenceChangeListener {

    private lateinit var alwaysOnDisplayPreference: SwitchPreferenceCompat
    private lateinit var alwaysOnWallpaperPreference: SwitchPreferenceCompat
    private lateinit var handwavePreference: SwitchPreferenceCompat
    private lateinit var pickUpPreference: SwitchPreferenceCompat
    private lateinit var pocketPreference: SwitchPreferenceCompat

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.doze_panel, rootKey)

        val dozeEnabled = MotoActionsSettings.isDozeEnabled(requireActivity())

        val switchBar: MainSwitchPreference = findPreference(MotoActionsSettings.DOZE_ENABLE)!!
        switchBar.onPreferenceChangeListener = this
        switchBar.isChecked = dozeEnabled

        alwaysOnDisplayPreference = findPreference(MotoActionsSettings.ALWAYS_ON_DISPLAY)!!
        alwaysOnDisplayPreference.isEnabled = dozeEnabled
        alwaysOnDisplayPreference.isChecked =
            MotoActionsSettings.isAlwaysOnEnabled(requireActivity())
        alwaysOnDisplayPreference.onPreferenceChangeListener = this

        // The AOD switch inverts dependencies for gestures, so enable wallpaper explicitly.
        alwaysOnWallpaperPreference = findPreference(MotoActionsSettings.ALWAYS_ON_WALLPAPER)!!
        alwaysOnWallpaperPreference.isEnabled = dozeEnabled && alwaysOnDisplayPreference.isChecked
        alwaysOnWallpaperPreference.isChecked =
            MotoActionsSettings.isAlwaysOnWallpaperEnabled(requireActivity())
        alwaysOnWallpaperPreference.onPreferenceChangeListener = this

        handwavePreference = findPreference(MotoActionsSettings.GESTURE_IR_WAKEUP_KEY)!!
        handwavePreference.isEnabled = dozeEnabled
        handwavePreference.onPreferenceChangeListener = this

        pickUpPreference = findPreference(MotoActionsSettings.GESTURE_PICK_UP_KEY)!!
        pickUpPreference.isEnabled = dozeEnabled
        pickUpPreference.onPreferenceChangeListener = this

        pocketPreference = findPreference(MotoActionsSettings.GESTURE_POCKET_KEY)!!
        pocketPreference.isEnabled = dozeEnabled
        pocketPreference.onPreferenceChangeListener = this

        if (!MotoActionsSettings.alwaysOnWallpaperAvailable(requireActivity())) {
            preferenceScreen.removePreference(alwaysOnWallpaperPreference)
        }

        if (!MotoActionsSettings.alwaysOnDisplayAvailable(requireActivity())) {
            preferenceScreen.removePreference(alwaysOnDisplayPreference)
        } else {
            val ambientDisplayCategory: PreferenceCategory = findPreference("ambient_display_key")!!
            ambientDisplayCategory.dependency = MotoActionsSettings.ALWAYS_ON_DISPLAY
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val isChecked = newValue as Boolean

        when (preference.key) {
            MotoActionsSettings.ALWAYS_ON_DISPLAY -> {
                MotoActionsSettings.enableAlwaysOn(requireActivity(), isChecked)
                alwaysOnWallpaperPreference.isEnabled = isChecked
            }

            MotoActionsSettings.ALWAYS_ON_WALLPAPER -> {
                return MotoActionsSettings.enableAlwaysOnWallpaper(requireActivity(), isChecked)
            }

            MotoActionsSettings.DOZE_ENABLE -> {
                MotoActionsSettings.enableDoze(requireActivity(), isChecked)

                if (!isChecked) {
                    MotoActionsSettings.enableAlwaysOn(requireActivity(), false)
                    alwaysOnDisplayPreference.isChecked = false
                }

                alwaysOnDisplayPreference.isEnabled = isChecked
                alwaysOnWallpaperPreference.isEnabled =
                    isChecked && alwaysOnDisplayPreference.isChecked
                handwavePreference.isEnabled = isChecked
                pickUpPreference.isEnabled = isChecked
                pocketPreference.isEnabled = isChecked
            }
        }

        return true
    }
}
