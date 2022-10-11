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
    @JsonProperty val nonce:Long = 0,
    @JsonProperty val timestamp: Long = Instant.now().toEpochMilli(),
    @JsonProperty val header: String = calculateHeader(blockData, previousHash, expectedHeight, timestamp, nonce)
) {


    @JsonIgnore
    private val logger = Logger.getLogger(this::class.java)


    fun validate(): Boolean {
        //TODO("Implement validation rules")
        return false
    }

    fun isSignedBy(publicKey: PublicKey, id: () -> String = { publicKey.id() }): Boolean {
        val signature = signatures.get(key = id()) ?: return false
        val res = this.header.verifySignature(publicKey, signature)
        val logLevel: Level = if (res) Level.INFO else Level.WARN
        logger.log(logLevel, "Block $this is ${if (res) "correctly" else "incorrectly"} signed by ${id()}")
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
}
