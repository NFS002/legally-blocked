package blokd.block

import blokd.contract.registerContract
import blokd.core.BlockChain
import blokd.merkle.newKeypair
import org.junit.Before
import org.junit.Test
import java.security.KeyPair
import kotlin.test.assertNotEquals


class BlockTest {

    lateinit var block: Block
    lateinit var blockChain: BlockChain
    lateinit var keyPairs: List<KeyPair>

    @Before()
    fun before() {
        blockChain = BlockChain()
        keyPairs = (1..10).map { newKeypair() }
    }


    @Test
    fun calculateHash() {
        repeat(2) { registerContract(blockChain, keyPairs[0], keyPairs[1].public) }
        val block1Header = blockChain.blocks[0].header
        val block2Header = blockChain.blocks[1].header
        assertNotEquals(block1Header, block2Header)
    }
}