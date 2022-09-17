package blokd.contract

import blokd.actions.Contract
import blokd.core.BlockChain

fun complete(blockChain: BlockChain, contract:Contract) : Boolean {
    val contractId = contract.contractId.toString()
    blockChain.findSigned(contractId)?.let {
        return blockChain.isValid()
    }
    return false
}