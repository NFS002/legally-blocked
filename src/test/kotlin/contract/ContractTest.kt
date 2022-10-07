package blokd.contract

import blokd.actions.Contract
import blokd.extensions.newKeypair
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.security.KeyPair
import java.security.SignatureException

class ContractTest {

    lateinit var keyPairs: List<KeyPair>

    @Before
    fun before() {
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
        val contract = registerContract(keyPairs[0], keyPairs[1].public)

        signContract(contract, keyPairs[1].private)

        Assert.assertTrue(complete(contract))
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
    *   the blockchain adds 10 more consecutive empty blocks
    *   'u2' signs contract 'c1' as 'sc1'
    *   'u2' submits 'sc1'
    * Then:
    *   contract is completed
    * */
    @Test
    fun `Happy contract journey - contract signed 10 blocks later`() {
        val contract = registerContract(keyPairs[0], keyPairs[1].public)

        repeat(10) { emptyBlock() }

        signContract(contract, keyPairs[1].private)

        Assert.assertTrue(complete(contract))
    }

    /*
   * Given:
   *   contract 'c1'
   *   contract 'c2'
   *   user 'u1'
   *   user 'u2'
   *   user 'u3'
   * When:
   *   'u1' creates 'c1' with intended recipient as 'u3'
   *   'u1' signs 'c1'
   *   'u1' registers 'c1'
   *   'u2' signs contract 'c1' as 'sc1'
   *   'u2' submits 'sc1'
   *   'u3' signs contract 'c1' as 'sc2'
   *   'u3' submits 'sc2'
   * Then:
   *   c1 is complete:
   *   c1 was signed twice - first by the incorrect recipient, then by the correct recipient.
   * */
    @Test
    fun `Happy journey - signed twice - firstly incorrect recipient, then correct recipient`() {
        val contract = registerContract(keyPairs[0], keyPairs[1].public)

        runCatching {
            signContract(contract, keyPairs[2].private)
        }
        signContract(contract, keyPairs[1].private)


        Assert.assertTrue(complete(contract))
    }



    /*
    * Given:
    *   contract 'c1'
    *   contract 'c2'
    *   user 'u1'
    *   user 'u2'
    *   user 'u3'
    * When:
    *   'u1' creates 'c1' with intended recipient as 'u2'
    *   'u1' creates 'c2' with intended recipient as 'u3'
    *   'u1' signs 'c1'
    *   'u1' registers 'c1'
    *   'u1' signs 'c2'
    *   'u1' registers 'c2'
    *   'u3' signs contract 'c2' as 'sc2'
    *   'u3' submits 'sc2'
    * Then:
    *   c1 is not complete (as it was never signed by its intended recipient)
    *   c2 is complete
    * */
    @Test
    fun `Two contracts - one happy and one sad`() {
        val contract1 = registerContract(keyPairs[0], keyPairs[1].public)
        val contract2 = registerContract(keyPairs[0], keyPairs[2].public)

        signContract(contract2, keyPairs[2].private)

        Assert.assertFalse(complete(contract1))
        Assert.assertTrue(complete(contract2))
    }

    /*
    * Given:
    *   contract 'c1'
    *   user 'u1'
    *   user 'u2'
    * When:
    *   'u1' creates 'c1' with intended recipient as 'u2'
    *   'u1' registers 'c1'
    * Then:
    *   contract is not completed (because it was never signed by its creator)
    * */
    @Test(expected = SignatureException::class)
    fun `Unhappy contract journey - Contract not signed by its creator when it is registered`() {
        registerContract(keyPairs[0], keyPairs[1].public, sign = false)
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
        val contract = registerContract(keyPairs[0], keyPairs[1].public)

        Assert.assertFalse(complete(contract))
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
        val contract = Contract("This is a contract", keyPairs[1].public, keyPairs[0].public)

        signContract(contract, keyPairs[1].private)

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
    @Test(expected = SignatureException::class)
    fun `Sad contract journey - contract signed by another party than the intended recipient`() {
        val contract = registerContract(keyPairs[0], keyPairs[1].public)
        signContract(contract, keyPairs[3].private)
    }
}