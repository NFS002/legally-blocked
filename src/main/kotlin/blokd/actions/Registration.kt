package blokd.actions

import blokd.core.BlockChain
import blokd.core.assets.Asset
import blokd.extensions.hash
import blokd.extensions.sign
import blokd.extensions.verifySignature
import java.security.PrivateKey
import java.security.PublicKey

data class Registration(val asset:Asset) : BlockData {

    override val encoded: String = "${asset.name}-registration".hash()

    override var signature: ByteArray = ByteArray(0)

    override fun sign(privateKey: PrivateKey) : Registration {
        signature = encoded.sign(privateKey)
        return this
    }

    override fun validateSignature(publicKey: PublicKey) : Boolean {
        return encoded.verifySignature(publicKey, signature)
    }
}