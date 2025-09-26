package com.mzansi.recipes.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log // Import Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NetworkMonitor(context: Context) {

    private val appContext = context.applicationContext
    private val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(isCurrentlyOnline()) // Initial check
    val isOnline: StateFlow<Boolean> = _isOnline

    private val scope = CoroutineScope(Dispatchers.Default)
    private val TAG = "NetworkMonitor" // Logging Tag

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network available: $network")
            publish()
        }
        override fun onLost(network: Network) {
            Log.d(TAG, "Network lost: $network")
            publish()
        }
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            Log.d(TAG, "Network capabilities changed for $network: $caps")
            publish()
        }
    }

    init {
        try {
            Log.d(TAG, "Initializing NetworkMonitor")
            _isOnline.value = isCurrentlyOnline()
            val req = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(req, callback)
            Log.d(TAG, "Network callback registered. Initial online state: ${_isOnline.value}")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException during NetworkMonitor init (missing ACCESS_NETWORK_STATE permission?): ${e.message}", e)
            _isOnline.value = false // Default to offline on critical error
        } catch (e: Exception) {
            Log.e(TAG, "Exception during NetworkMonitor init: ${e.message}", e)
            _isOnline.value = false // Default to offline on other errors
        }
    }

    private fun publish() {
        scope.launch {
            val currentStatus = isCurrentlyOnline()
            if (_isOnline.value != currentStatus) {
                Log.d(TAG, "Publishing network status: $currentStatus")
                _isOnline.value = currentStatus
            }
        }
    }

    private fun isCurrentlyOnline(): Boolean {
        return try {
            val network = cm.activeNetwork
            if (network == null) {
                Log.d(TAG, "isCurrentlyOnline: No active network.")
                return false
            }
            val caps = cm.getNetworkCapabilities(network)
            if (caps == null) {
                Log.d(TAG, "isCurrentlyOnline: No network capabilities for active network.")
                return false
            }
            val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            Log.d(TAG, "isCurrentlyOnline: Has Internet: $hasInternet, Is Validated: $isValidated")
            hasInternet && isValidated
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in isCurrentlyOnline (missing ACCESS_NETWORK_STATE permission?): ${e.message}", e)
            false // Default to offline on critical error
        } catch (e: Exception) {
            Log.e(TAG, "Exception in isCurrentlyOnline: ${e.message}", e)
            false // Default to offline on other errors
        }
    }

    fun close() {
        Log.d(TAG, "Closing NetworkMonitor")
        try { 
            cm.unregisterNetworkCallback(callback) 
            Log.d(TAG, "Network callback unregistered.")
        } catch (e: Exception) {
            Log.e(TAG, "Exception unregistering network callback: ${e.message}", e)
        }
        scope.cancel()
        Log.d(TAG, "Coroutine scope cancelled.")
    }
}