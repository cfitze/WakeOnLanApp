package com.example.wakeonlanapp.viewmodel

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Base64

object KeyManager {
    //private const val KEY_ALIAS = "MySSHKeyAlias"
    private const val KEY_ALIAS = "MyKeyAlias"

    init {
        generateKeyPairIfNeeded()
    }

    // Generate RSA KeyPair in Android Keystore
    fun generateKeyPairIfNeeded() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        if (!keyStore.containsAlias(KEY_ALIAS)) {
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
        }
    }

    fun getPrivateKey(): PrivateKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        return keyStore.getKey(KEY_ALIAS, null) as PrivateKey
    }

    fun getPublicKey(): PublicKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        return keyStore.getCertificate(KEY_ALIAS).publicKey
    }

    fun getOpenSshPublicKey(): String {
        val publicKey = getPublicKey()
        val encodedKey = Base64.getEncoder().encodeToString(publicKey.encoded)
        return "ssh-rsa $encodedKey"
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return aliases
    }

    fun getCurrentAlias(): String {
        return KEY_ALIAS // Return the hardcoded alias currently in use
    }
}
