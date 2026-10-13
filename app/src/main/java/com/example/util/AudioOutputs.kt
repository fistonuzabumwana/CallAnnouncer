package com.example.util

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build

/** Headphones, Bluetooth earbuds and car audio connected to the phone. */
object AudioOutputs {

    private val PERSONAL_OUTPUTS = buildSet {
        add(AudioDeviceInfo.TYPE_WIRED_HEADSET)
        add(AudioDeviceInfo.TYPE_WIRED_HEADPHONES)
        add(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)
        add(AudioDeviceInfo.TYPE_BLUETOOTH_SCO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) add(AudioDeviceInfo.TYPE_USB_HEADSET)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) add(AudioDeviceInfo.TYPE_HEARING_AID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(AudioDeviceInfo.TYPE_BLE_HEADSET)
            add(AudioDeviceInfo.TYPE_BLE_SPEAKER)
        }
    }

    /** The connected headphones or car output, preferring media-capable ones over call-only (SCO). */
    fun personalOutput(context: Context): AudioDeviceInfo? {
        val audioManager = context.getSystemService(AudioManager::class.java) ?: return null
        return try {
            audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                .filter { it.type in PERSONAL_OUTPUTS }
                .minByOrNull { if (it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) 1 else 0 }
        } catch (_: Exception) {
            null
        }
    }
}
