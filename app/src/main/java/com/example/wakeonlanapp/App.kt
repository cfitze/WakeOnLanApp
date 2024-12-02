package com.example.wakeonlanapp

import android.app.Application
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Provider
import java.security.Security

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Add the Bouncy Castle provider early in the app lifecycle
        setupBouncyCastle()
    }

    private fun setupBouncyCastle() {
        val provider: Provider? = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)
        if (provider != null) {
            if (provider.javaClass == BouncyCastleProvider::class.java) {
                // BC is already registered and is the correct implementation
                return
            } else {
                // Remove the existing (possibly outdated) BC provider
                Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
            }
        }
        // Add the custom Bouncy Castle provider
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }
}
