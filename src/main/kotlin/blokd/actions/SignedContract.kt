package blokd.actions

import blokd.extensions.sign
import blokd.extensions.verifySignature
import java.security.PrivateKey
import java.security.PublicKey

class SignedContract(val contractId: String, signedBy:PrivateKey) : BlockData {

    override val encoded: String = "${contractId}-signed"

    override var signature = encoded.sign(signedBy)


    override fun validateSignature(publicKey: PublicKey): Boolean {
        return encoded.verifySignature(publicKey, signature)
    }

    override fun sign(privateKey: PrivateKey) : SignedContract {
        this.signature = encoded.sign(privateKey)
        return this
    }
}