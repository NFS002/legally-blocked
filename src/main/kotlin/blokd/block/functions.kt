package blokd.block

import blokd.actions.BlockData
import blokd.extensions.hash

fun calculateHeader(blockData: List<BlockData>, previousHash: String, expectedHeight: Int, timestamp: Long, nonce: Long): String {
    val dt = blockData.joinToString(separator = "") { it.encoded }
    return "$previousHash$timestamp$dt$expectedHeight$nonce".hash()
}

fun calculateHeader(block:Block): String {
    return calculateHeader(
        block.blockData,
        block.previousHash,
        block.expectedHeight,
        block.timestamp,
        block.nonce
    )
}