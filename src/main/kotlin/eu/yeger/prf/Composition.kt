package eu.yeger.prf

import eu.yeger.prf.exception.CompositionException
import kotlin.math.min

class Composition constructor(
    private val evaluator: Function,
    private vararg val functions: Function,
    private val lazy: Boolean = false
    ) : Function() {

    init {
        if (evaluator.arity > functions.size) throw CompositionException(evaluator.arity, functions.size)
        setArity(functions.map { it.arity }.max() ?: 0)
    }

    override fun evaluate(arguments: Array<Argument>) =
        evaluator.applyArguments(
            functions
                .slice(0 until min(evaluator.arity, functions.size))
                .map { it.asArgument(arguments, lazy) }
                .toTypedArray()
        )
}
