package blokd.actions

import java.security.PrivateKey

sealed interface BlockData {

    fun validateSignature() : Boolean

    fun sign(privateKey: PrivateKey) : BlockData

    val encoded : String

    val signature: ByteArray
}
