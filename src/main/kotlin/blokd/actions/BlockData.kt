package blokd.actions

import java.security.PrivateKey
import java.security.PublicKey

sealed interface BlockData {

    fun validateSignature(publicKey: PublicKey) : Boolean

    fun sign(privateKey: PrivateKey) : BlockData

    val encoded : String

    val signature: ByteArray
}
