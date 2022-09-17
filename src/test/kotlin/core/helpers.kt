package blokd.core

import blokd.block.Block
import blokd.core.assets.Asset
import blokd.actions.Registration

fun registerAsset(blockChain: BlockChain, asset: Asset): Registration {
    val tx = blockChain.createRegistration(asset)
    val prevHash = blockChain.getPreviousBlock()?.header ?: ""
    val block = Block(previousHash = prevHash)
    block.addBlockData(tx)
    blockChain.add(block)
    return tx
}


fun deregisterAsset(blockChain: BlockChain, asset: Asset) {
    blockChain.assets.remove(asset.name)
}