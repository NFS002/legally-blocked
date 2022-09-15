package blokd.transactions

import blokd.extensions.hash
import blokd.extensions.sign
import blokd.extensions.verifySignature
import java.security.PrivateKey
import java.security.PublicKey
import java.time.Instant
import java.util.UUID

class Contract(val owner: PublicKey, val text: String, val potentialRecipents: List<PublicKey>) : BlockData {

    val createdAt:Long = Instant.now().epochSecond

    override var encoded = "$owner$text$createdAt$potentialRecipents".hash()

    val contractId = UUID.fromString(this.encoded)

    override var signature = ByteArray(0)

    override fun sign(privateKey: PrivateKey) : Contract {
        signature = encoded.sign(privateKey)
        return this
    }



    override fun validateSignature(): Boolean {
        return encoded.verifySignature(owner, signature)
    }

}