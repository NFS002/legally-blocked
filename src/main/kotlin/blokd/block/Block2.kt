package blokd.block

import blokd.actions.BlockData
import blokd.actions.Contract
import blokd.extensions.hash
import blokd.extensions.id
import blokd.extensions.sign
import blokd.extensions.verifySignature
import com.fasterxml.jackson.annotation.JsonIgnore
import org.apache.log4j.Level
import org.apache.log4j.Logger
import java.security.PrivateKey
import java.security.PublicKey
import java.time.Instant

data class Block2(val previousHash: String = BlockChain.getPreviousBlock()?.header ?: "",
                 val expectedHeight: Int = BlockChain.nextHeight) {

    @get:JsonIgnore
    val header: String
        get() = calculateHeader()

    var nonce: Long = 0

    private val timestamp: Long = Instant.now().toEpochMilli()

    private val logger = Logger.getLogger(this::class.java)

    val blockData: MutableList<Contract> = mutableListOf()

    private val signatures: HashMap<String, ByteArray> = hashMapOf()

    fun calculateHeader(): String {
        val dt = this.blockData.joinToString(separator = "") { it.encoded }
        return "$previousHash$timestamp$dt$nonce".hash()
    }

    fun addBlockData(transaction: Contract): Block2 {
        this.blockData.add(transaction)
        return this
    }

    fun doPow(prefix: String, difficulty: Int) {
        val target: String = prefix.repeat(difficulty)
        while (!this.header.startsWith(target)) {
            this.nonce += 1
        }
    }


    fun validate(): Boolean {
        //TODO("Implement validation rules")
        return false
    }

    fun isSignedBy(publicKey: PublicKey, id: () -> String = { publicKey.id() }): Boolean {
        val signature = signatures.get(key = id()) ?: return false
        val res = this.header.verifySignature(publicKey, signature)
        val logLevel: Level = if (res) Level.INFO else Level.WARN
        logger.log(logLevel) {
            "Block $this is ${if (res) "correctly" else "incorrectly"} signed by $id"
        }
        return res
    }

    fun sign(privateKey: PrivateKey, id: String) {
        signatures.compute(id) { key, value ->
            value?.let {
                logger.warn {
                    "Overwriting block signature for $key"
                }
            }
            this.header.sign(privateKey)
        }
    }

    override fun toString(): String {
        return "Block(transactions=${blockData.size}, hash=${header.subSequence(0, 5)}..., timestamp=${timestamp})"
    }
}