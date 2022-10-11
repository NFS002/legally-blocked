package blokd.block

import blokd.actions.BlockData
import blokd.extensions.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.log4j.Level
import org.apache.log4j.Logger
import java.security.PrivateKey
import java.security.PublicKey
import java.time.Instant

data class Block @JsonCreator constructor(
    @JsonProperty val previousHash: String = BlockChain.getPreviousBlock()?.header ?: "",
    @JsonProperty val expectedHeight: Int = 0,
    @JsonProperty val blockData: List<BlockData> = listOf(),
    @JsonProperty var signatures: HashMap<String, ByteArray> = hashMapOf(),
    @JsonProperty val nonce: Long = 0,
    @JsonProperty val timestamp: Long = Instant.now().toEpochMilli(),
    @JsonProperty val header: String = calculateHeader(blockData, previousHash, expectedHeight, timestamp, nonce)
) {


    @JsonIgnore
    private val logger = Logger.getLogger(this::class.java)


    fun validate(): Boolean {
        //TODO("Implement validation rules")
        return true
    }

    fun isSignedBy(publicKey: PublicKey, id: () -> String = { publicKey.id() }): Boolean {
        val signature = signatures.get(key = id()) ?: return false
        val res = this.header.verifySignature(publicKey, signature)
        val logLevel: Level = if (res) Level.INFO else Level.WARN
        logger.log(logLevel, "$this is ${if (res) "correctly" else "incorrectly"} signed by ${id()}")
        return res
    }

    fun sign(privateKey: PrivateKey, id: String) {
        signatures.compute(id) { key, value ->
            value?.let {
                logger.warn("Overwriting block signature for $key")
            }
            this.header.sign(privateKey)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Block
        return (
                other.blockData.equals(this.blockData) &&
                other.header.equals(this.header) &&
                other.timestamp.equals(this.timestamp) &&
                other.nonce.equals(this.nonce) &&
                other.signatures.decode().equals(this.signatures.decode())
        )
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return "Block[header=${header.shorten()},data=${blockData.size}, signatures=${signatures.size}, timestamp=$timestamp, nonce=$nonce]"
    }
}
