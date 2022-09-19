package blokd.block

import blokd.extensions.hash
import blokd.actions.BlockData
import java.security.SignatureException
import java.time.Instant

data class Block(val previousHash: String) {

    val header: String
        get() = calculateHeader()

    var nonce: Long = 0

    private val timestamp: Long = Instant.now().toEpochMilli()

    val blockData: MutableList<BlockData> = mutableListOf()

    fun calculateHeader(): String {
        val dt = this.blockData.joinToString(separator = "") { it.encoded }
        return "$previousHash$timestamp$dt$nonce".hash()
    }

    fun addBlockData(transaction: BlockData): Block {
        this.blockData.add(transaction)
        return this
    }

    fun doPow(prefix: String, difficulty: Int) {
        val target: String = prefix.repeat(difficulty)
        while (!this.header.startsWith(target)) {
            this.nonce += 1
        }
    }

    override fun toString(): String {
        return "Block(transactions=${blockData.size}, hash=${header.subSequence(0, 5)}..., timestamp=${timestamp})"
    }
}