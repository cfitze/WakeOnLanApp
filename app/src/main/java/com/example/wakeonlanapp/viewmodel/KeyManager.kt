package com.example.wakeonlanapp.viewmodel

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

object KeyManager {

    private const val KEY_ALIAS = "MyKeyAlias"

    // Generate RSA key pair if it doesn't already exist
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

    // Retrieve private key from Keystore
    fun getPrivateKey(): PrivateKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        return keyStore.getKey(KEY_ALIAS, null) as PrivateKey
    }

    // Convert the private key to PEM format
    fun getPrivateKeyAsPem(): String {
        val privateKey = getPrivateKey()

        val keyFactory = KeyFactory.getInstance("RSA")
        val pkcs8Spec = keyFactory.getKeySpec(privateKey, java.security.spec.PKCS8EncodedKeySpec::class.java)
        val pkcs8Bytes = pkcs8Spec.encoded

        return Base64.encodeToString(pkcs8Bytes, Base64.NO_WRAP)
    }

    // Retrieve public key from Keystore
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
        val base64Key = Base64.encodeToString(publicKeyBytes, Base64.NO_WRAP)
        return "ssh-rsa $base64Key android_device"
    }

    // Sign data using the private key
    /*
    fun signData(data: ByteArray): ByteArray {
        val privateKey = getPrivateKey()
        val signature = Signature.getInstance("SHA256withRSA").apply {
            initSign(privateKey)
            update(data)
        }
        return signature.sign()
    }

     */
}
