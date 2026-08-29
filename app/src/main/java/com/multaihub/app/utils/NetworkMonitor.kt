package com.multaihub.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Observes whether the device currently has an Internet-capable network.
 *
 * Network availability is dynamic and may change between WebView requests, so callers
 * need a lifecycle-aware [Flow] rather than a cached Boolean.
 *
 * @param context Any Android context; the application context is used internally.
 */
class NetworkMonitor(context: Context) {

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Emits the current connectivity state and subsequent changes.
     *
     * The flow is cold and unregisters its network callback when the last collector
     * cancels, making it safe to collect from Composables or ViewModels.
     */
    val isNetworkAvailable: Flow<Boolean> = callbackFlow {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                // Another network may still be active; recomputing avoids false offline events.
                trySend(isCurrentlyConnected())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            }
        }

        try {
            connectivityManager.registerNetworkCallback(request, callback)
            trySend(isCurrentlyConnected())
        } catch (securityException: SecurityException) {
            // Surface a safe offline state instead of crashing a collector if the platform
            // rejects network callback registration.
            trySend(false)
            close(securityException)
        }

        awaitClose {
            runCatching { connectivityManager.unregisterNetworkCallback(callback) }
        }
    }

    /**
     * Returns the current Internet capability without assuming a particular transport.
     *
     * Checks both Wi-Fi and cellular transports by looking at the `NET_CAPABILITY_INTERNET`
     * capability rather than a specific transport type.
     */
    fun isCurrentlyConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
