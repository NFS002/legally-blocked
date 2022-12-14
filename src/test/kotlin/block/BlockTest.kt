package blokd.block

import blokd.contract.registerContract
import blokd.extensions.newKeypair
import blokd.extensions.newKeypair2
import org.junit.Before
import org.junit.Test
import java.security.KeyPair
import kotlin.test.assertNotEquals


class BlockTest {

    lateinit var block: Block
    lateinit var keyPairs: List<KeyPair>

    @Before()
    fun before() {
        keyPairs = (1..10).map { newKeypair2() }
    }


    @Test
    fun calculateHash() {
        repeat(2) { registerContract(keyPairs[0], keyPairs[1].public) }
        val block1Header = BlockChain.blocks[0].header
        val block2Header = BlockChain.blocks[1].header
        assertNotEquals(block1Header, block2Header)
    }
}