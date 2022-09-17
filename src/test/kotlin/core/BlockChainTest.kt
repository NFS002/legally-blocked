package blokd.core


import blokd.core.assets.Asset
import blokd.core.assets.Token
import blokd.newKeypair
import blokd.signAndMine
import org.junit.Before
import org.junit.Test
import java.security.KeyPair
import java.security.SignatureException

class BlockChainTest {

    lateinit var blockChain: BlockChain
    lateinit var keyPair: KeyPair
    lateinit var asset: Asset

    @Before
    fun beforeTest() {
        blockChain = BlockChain()
        keyPair = newKeypair()
        asset = Token(name = "TestToken", symbol = "TT", remainingUnusedBalance = 100)
    }

    /**
     * Tests the blockchain is initially in a valid state
     */
    @Test
    fun initiallyValid() {
        assert(blockChain.isValid())
    }

    /**
     * When a blocks are added to the blockchain,
     * if that blocks is registering a new asset,
     * when the block is mined,
     * then the  asset is successfully registered.
     */
    @Test
    fun canRegisterAsset() {
        registerAsset(blockChain, asset, keyPair.public, keyPair.private)
        assert(blockChain.hasRegisteredAsset(asset.name))
    }

    /**
     * Attempting to sign an asset registration with a different key than
     * the pair from which it was initially created will cause an exception
     */
    @Test(expected = SignatureException::class)
    fun invalidRegistrationSignature() {
        val keyPair2 = newKeypair()
        registerAsset(blockChain, asset, keyPair.public, keyPair2.private)
    }

    /**
     * Creating a wallet for an asset which
     * has not been registered yet will throw an exception
     */
    @Test(expected = IllegalArgumentException::class)
    fun invalidWallet() {
            blockChain.newWallet(asset.name)
    }

    /**
     * Attempting to mint an asset that has
     * not been registered, will throw an exception
     */
    @Test(expected = IllegalArgumentException::class)
    fun invalidMint() {
        registerAsset(blockChain, asset, keyPair.public, keyPair.private)
        val wallet = blockChain.newWallet(asset.name)
        deregisterAsset(blockChain, asset)
        blockChain.mintFromAsset(asset.name, wallet, amount = 10)
    }

    /**
     * Attempting to mine a transaction for an asset which has not been registered
     * will throw an exception
     */
    @Test(expected = IllegalArgumentException::class)
    fun invalidTransaction() {
        registerAsset(blockChain, asset, keyPair.public, keyPair.private)
        val wallet = blockChain.newWallet(asset.name)
        val wallet2 = blockChain.newWallet(asset.name)
        val tx0 = blockChain.mintFromAsset(asset.name, wallet, 10)
        signAndMine(blockChain, transaction = tx0, privateKey = asset.keyPair.private)
        val tx1 = wallet.sendFundsTo(wallet2, 10)
        deregisterAsset(blockChain, asset)
        signAndMine(blockChain, transaction = tx1, privateKey = wallet.privateKey)
    }

    /**
     * Registering an asset under the same name as an already registered asset
     * will cause an exception
     */
    @Test(expected = IllegalArgumentException::class)
    fun invalidRegistration() {
        registerAsset(blockChain, asset, asset.keyPair.public, asset.keyPair.private)
        val asset2 = Token(name = asset.name)
        registerAsset(blockChain, asset2, asset2.keyPair.public, asset2.keyPair.private)

    }

    /**
     * After registering an asset and mining a transaction in consecutive blocks,
     * the blockchain is still valid
     */
    @Test()
    fun consecutiveBlocksValid() {
        registerAsset(blockChain, asset, keyPair.public, keyPair.private)
        val wallet = blockChain.newWallet(asset.name)
        val tx = blockChain.mintFromAsset(asset.name, wallet, 100)
        signAndMine(blockChain, transaction = tx, privateKey = asset.keyPair.private)
        assert(blockChain.isValid())
    }

    /**
     * After registering multiple assets in seperate blocks
     * the blockchain is still in a valid state
     */
    @Test()
    fun consecutiveBlocksValid2() {
        registerAsset(blockChain, asset, asset.keyPair.public, asset.keyPair.private)
        val asset2 = Token(name = "TestToken2")
        registerAsset(blockChain, asset2, asset2.keyPair.public, asset2.keyPair.private)
        assert(blockChain.isValid())
    }

    /**
     * Consecutive blocks which are not using the hash of the previous block are invalid
     */
    @Test()
    fun consecutiveBlocksInvalid() {
        registerAsset(blockChain, asset, keyPair.public, keyPair.private)
        val wallet = blockChain.newWallet(asset.name)
        val tx = blockChain.mintFromAsset(asset.name, wallet, 10)
        val tx2 = blockChain.mintFromAsset(asset.name, wallet, 10)
        signAndMine(blockChain, transaction = tx, privateKey = asset.keyPair.private)
        blockChain.blocks[0].addBlockData(tx2) /* Insert data into previous block, making invalid */
        assert(!blockChain.isValid())
    }
}