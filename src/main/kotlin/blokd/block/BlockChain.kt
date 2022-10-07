package blokd.block

import blokd.actions.Contract
import blokd.actions.SignedContract
import blokd.block.cache.Cache
import blokd.block.cache.Fcache
import blokd.block.cache.PCache
import blokd.extensions.ifTrue
import blokd.extensions.then
import java.security.SignatureException

object BlockChain {

    val blocks: MutableList<Block> = mutableListOf()
    private const val difficulty: Int = 2
    private const val prefix: String = "0"

    private val contracts: HashMap<String, Contract> = hashMapOf()

    val nextHeight: Int
    get() = blocks.size


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

    fun canAccept(block: Block) : Boolean {
        BlockChain
        val currHash:String = this.getPreviousBlock()?.header ?:  ""
        var res = false
        (block.expectedHeight == this.nextHeight).ifTrue {
            (block.previousHash.equals(currHash)).ifTrue {
                // TODO("Check block meets validation rules")
                add(block)
                res = isValid()
                blocks.removeLast()
            }
        }
        return res
    }

    fun add(block: Block): Block {
        val minedBlock = mine(block)
        blocks.add(minedBlock)
        return minedBlock
    }

    private fun mine(block: Block): Block {
        print("Mining: $block... ")
        block.doPow(prefix, difficulty)
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
                    val contract = contracts.getOrElse(contractId) {
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
        return block.header.startsWith(prefix.repeat(difficulty))
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
        return contracts.containsKey(contractId)
    }

    fun findSigned(contractId: String): SignedContract? {
        contracts.getOrElse(contractId) { throw IllegalStateException("Initial contract was never registered") }
        blocks.forEach { block ->
            block.blockData.forEach { blockData ->
                (blockData as? SignedContract)?.let {
                    (blockData.contractId == contractId).then {
                        return blockData
                    }
                }
            }
        }
        return null
    }

    fun getPreviousBlock(): Block? {
        return blocks.lastOrNull()
    }
}