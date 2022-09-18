package blokd.contract

import blokd.block.Block
import blokd.core.BlockChain
import blokd.newKeypair
import blokd.actions.Contract
import blokd.actions.SignedContract
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.security.KeyPair
import kotlin.test.expect

class ContractTest {

    lateinit var blockChain: BlockChain
    lateinit var keyPairs: List<KeyPair>

    @Before
    fun before() {
        blockChain = BlockChain()
        keyPairs = (1..10).map { newKeypair() }
    }

    /*
    * Given:
    *   contract 'c1'
    *   user 'u1'
    *   user 'u2'
    * When:
    *   'u1' creates 'c1' with intended recipient as 'u2'
    *   'u1' signs 'c1'
    *   'u1' registers 'c1'
    *   'u2' signs contract 'c1' as 'sc1'
    *   'u2' submits 'sc1'
    * Then:
    *   contract is completed
    * */
    @Test
    fun `Simple happy contract journey`() {
        val contract = registerContract(blockChain, keyPairs[0].private, keyPairs[1].public)

        signContract(blockChain, contract, keyPairs[1].private)

        Assert.assertTrue(complete(blockChain, contract))
    }

    /*
    * Given:
    *   contract 'c1'
    *   user 'u1'
    *   user 'u2'
    * When:
    *   'u1' creates 'c1' with intended recipient as 'u2'
    *   'u1' signs 'c1'
    *   'u1' registers 'c1'
    * Then:
    *   contract is not completed (because it was never signed by its intended recipient)
    * */
    @Test
    fun `Unhappy contract journey - contract not signed by recipient`() {
        val contract = registerContract(blockChain, keyPairs[0].private, keyPairs[1].public)

        Assert.assertFalse(complete(blockChain, contract))
    }


    /*
    * Given:
    *   contract 'c1'
    *   user 'u1'
    *   user 'u2'
    * When:
    *   'u2' signs contract 'c1' as 'sc1'
    *   'u2' submits 'sc1'
    * Then:
    *   contract is not completed (because it was signed but never initially registered)
    * */
    @Test(expected = IllegalArgumentException::class)
    fun `Unhappy contract journey - the signed contract was never initially registered`() {
        val contract = Contract("This is a contract", keyPairs[1].public)

        signContract(blockChain, contract, keyPairs[1].private)

    }

    /*
   * Given:
   *   contract 'c1'
   *   user 'u1'
   *   user 'u2'
   *   user 'u3'
   * When:
   *   'u1' creates 'c1' with intended recipient as 'u2'
   *   'u1' signs 'c1'
   *   'u1' registers 'c1'
   *   'u3' signs contract 'c1' as 'sc1'
   *   'u3' submits 'sc1'
   * Then:
   *   contract is not complete (as it was not signed by its itended recipient)
   * */
    @Test(expected = IllegalStateException::class)
    fun `Sad contract journey - contract signed by another party than the intended recipient`() {
        val contract = registerContract(blockChain, keyPairs[0].private, keyPairs[1].public)
        signContract(blockChain, contract, keyPairs[3].private)
    }
}