package blokd.merkle

import blokd.core.BlockChain
import blokd.core.assets.Token
import blokd.newKeypair
import blokd.transactions.Transaction
import blokd.wallet.Wallet
import java.util.*


fun randomName(): String = UUID.randomUUID().toString()

fun toTransaction(blockChain: BlockChain, value: Int): Transaction {
    val keypair = newKeypair()
    val asset = Token(name = randomName())
    val wallet1 = Wallet(keypair.public, keypair.private, blockChain, asset)
    val wallet2 = Wallet(keypair.public, keypair.private, blockChain, asset)
    return Transaction(asset, wallet1.publicKey, wallet2.publicKey, value)
}

/* Pre-order search through binary tree, checking the value of the parent is equal to the combined
hashes of the children at each stage */
fun isValid(root: MerkleNode): Boolean {
    /* Initialise the result to true until is proved false */
    var result = true

    /* Pre-order search through binary tree, checking the value of the parent is equal to the combined
    hashes of the children at each stage */
    fun subtreeIsValid(node: MerkleNode?): Boolean {
        node?.let { n ->
            when {
                !n.isLeaf() -> {
                    val combined = n.leftTree?.hashWith(n.rightTree)
                    result = combined.equals(n.hash) and
                            subtreeIsValid(n.leftTree) and subtreeIsValid(n.rightTree)
                }
            }
        }
        return result
    }

    val combined = root.leftTree?.hashWith(root.rightTree)
    return combined.equals(root.hash) and
            subtreeIsValid(root.leftTree) and subtreeIsValid(root.rightTree)
}

