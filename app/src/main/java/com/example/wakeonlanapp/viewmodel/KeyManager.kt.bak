package com.example.wakeonlanapp.viewmodel

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PublicKey
import android.util.Base64

object KeyManager {

    private const val KEY_ALIAS = "MyKeyAlias"

    // Generate RSA key pair if it doesn't already exist
    fun generateKeyPairIfNeeded() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        // Check if the key pair already exists
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                "AndroidKeyStore"
            )

            keyPairGenerator.initialize(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_DECRYPT
                )
                    .setKeySize(2048)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .build()
            )

            keyPairGenerator.generateKeyPair()
        }
    }

    // Retrieve the public key from Keystore
    fun getPublicKey(): PublicKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        return keyStore.getCertificate(KEY_ALIAS).publicKey
    }

    // Convert the public key to OpenSSH format
    fun getOpenSshPublicKey(): String {
        val publicKey = getPublicKey()
        val publicKeyBytes = publicKey.encoded

        // Convert the key to Base64
        val base64PublicKey = Base64.encodeToString(publicKeyBytes, Base64.NO_WRAP)

        // Format the key as OpenSSH
        return "ssh-rsa $base64PublicKey android_device"
    }
}
