package blokd.block.cache

import blokd.block.Block

object Fcache : Cache {
    private val map: HashMap<Int, Block> = hashMapOf()

    override fun add(block: Block) {
        val height = block.expectedHeight
        map.compute(height) { _, oldBlock ->
            preferred(block, oldBlock)
        }
    }

    /* Prefer according to validation rules */
    fun preferred(block1: Block, block2: Block?): Block {
        return (block2?.also {
            //TODO("Implement validation rules")
        } ?: block1)
    }

    override fun get(height: Int): Block? {
        return map[height]
    }

}