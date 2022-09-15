package blokd.wallet

import blokd.block.Block
import blokd.core.BlockChain

fun mintAndMine(
     blockChain: BlockChain,
     name:String, wallet: Wallet, value:Int = 10)
{
     val tx = blockChain.mintFromAsset(name, wallet, value)
    val prevHash = blockChain.getPreviousBlock()?.header ?: ""
    val block = Block(previousHash = prevHash)
    block.addBlockData(tx)
    blockChain.add(block)
}

fun transferAndMine(blockChain: BlockChain, sender: Wallet, recipient: Wallet, value:Int = 10)  {
    val prevHash = blockChain.getPreviousBlock()?.header ?: ""
    val block = Block(previousHash = prevHash)
     val tx = sender.sendFundsTo(recipient, value)
    block.addBlockData(tx)
    blockChain.add(block)
}