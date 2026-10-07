package com.example.prog_poe_final.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object OfflineManager {

    private const val QUEUE_KEY = "offline_queue"

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Save unsent message or action
    @SuppressLint("MutatingSharedPrefs")
    fun saveToQueue(context: Context, message: String) {
        val prefs = context.getSharedPreferences("OfflinePrefs", Context.MODE_PRIVATE)
        val existing = prefs.getStringSet(QUEUE_KEY, mutableSetOf()) ?: mutableSetOf()

        existing.add(message)

        prefs.edit().putStringSet(QUEUE_KEY, existing).apply()
    }

    // Get all unsent items
    fun getQueue(context: Context): MutableSet<String> {
        val prefs = context.getSharedPreferences("OfflinePrefs", Context.MODE_PRIVATE)
        return prefs.getStringSet(QUEUE_KEY, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    }

    // Clear saved items after uploading
    fun clearQueue(context: Context) {
        val prefs = context.getSharedPreferences("OfflinePrefs", Context.MODE_PRIVATE)
        prefs.edit().remove(QUEUE_KEY).apply()
    }
}
