package blokd.actions

import blokd.core.BlockChain
import blokd.extensions.hash
import blokd.extensions.sign
import blokd.extensions.then
import blokd.extensions.verifySignature
import java.security.PrivateKey
import java.security.PublicKey

class SignedContract(val contractId: String) : BlockData {

    override var signature: ByteArray = ByteArray(0)

    override val encoded: String = contractId.hash()


    override fun validateSignature(publicKey: PublicKey): Boolean {
        return encoded.verifySignature(publicKey, signature)
    }

    override fun sign(privateKey: PrivateKey) : SignedContract {
        this.signature = encoded.sign(privateKey)
        return this
    }
}