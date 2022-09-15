package blokd.extensions

import java.math.BigInteger
import java.security.*
import java.util.*
import kotlin.collections.Collection

fun String.hash(algorithm: String = "SHA-256"): String {
    val messageDigest = MessageDigest.getInstance(algorithm)
    messageDigest.update(this.toByteArray())
    return String.format("%064x", BigInteger(1, messageDigest.digest()))
}

fun String.sign(privateKey: PrivateKey, algorithm: String = "SHA256withRSA") : ByteArray {
    val rsa = Signature.getInstance(algorithm)
    rsa.initSign(privateKey)
    rsa.update(this.toByteArray())
    return rsa.sign()
}

fun String.verifySignature(publicKey: PublicKey, signature: ByteArray, algorithm: String = "SHA256withRSA"): Boolean {
    val rsa = Signature.getInstance(algorithm)
    rsa.initVerify(publicKey)
    rsa.update(this.toByteArray())
    return rsa.verify(signature)
}

infix fun String.xor(that: String): String {

    val caller: String = this

    (that.isEmpty()).then {
        return caller
    }

    (this.length == that.length).then {
        return caller.mapIndexed { idx, c ->
            that[idx].code.xor(c.code)
        }.joinToString(separator = "")
    }
    throw IllegalArgumentException("Both strings must be of the same length")
}

fun Key.encodeToString(): String {
    return Base64.getEncoder().encodeToString(this.encoded)
}

inline fun Boolean?.then(block: Boolean.() -> Unit): Boolean? {
    if (this == true) {
        block()
    }
    return this
}

fun Collection<String>.hashList() {
    this.reduce{ acc, v -> (v xor acc).hash()}
}