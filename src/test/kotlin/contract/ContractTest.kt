package blokd.contract

import blokd.block.Block
import blokd.core.BlockChain
import blokd.newKeypair
import blokd.actions.Contract
import blokd.actions.SignedContract
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.test.expect

class ContractTest {

    @Before
    fun before() {

    }

    /* Happy contract journey
    * User A
            * Create contract
            * Sign contract
            * Register contract
    * User B
            * Gets contract
            * Signs contract
            * Submits signed contract

    Assert: contract is completed
    * */
    @Test
    fun contractJourney() {
        val blockChain = BlockChain()
        val keyPair1 = newKeypair()
        val keyPair2 = newKeypair()
        val contractText = "This is a very serious contract"
        val contract = Contract(owner = keyPair1.public, text = contractText, intendedRecipent = keyPair2.public )
        contract.sign(keyPair1.private)
        val block1 = Block(previousHash = "")
        block1.addBlockData(contract)
        blockChain.add(block1)

        val signedContract = SignedContract(blockChain, contract.contractId.toString())
        signedContract.sign(keyPair2.private)
        val block2 = Block(block1.header)
        block2.addBlockData(signedContract)
        blockChain.add(block2)

        Assert.assertTrue(complete(blockChain, contract))
    }

    /* Sad contract journey - contract not signed */
    @Test
    fun unsignedContract() {
        val blockChain = BlockChain()
        val keyPair1 = newKeypair()
        val keyPair2 = newKeypair()
        val contractText = "This is a very serious contract"
        val contract = Contract(owner = keyPair1.public, text = contractText, intendedRecipent = keyPair2.public )
        contract.sign(keyPair1.private)
        val block1 = Block(previousHash = "")
        block1.addBlockData(contract)
        blockChain.add(block1)

        Assert.assertFalse(complete(blockChain, contract))
    }


    /* Sad contract journey - non existent contract signed */
    @Test(expected = IllegalArgumentException::class)
    fun nonexistentContract() {
        val blockChain = BlockChain()
        val keyPair1 = newKeypair()
        val keyPair2 = newKeypair()
        val contractText = "This is a very serious contract"
        val contract = Contract(owner = keyPair1.public, text = contractText, intendedRecipent = keyPair2.public )
        contract.sign(keyPair1.private)
        val block1 = Block(previousHash = "")
        block1.addBlockData(contract)
        //blockChain.add(block1)

        val contractId = contract.contractId.toString().plus("-non-existent")
        val signedContract = SignedContract(blockChain, contractId)
        signedContract.sign(keyPair2.private)
        val block2 = Block(block1.header)
        block2.addBlockData(signedContract)
        blockChain.add(block2)

    }
}