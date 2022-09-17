package blokd.actions

import blokd.core.BlockChain
import blokd.extensions.hash
import blokd.extensions.sign
import blokd.extensions.then
import blokd.extensions.verifySignature
import java.security.PrivateKey

class SignedContract(val blockChain: BlockChain, val contractId: String) : BlockData {

    override var signature: ByteArray = ByteArray(0)

    override val encoded: String = contractId.hash()

    fun findOrThrow(): Contract {
        this.blockChain.blocks.forEach{block ->
            block.blockData.forEach {
                blockData ->
                when (blockData) {
                    is Contract -> {
                        (blockData.contractId.toString() == contractId).then {
                            return blockData
                        }
                    }
                }
            }
        }
        throw IllegalArgumentException("Contract not found")
    }


    override fun validateSignature(): Boolean {
        val contract:Contract = findOrThrow()
        return encoded.verifySignature(contract.intendedRecipent, signature)
    }

    override fun sign(privateKey: PrivateKey) : SignedContract {
        this.signature = encoded.sign(privateKey)
        return this
    }
}