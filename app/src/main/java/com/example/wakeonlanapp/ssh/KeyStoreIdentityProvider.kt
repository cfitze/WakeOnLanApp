import net.schmizz.sshj.signature.Signature
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature as JavaSignature
import net.schmizz.sshj.common.KeyType

class KeyStoreIdentityProvider(
    private val publicKey: PublicKey,
    private val privateKey: PrivateKey
) : KeyProvider, Signature {

    private lateinit var signature: JavaSignature

    // Return the algorithm name for the signature
    override fun getSignatureName(): String {
        return "SHA256withRSA" // Algorithm supported by Android Keystore and SSHJ
    }

    // Return the public key
    override fun getPublic(): PublicKey {
        return publicKey
    }

    // Initialize signing with the private key
    override fun initSign(prvkey: PrivateKey) {
        signature = JavaSignature.getInstance(getSignatureName(), "AndroidKeyStore") // Keystore-specific signature
        signature.initSign(prvkey)
    }

    // Initialize verification with the public key
    override fun initVerify(pubkey: PublicKey) {
        signature = JavaSignature.getInstance(getSignatureName())
        signature.initVerify(pubkey)
    }

    // Update the data to be signed or verified
    override fun update(H: ByteArray) {
        signature.update(H)
    }

    // Update the data to be signed or verified with a slice of a byte array
    override fun update(H: ByteArray, off: Int, len: Int) {
        signature.update(H, off, len)
    }

    // Sign the data and return the signature
    override fun sign(): ByteArray {
        return signature.sign()
    }

    // Verify the data with the given signature
    override fun verify(sig: ByteArray): Boolean {
        return signature.verify(sig)
    }

    // Return the type of key (RSA in this case)
    override fun getType(): KeyType {
        return KeyType.RSA
    }
}
