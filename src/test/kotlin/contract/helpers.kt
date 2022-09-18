package blokd.contract

import blokd.actions.Contract
import blokd.actions.SignedContract
import blokd.block.Block
import blokd.core.BlockChain
import java.security.PrivateKey
import java.security.PublicKey

fun complete(blockChain: BlockChain, contract:Contract) : Boolean {
    val contractId = contract.contractId.toString()
    blockChain.findSigned(contractId)?.let {
        return blockChain.isValid()
    }
    return false
}

fun registerContract(blockChain: BlockChain, owner: PrivateKey, intendedRecipient: PublicKey): Contract {
    val contractText = "This is a very serious contract"
    val contract = Contract(text = contractText, intendedRecipent = intendedRecipient)
    contract.sign(owner)
    val prevHash = blockChain.getPreviousBlock()?.header ?: ""
    val block = Block(previousHash = prevHash)
    block.addBlockData(contract)
    blockChain.add(block)
    return contract
}

fun signContract(blockChain: BlockChain, contract: Contract, signer: PrivateKey) : SignedContract {
    val contractId = contract.contractId.toString()
    val signedContract = SignedContract(contractId)
    signedContract.sign(signer)
    val prevHash = blockChain.getPreviousBlock()?.header ?: ""
    val block2 = Block(previousHash = prevHash)
    block2.addBlockData(signedContract)
    blockChain.add(block2)
    return signedContract
}