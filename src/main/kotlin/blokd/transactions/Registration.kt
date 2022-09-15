package blokd.transactions

import blokd.core.BlockChain
import blokd.core.assets.Asset
import blokd.extensions.hash
import blokd.extensions.sign
import blokd.extensions.verifySignature
import java.security.PrivateKey
import java.security.PublicKey

data class Registration(val blockChain: BlockChain, val asset:Asset, val registrar: PublicKey) : BlockData {

    override val encoded: String = "${blockChain}-register-${asset.name}".hash()

    override var signature: ByteArray = ByteArray(0)

    override fun sign(privateKey: PrivateKey) : Registration {
        signature = encoded.sign(privateKey)
        return this
    }

    override fun validateSignature() : Boolean {
        return encoded.verifySignature(registrar, signature)
    }
}