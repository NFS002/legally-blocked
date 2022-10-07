package blokd.extensions

import org.apache.log4j.Logger
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.nio.file.Path
import java.security.*
import java.security.spec.EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import kotlin.collections.Collection

private const val BASE_PROPERTIES_PATH = "/Users/noah/projects/legally-blocked/src/main/resources/blokd.properties"

private const val CONFIGKEY__KEYPATH = "keypath"

private const val PUBLIC_KEY_FILENAME = "public"

private const val PRIVATE_KEY_FILENAME = "private"

val LOGGER: Logger = Logger.getLogger("blokd.util")

val BASE_PROPERTIES = loadBlokdProperties()

val PRIMARY_KEYPAIR = getOrLoadKeys()



fun loadBlokdProperties() : Properties {
    val f = File(BASE_PROPERTIES_PATH)
    val props = Properties()
    FileInputStream(f).use {
        props.load(it)
        return props
    }
}

fun String.hash(algorithm: String = "SHA-256"): String {
    val messageDigest = MessageDigest.getInstance(algorithm)
    messageDigest.update(this.toByteArray())
    val base64Hash = String.format("%064x", BigInteger(1, messageDigest.digest()))
    LOGGER.debug{ "Calculated base64 hash of string '${this.shorten()}' as '${base64Hash.shorten()}"}
    return base64Hash
}

fun String.sign(privateKey: PrivateKey, algorithm: String = "SHA256withRSA") : ByteArray {
    val rsa = Signature.getInstance(algorithm)
    rsa.initSign(privateKey)
    rsa.update(this.toByteArray())
    val signed = rsa.sign()
    LOGGER.debug{ "Signed string '${this.shorten()}' with private key " +
            "${privateKey.encodeToString().shorten()} and calculated ${signed.toString().shorten()}"
    }
    return signed
}

fun String.shorten(i:Int = 3) : String {
    val l = this.length
    return when (l > i * 2) {
        true -> "${this.subSequence(0, i)}...${this.subSequence(l - i, l)}"
        else -> this
    }
}

fun String.verifySignature(publicKey: PublicKey, signature: ByteArray, algorithm: String = "SHA256withRSA"): Boolean {
    val rsa = Signature.getInstance(algorithm)
    rsa.initVerify(publicKey)
    rsa.update(this.toByteArray())
    val res = rsa.verify(signature)
    LOGGER.debug{ "Verified signature of ${signature.toString().shorten()} with public key " +
            "${publicKey.encodeToString().shorten()} with result=$res"}
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

fun Key.id() : String {
    return UUID.nameUUIDFromBytes(this.encodeToString().toByteArray()).toString()
}


fun Key.encodeToString(): String {
    return Base64.getEncoder().encodeToString(this.encoded)
}

inline fun Boolean.then(block: Boolean.() -> Unit): Boolean {
    if (this) {
        block()
    }
    return this
}


inline fun Boolean.ifTrue(block: Boolean.() -> Unit): Boolean {
    if (this) block()
    return this@ifTrue
}

inline fun Boolean.ifFalse(block: Boolean.() -> Unit): Boolean {
    if (!this) block()
    return this@ifFalse
}

fun Collection<String>.hashList() {
    this.reduce{ acc, v -> (v xor acc).hash()}
}

fun newKeypair() : KeyPair {
    val generator = KeyPairGenerator.getInstance("RSA")
    generator.initialize(2048)
    val keyPair = generator.generateKeyPair()
    LOGGER.debug {
        "Generated new keypair with id=${keyPair.public.id()}"
    }
    saveKeyPair(keyPair)
    return keyPair
}

fun saveKeyPair(keyPair: KeyPair) {
    val keypath = BASE_PROPERTIES.getProperty(CONFIGKEY__KEYPATH)
    val f1 = Path.of(keypath, PUBLIC_KEY_FILENAME).toFile()
    val f2 = Path.of(keypath, PRIVATE_KEY_FILENAME).toFile()
    f1.createNewFile().then {
        f2.createNewFile().then {
            f1.writeBytes(keyPair.public.encoded)
            f2.writeBytes(keyPair.private.encoded)
            LOGGER.debug("Saved new keypair in folder ${keypath} with id=${keyPair.public.id()}")
        }
    }
}

fun getOrLoadKeys() : KeyPair {
    return loadKeyPair() ?: newKeypair()
}

private fun loadKeyPair() : KeyPair? {
    val keypath = BASE_PROPERTIES.getProperty(CONFIGKEY__KEYPATH)
    val f1 = Path.of(keypath, PUBLIC_KEY_FILENAME).toFile()
    val f2 = Path.of(keypath, PRIVATE_KEY_FILENAME).toFile()
    val canRead = f1.exists().and(f2.exists()).and(f1.canRead()).and(f2.canRead())
    canRead.ifFalse {
        LOGGER.error("Cannot read keypair from ${keypath}")
    }
    return if (canRead) KeyPair(loadPublic(f1), loadPrivate(f2)) else null
}

private fun loadPublic(f1: File) : PublicKey {
    LOGGER.debug("Loading public key from file ${f1.path}")
    val encoded = f1.readBytes()
    val keyFactory = KeyFactory.getInstance("RSA")
    val publicKeySpec: EncodedKeySpec = X509EncodedKeySpec(encoded)
    return keyFactory.generatePublic(publicKeySpec)
}

private fun loadPrivate(f1: File) : PrivateKey {
    LOGGER.debug("Loading private key from file ${f1.path}" )
    val encoded = f1.readBytes()
    val keyFactory = KeyFactory.getInstance("RSA")
    val privateKeySpec: EncodedKeySpec = PKCS8EncodedKeySpec(encoded)
    return keyFactory.generatePrivate(privateKeySpec)
}
