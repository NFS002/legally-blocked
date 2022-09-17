package blokd.core

import blokd.block.Block
import blokd.core.assets.Asset
import blokd.actions.Registration
import java.security.PrivateKey
import java.security.PublicKey

fun registerAsset(blockChain: BlockChain, asset: Asset, publicKey: PublicKey, privateKey: PrivateKey): Registration {
    val tx = blockChain.createRegistration(asset, publicKey)
    tx.sign(privateKey)
    val prevHash = blockChain.getPreviousBlock()?.header ?: ""
    val block = Block(previousHash = prevHash)
    block.addBlockData(tx)
    blockChain.add(block)
    return tx
}


fun deregisterAsset(blockChain: BlockChain, asset: Asset) {
    blockChain.assets.remove(asset.name)
}