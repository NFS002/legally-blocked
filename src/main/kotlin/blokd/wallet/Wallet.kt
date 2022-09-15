package blokd.wallet

import blokd.core.BlockChain
import blokd.core.assets.Token
import blokd.transactions.Transaction
import java.security.PrivateKey
import java.security.PublicKey

data class Wallet(val publicKey: PublicKey, val privateKey: PrivateKey, val blockChain: BlockChain, val token: Token) {

    val balance: Int get() {
        val allMyTx = getMyTransactions()
        val output = allMyTx.filter { it.sender == publicKey }.sumOf { it.amount }
        val input = allMyTx.filter { it.recipient == publicKey }.sumOf { it.amount }
        return input - output
    }


    private fun getMyTransactions() : Collection<Transaction> {
        val mytransactions: MutableList<Transaction> = mutableListOf()
        for (block in blockChain.blocks) {
            for (tx in block.blockData) {
                if (tx is Transaction && tx.asset.name == token.name &&
                    (tx.recipient == publicKey || tx.sender == publicKey)) {
                    mytransactions.add(tx)
                }
            }
        }
        return mytransactions
    }

    fun sendFundsTo(recipient: Wallet, amountToSend: Int) : Transaction {

        if (recipient.token != this.token) {
            throw java.lang.IllegalArgumentException("Recipient wallet is invalid")
        }

        if (amountToSend > balance) {
            throw IllegalStateException("Insufficient funds")
        }

        val tx = Transaction(asset=token, sender = publicKey, recipient = recipient.publicKey, amount = amountToSend)
        return tx.sign(privateKey)
    }
}