package blokd.block.cache

import blokd.block.Block

interface Cache {

    fun add(block: Block)

    fun get(height:Int) : Block?
}