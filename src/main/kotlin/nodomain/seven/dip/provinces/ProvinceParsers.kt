package nodomain.seven.dip.provinces

import nodomain.seven.dip.orders.PartiallyParsed

class TakeN<R>(val n: Int, val output: R): PartiallyParsed<R> {
    var counter = 1
    override fun isComplete(): Boolean = counter >= n
    override fun feed(string: String) { counter++ }
    override fun provideComplete(): R = output
}

class Defer<R>(val totalWords: Int, val deferredAt: Int, val decider: (String) -> R): PartiallyParsed<R> {
    init { require(totalWords >= deferredAt) }
    var counter = 1
    var output: R? = null
    override fun isComplete(): Boolean = counter >= totalWords
    override fun feed(string: String) { if (++counter == deferredAt) output = decider(string) }
    override fun provideComplete(): R = output!!
}