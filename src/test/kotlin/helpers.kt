package blokd

import blokd.block.Block
import blokd.core.BlockChain
import blokd.actions.Transaction
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey


fun newKeypair() : KeyPair {
    val generator = KeyPairGenerator.getInstance("RSA")
    generator.initialize(2048)
    return generator.generateKeyPair()
}

fun signAndMine(blockChain: BlockChain, transaction: Transaction, privateKey: PrivateKey) {
    val prevBlockHash = blockChain.getPreviousBlock()?.header ?: ""
    val block = Block(previousHash = prevBlockHash)
    transaction.sign(privateKey)
    block.addBlockData(transaction)
    blockChain.add(block)
}