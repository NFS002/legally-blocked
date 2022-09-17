package blokd.core

import blokd.block.Block
import blokd.core.assets.Asset
import blokd.core.assets.Token
import blokd.actions.Contract
import blokd.actions.Registration
import blokd.actions.SignedContract
import blokd.actions.Transaction
import blokd.extensions.then
import blokd.wallet.Wallet
import java.security.KeyPairGenerator
import java.security.PublicKey

class BlockChain {

    val blocks: MutableList<Block> = mutableListOf()
    private val difficulty: Int = 2
    private val prefix: String = "0"

    val assets: HashMap<String, Asset> = hashMapOf()
    val contracts: HashMap<String, Contract> = hashMapOf()

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

    fun add(block: Block) : Block {
        val minedBlock = mine(block)
        blocks.add(minedBlock)
        return minedBlock
    }

    private fun mine(block: Block) : Block {
        print("Mining: $block... ")
        block.doPow(this.prefix, this.difficulty)
        process(block)

        println("Mined!")

        return block
    }


    private fun process(block: Block) {
        // Consider it done ?
        for (blockData in block.blockData) {
            if (blockData.validateSignature()) {
                when (blockData) {
                    is Registration -> {
                        if (hasRegisteredAsset(blockData.asset.name)) {
                            throw java.lang.IllegalArgumentException(
                                "Attempting to register asset ${blockData.asset.name} which has already been registered"
                            )
                        }
                        val asset = blockData.asset
                        assets.put(asset.name, asset)
                    }
                    is Transaction -> {
                        if (!hasRegisteredAsset(blockData.asset.name)) {
                            throw java.lang.IllegalArgumentException(
                                "Attempting to mine transaction for ${blockData.asset.name} which has not been registered"
                            )
                        }
                    }
                    is Contract -> registerContract(blockData)
                    is SignedContract -> {
                        val contractId = blockData.contractId
                        (!hasRegisteredContract(contractId)).then {
                            throw java.lang.IllegalArgumentException("Contract not registered")
                        }
                        (!blockData.validateSignature()).then {
                            throw IllegalStateException("Contract signature is invalid")
                        }
                    }
                }
            }
        }
    }

    private fun isMined(block: Block): Boolean {
        return block.header.startsWith(this.prefix.repeat(this.difficulty))
    }


    fun newWallet(asset: String): Wallet {
        val registeredAsset: Token = assets.get(asset) as? Token
            ?: throw java.lang.IllegalArgumentException("Asset is not registered or is not a token")

        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        val keyPair = generator.generateKeyPair()

        return Wallet(keyPair.public, keyPair.private, this, registeredAsset)
    }

    fun createRegistration(asset: Asset, registrar: PublicKey) : Registration {
        if (hasRegisteredAsset(asset.name)) {
            throw java.lang.IllegalArgumentException("Name '${asset.name}' is already registered")
        }
        return Registration(asset = asset, blockChain = this, registrar = registrar)
    }

    private fun registerContract(contract: Contract) {
        val contractId = contract.contractId.toString()

        if (hasRegisteredContract(contractId = contractId)) {
            throw java.lang.IllegalArgumentException("Contract '${contract.contractId}' is already registered")
        }

        contracts.put(contractId, contract)
    }


    fun mintFromAsset(name: String, to: Wallet, amount: Int): Transaction {
        val token = assets.get(name) as? Token
        if (token == null) {
            throw java.lang.IllegalArgumentException("Asset does not exist or is not a token")
        } else {
            return token.mint(to, amount)
        }
    }

    fun hasRegisteredAsset(name: String): Boolean {
        return assets.containsKey(name)
    }

    fun hasRegisteredContract(contractId: String): Boolean {
        return this.contracts.containsKey(contractId)
    }

    fun findSigned(contractId: String): SignedContract? {
        this.contracts.getOrElse(contractId) { throw IllegalStateException("Initial contract was never registered") }
        this.blocks.forEach{block ->
            block.blockData.forEach {
                    blockData ->
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