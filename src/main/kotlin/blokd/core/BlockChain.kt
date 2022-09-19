package blokd.core

import blokd.actions.Contract
import blokd.actions.SignedContract
import blokd.block.Block
import blokd.extensions.then
import java.security.SignatureException

object BlockChain {

    val blocks: MutableList<Block> = mutableListOf()
    private val difficulty: Int = 2
    private val prefix: String = "0"

    private val contracts: HashMap<String, Contract> = hashMapOf()


    fun isValid(): Boolean {
        when {
            blocks.isEmpty() -> return true
            blocks.size == 1 -> return blocks[0].header == blocks[0].calculateHeader()
            else -> {
                for (i in 1 until blocks.size) {
                    val previousBlock = blocks[i - 1]
                    val currentBlock = blocks[i]

                    when {
                        currentBlock.header != currentBlock.calculateHeader() -> return false
                        currentBlock.previousHash != previousBlock.calculateHeader() -> return false
                        !(isMined(previousBlock) && isMined(currentBlock)) -> return false
                    }
                }
                return true
            }
        }
    }

    fun add(block: Block): Block {
        val minedBlock = mine(block)
        blocks.add(minedBlock)
        return minedBlock
    }

    private fun mine(block: Block): Block {
        print("Mining: $block... ")
        block.doPow(this.prefix, this.difficulty)
        process(block)

        println("Mined!")

        return block
    }


    private fun process(block: Block) {
        // Consider it done ?
        for (blockData in block.blockData) {
            when (blockData) {
                is Contract -> registerContract(blockData)
                is SignedContract -> {
                    val contractId = blockData.contractId
                    val contract = this.contracts.getOrElse(contractId) {
                        throw java.lang.IllegalArgumentException("Contract not registered")
                    }

                    (!blockData.validateSignature(contract.intendedRecipient)).then {
                        throw SignatureException("Contract signature is invalid")
                    }
                }
            }
        }
    }

    private fun isMined(block: Block): Boolean {
        return block.header.startsWith(this.prefix.repeat(this.difficulty))
    }


    private fun registerContract(contract: Contract) {

        val contractId = contract.id

        if (hasRegisteredContract(contractId = contractId)) {
            throw java.lang.IllegalArgumentException("Contract '${contractId}' is already registered")
        }

        if (!contract.validateSignature(contract.owner)) {
            throw SignatureException("Contract was not signed by its initial owner")
        }

        contracts[contractId] = contract
    }

    private fun hasRegisteredContract(contractId: String): Boolean {
        return this.contracts.containsKey(contractId)
    }

    fun findSigned(contractId: String): SignedContract? {
        this.contracts.getOrElse(contractId) { throw IllegalStateException("Initial contract was never registered") }
        this.blocks.forEach { block ->
            block.blockData.forEach { blockData ->
                when (blockData) {
                    is SignedContract -> {
                        (blockData.contractId == contractId).then {
                            return blockData
                        }
                    }
                }
            }
        }
        return null
    }

    fun getPreviousBlock(): Block? {
        return (
                if (blocks.isEmpty()) null
                else blocks.last()
                )
    }
}