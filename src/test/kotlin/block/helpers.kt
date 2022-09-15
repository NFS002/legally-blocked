package blokd.block

import blokd.core.BlockChain
import blokd.core.assets.Asset
import blokd.transactions.Registration
import java.security.PrivateKey
import java.security.PublicKey

fun registerAsset(blockChain: BlockChain, block: Block, asset: Asset, publicKey: PublicKey, privateKey: PrivateKey): Registration {
    val reg0 = blockChain.createRegistration(asset, publicKey)
    reg0.sign(privateKey)
    block.addBlockData(reg0)
    blockChain.add(block)
    return reg0
}