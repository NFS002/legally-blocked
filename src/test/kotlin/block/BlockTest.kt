package blokd.block

import blokd.core.BlockChain
import blokd.core.assets.Asset
import blokd.core.assets.Token
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotEquals


class BlockTest {

    lateinit var block: Block
    lateinit var blockChain: BlockChain
    lateinit var asset: Asset

    @Before()
    fun before() {
        blockChain = BlockChain()
        asset = Token()
        block = Block(previousHash = "")
    }

    /**
     * The block hash will be recalculated after transactions
     * are added and the block is finalised
     */
    @Test
    fun calculateHash() {
        val hash0 = block.header
        registerAsset(blockChain, block, asset, asset.keyPair.private)
        val hash1 = block.header
        assertNotEquals(hash0, hash1)
    }
}