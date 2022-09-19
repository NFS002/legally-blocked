package blokd.core


import blokd.merkle.newKeypair
import org.junit.Before
import org.junit.Test
import java.security.KeyPair

class BlockChainTest {

    lateinit var keyPair: KeyPair

    @Before
    fun beforeTest() {
        keyPair = newKeypair()
    }

    /**
     * Tests the blockchain is initially in a valid state
     */
    @Test
    fun initiallyValid() {
        assert(BlockChain.isValid())
    }
}