package blokd.contract

import blokd.actions.Contract
import blokd.actions.SignedContract
import blokd.block.Block
import blokd.block.BlockChain
import blokd.extensions.then
import blokd.merkle.randomName
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey

fun complete(contract: Contract) : Boolean {
    val contractId = contract.id
    BlockChain.findSigned(contractId)?.let {
        return contract.validateSignature(contract.owner) && BlockChain.isValid()
    }
    return false
}

fun emptyBlock() {
    val prevHash = BlockChain.getPreviousBlock()?.header ?: ""
    val block = Block(previousHash = prevHash)
    BlockChain.add(block)
}

fun registerContract(owner: KeyPair, intendedRecipient: PublicKey, sign: Boolean = true): Contract {
    val contractText = randomName()
    val contract = Contract(text = contractText, owner = owner.public, intendedRecipient = intendedRecipient)
    sign.then { contract.sign(owner.private) }
    val prevHash = BlockChain.getPreviousBlock()?.header ?: ""
    val block = Block(previousHash = prevHash)
    block.addBlockData(contract)
    BlockChain.add(block)
    return contract
}

fun signContract(contract: Contract, signer: PrivateKey) : SignedContract {
    val contractId = contract.id
    val signedContract = SignedContract(contractId, signedBy = signer)
    val prevHash = BlockChain.getPreviousBlock()?.header ?: ""
    val block2 = Block(previousHash = prevHash)
    block2.addBlockData(signedContract)
    BlockChain.add(block2)
    return signedContract
}