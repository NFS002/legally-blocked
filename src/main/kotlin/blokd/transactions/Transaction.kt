package blokd.transactions

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
                       val amount: Int,
                       var hash: String = "") : BlockData {

    override var signature: ByteArray = ByteArray(0)

    override val encoded: String by lazy {
        "${sender.encodeToString()}${recipient.encodeToString()}$amount"
    }

    var salt: Long = 0
        get() {
            field += 1
            return field
        }

    init {

        hash = "$encoded$salt".hash()
    }

    override fun sign(privateKey: PrivateKey) : Transaction {
        signature = encoded.sign(privateKey)
        return this
    }

    override fun validateSignature() : Boolean {
        return encoded.verifySignature(sender, signature)
    }
}