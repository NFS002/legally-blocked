package blokd.core.assets

import java.security.KeyPair
import java.security.KeyPairGenerator

abstract class Asset {

    var keyPair: KeyPair

    abstract val name: String

    init {
        keyPair = generateKeyPair()
    }

    private fun generateKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        return generator.generateKeyPair()
    }
}