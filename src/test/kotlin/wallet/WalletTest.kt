package blokd.wallet

import blokd.core.BlockChain
import blokd.core.assets.Asset
import blokd.core.assets.Token
import blokd.core.registerAsset
import blokd.newKeypair
import org.junit.Before
import org.junit.Test
import java.security.KeyPair
import kotlin.test.assertEquals

class WalletTest {

    lateinit var blockChain: BlockChain
    lateinit var asset:Asset
    lateinit var wallet: Wallet
    lateinit var keyPair: KeyPair

    @Before
    fun before() {
        blockChain = BlockChain()
        asset = Token()
        keyPair = newKeypair()
        registerAsset(blockChain, asset, keyPair.public, keyPair.private)
        wallet = Wallet(keyPair.public, keyPair.private, blockChain, asset as Token)
    }

    /**
     * The wallet balance is successfully updated after minting 10 tokens
     * from the registered asset
     */
    @Test
    fun basicMint() {
        mintAndMine(blockChain = blockChain, name = asset.name, wallet = wallet)
        assertEquals(wallet.balance, 10)
    }

    /**
     * The tokens remaining unused balance is successfully updated and
     * an exception is thrown if attempting to mint more than the
     * remaining unused balance of the token
     *
     */
    @Test(expected = java.lang.IllegalArgumentException::class)
    fun overMint() {
        mintAndMine(blockChain = blockChain, name = asset.name, wallet = wallet, value = 900)
        mintAndMine(blockChain, name = asset.name, wallet = wallet, value = 200)
    }

    /**
     * Sending funds to another wallet greater than your balance
     * will throw an exception
     */
    @Test(expected = IllegalStateException::class)
    fun insufficientFunds() {
        val wallet2 = blockChain.newWallet(asset.name)
        transferAndMine(blockChain, wallet, wallet2)
    }

    /**
     * The tokens remaining unused balance is successfully updated and
     * an exception is thrown if attempting to mint more than the
     * remaining unused balance of the token
     *
     */
    fun simpleTransfer() {
        mintAndMine(blockChain = blockChain, name = asset.name, wallet = wallet)
        val wallet2 = blockChain.newWallet(asset.name)
        assertEquals(wallet2.balance, 0)
        transferAndMine(blockChain, wallet, wallet2)
        assertEquals(wallet2.balance, 10)

    }
}