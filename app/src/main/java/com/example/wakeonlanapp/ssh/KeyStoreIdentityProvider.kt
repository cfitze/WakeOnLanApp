package com.example.wakeonlanapp.ssh

import net.schmizz.sshj.common.KeyType
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import java.security.PrivateKey
import java.security.PublicKey

class KeyStoreIdentityProvider(
    private val publicKey: PublicKey,
    private val privateKey: PrivateKey
) : KeyProvider {

    /*
    override fun getKeyPair(): KeyPair {
        // Implement the required method to return the key pair
        return KeyPair(publicKey, privateKey)
    }

    override fun getSignature(): Signature {
        // Pass the required parameters to SignatureRSA constructor
        return SignatureRSA()
    }
     */
    override fun getType(): KeyType {
        // Return the correct key type
        return KeyType.RSA
    }

    override fun getPublic(): PublicKey {
        // Implement the method to return the public key
        return publicKey
    }

    override fun getPrivate(): PrivateKey {
        // Implement the method to return the private key
        return privateKey
    }
}
