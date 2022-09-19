package blokd.contract

import blokd.actions.Contract
import blokd.actions.SignedContract
import blokd.block.Block
import blokd.core.BlockChain
import blokd.extensions.then
import blokd.merkle.randomName
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey

fun complete(blockChain: BlockChain, contract: Contract) : Boolean {
    val contractId = contract.id
    blockChain.findSigned(contractId)?.let {
        return contract.validateSignature(contract.owner) && blockChain.isValid()
    }
    return false
}

fun emptyBlock(blockChain: BlockChain) {
    val prevHash = blockChain.getPreviousBlock()?.header ?: ""
    val block = Block(previousHash = prevHash)
    blockChain.add(block)
}

fun registerContract(blockChain: BlockChain, owner: KeyPair, intendedRecipient: PublicKey, sign:Boolean = true): Contract {
    val contractText = randomName()
    val contract = Contract(text = contractText, owner = owner.public, intendedRecipient = intendedRecipient)
    sign.then { contract.sign(owner.private) }
    val prevHash = blockChain.getPreviousBlock()?.header ?: ""
    val block = Block(previousHash = prevHash)
    block.addBlockData(contract)
    blockChain.add(block)
    return contract
}

fun signContract(blockChain: BlockChain, contract: Contract, signer: PrivateKey) : SignedContract {
    val contractId = contract.id
    val signedContract = SignedContract(contractId, signedBy = signer)
    val prevHash = blockChain.getPreviousBlock()?.header ?: ""
    val block2 = Block(previousHash = prevHash)
    block2.addBlockData(signedContract)
    blockChain.add(block2)
    return signedContract
}