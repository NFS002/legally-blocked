package blokd.core.assets

import blokd.transactions.Transaction
import blokd.wallet.Wallet


data class Token(override val name:String = "MyToken",
                 val symbol:String = "±",
                 private var remainingUnusedBalance: Int = 1000) : Asset() {

    fun mint(to: Wallet, amount: Int) : Transaction {
        if (amount > remainingUnusedBalance) {
            throw java.lang.IllegalArgumentException("Insufficient remaining balance in asset")
        }
        val tx1 = Transaction(sender = keyPair.public, recipient = to.publicKey, amount = amount, asset = this)
        remainingUnusedBalance -= amount
        tx1.sign(keyPair.private)
        return tx1
    }
}