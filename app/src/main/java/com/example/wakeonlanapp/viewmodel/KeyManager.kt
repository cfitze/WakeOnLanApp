package com.example.wakeonlanapp.viewmodel

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Base64

object KeyManager {
    private const val TAG = "KeyManager"
    private const val KEY_ALIAS = "MyKeyAlias"

    init {
        generateKeyPairIfNeeded()
    }

    // Generate RSA KeyPair in Android Keystore
    fun generateKeyPairIfNeeded() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                Log.d(TAG, "Key alias not found. Generating a new key pair.")

                val keyPairGenerator = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_RSA,
                    "AndroidKeyStore"
                )
                keyPairGenerator.initialize(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                    )
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                        .build()
                )
                keyPairGenerator.generateKeyPair()
                Log.d(TAG, "Key pair generated successfully with alias: $KEY_ALIAS")
            } else {
                Log.d(TAG, "Key alias already exists: $KEY_ALIAS")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating key pair: ${e.message}", e)
        }
    }

    fun getPrivateKey(): PrivateKey {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }
            keyStore.getKey(KEY_ALIAS, null) as PrivateKey
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving private key: ${e.message}", e)
            throw e
        }
    }

    fun getPublicKey(): PublicKey {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }
            keyStore.getCertificate(KEY_ALIAS).publicKey
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving public key: ${e.message}", e)
            throw e
        }
    }

    fun getOpenSshPublicKey(): String {
        return try {
            val publicKey = getPublicKey()
            val encodedKey = Base64.getEncoder().encodeToString(publicKey.encoded)
            "ssh-rsa $encodedKey"
        } catch (e: Exception) {
            Log.e(TAG, "Error converting public key to OpenSSH format: ${e.message}", e)
            throw e
        }
    }

    fun listKeyAliases(): List<String> {
        val aliases = mutableListOf<String>()
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }
            val enumeration = keyStore.aliases()
            while (enumeration.hasMoreElements()) {
                aliases.add(enumeration.nextElement())
            }
            Log.d(TAG, "Key aliases retrieved: $aliases")
        } catch (e: Exception) {
            Log.e(TAG, "Error listing key aliases: ${e.message}", e)
        }
        return aliases
    }

    fun getCurrentAlias(): String {
        Log.d(TAG, "Current key alias: $KEY_ALIAS")
        return KEY_ALIAS
    }
}
