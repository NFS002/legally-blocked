package blokd.merkle

import blokd.core.BlockChain
import blokd.transactions.Transaction
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.function.Executable

class MerkleTest {

    lateinit var merkleTree: MerkleNode

    lateinit var transactions: List<Transaction>

    @Before
    fun setup() {
        val blockChain = BlockChain()
        transactions = (1..15).map {
            toTransaction(blockChain, it)
        }
        merkleTree = MerkleNode.fromTransactions(transactions)
    }

    @Test
    fun isValid() {
        assert(isValid(merkleTree))
    }

    /* Tests that a given transaction appears as a leaf node in a merkle tree.
    * If the transaction was not in the tree, it would not have any siblings
    * so this test gets all sibling nodes in the tree, and asserts that list is not
    * empty */
    @Test
    fun merkleProof_true() {
        Assertions.assertAll(transactions.mapIndexed { idx, tx ->
            Executable {
                merkleTree.findProof(tx.hash)?.let {
                    Assertions.assertTrue(
                        isValid(it),
                        "Failed at index ${idx}, tx found but tree was invalid"
                    )
                }
            }
        })
    }

    @Test
    fun merkleProof_false() {
        Assert.assertNull(merkleTree.findProof("not-a-hash-value"))
    }
}