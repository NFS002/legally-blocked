package blokd.actions

import blokd.core.assets.Asset
import blokd.extensions.encodeToString
import blokd.extensions.hash
import blokd.extensions.sign
import blokd.extensions.verifySignature
import java.security.PrivateKey
import java.security.PublicKey

data class Transaction(val asset: Asset,
                       val sender: PublicKey,
                       val recipient: PublicKey,
                       val amount: Int) : BlockData {

    override var signature: ByteArray = ByteArray(0)

    override val encoded: String by lazy {
        "${sender.encodeToString()}${recipient.encodeToString()}$amount"
    }

    var salt: Long = 0
        get() {
            field += 1
            return field
        }

    val hash = "$encoded$salt".hash()

    override fun sign(privateKey: PrivateKey) : Transaction {
        signature = encoded.sign(privateKey)
        return this
    }

    override fun validateSignature(publicKey: PublicKey) : Boolean {
        when (publicKey.equals(sender)) {
            true -> {
                return encoded.verifySignature(publicKey, signature)
            }
            else -> throw IllegalStateException("Transaction signatyre must be verified as the original sender")
        }
    }
}