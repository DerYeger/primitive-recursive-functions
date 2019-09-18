package eu.yeger.prf

fun c(value: Long) = Constant(value)

fun p(index: Int) = Projection(index)

fun s(): Successor = Successor()

//(x,y) -> x + y
fun addition(): Function {
    val first = p(0)
    return Recursion(
        first,
        first andThen s()
    )
}

//x -> x + value
fun add(value: Long) = addition().compose(p(0), c(value))

//x -> x - 1
fun predecessor() = Recursion(c(0), p(1))

//(x,y) -> x - y
fun subtraction(): Function {
    val first = p(0)

    return Recursion(
        first,
        first andThen predecessor()
    ).compose(
        p(1),
        p(0)
    )
}

//x -> x - value
fun subtract(value: Long) = subtraction().compose(p(0), c(value))

//x -> value - x
fun subtractFrom(value: Long) = subtraction().compose(c(value), p(0))

//(x,y) -> x * y
fun multiplication() = Recursion(c(0), addition().compose(p(0), p(2)))

//x -> x * value
fun multiplyBy(value: Long) =  multiplication().compose(p(0), c(value))

//x -> x²
fun square() =  multiplication().compose(p(0), p(0))

//(x,y) -> x^y
fun exp() =
    Recursion(
        c(1),
        multiplication().compose(
            p(0),
            p(2)
        )
    ).compose(
        p(1),
        p(0)
    )

fun caseDifferentiation(
    differentiationFunction: Function,
    zeroCaseFunction: Function,
    otherCaseFunction: Function
): Function {
    val subtractFromOne = subtractFrom(1)

    val zeroCaseTestFunction = multiplication().compose(
        zeroCaseFunction,
        differentiationFunction andThen subtractFromOne
    )

    val otherCaseTestFunction = multiplication().compose(
        otherCaseFunction,
        differentiationFunction andThen subtractFromOne andThen subtractFromOne
    )

    return addition().compose(
        zeroCaseTestFunction,
        otherCaseTestFunction
    )
}

fun boundedMuOperator(function: Function) =
    Recursion(
        c(0),
        caseDifferentiation(
            boundedMuOperatorDifferentiationFunction(function),
            p(1) andThen s(),
            p(0)
        )
    )

internal fun boundedMuOperatorDifferentiationFunction(function: Function): Function {
    val firstTestArguments = Array<Function>(function.arity) { p(it + 1)}
    firstTestArguments[0] = p(1) andThen s()
    val firstTestFunction = function.compose(*firstTestArguments)

    val secondTestFunction = p(0)

    val thirdTestArguments = firstTestArguments.clone()
    thirdTestArguments[0] = c(0)
    val thirdTestFunction = function.compose(*thirdTestArguments)

    val add = addition()
    val sub = subtraction()

    return add.compose(
        firstTestFunction,
        add.compose(
            secondTestFunction,
            sub.compose(
                c(1),
                thirdTestFunction
            )
        )
    )
}

//(x,y) -> ceiling(x / y)
fun ceilingDivision(): Function {
    //(n,x,y) -> x - n * y
    val g = subtraction().compose(
        p(1),
        multiplication().compose(
            p(0),
            p(2)
        )
    )

    return boundedMuOperator(g).compose(
        p(0),
        p(0),
        p(1)
    )
}

//(x,y) -> floor(x / y)
//or 0 if y == 0
fun floorDivision(): Function {
    val ceilingDivision = ceilingDivision()

    val differentiationFunction = subtraction().compose(
        multiplication().compose(
            ceilingDivision,
            p(1)
        ),
        p(0)
    )

    return caseDifferentiation(
        differentiationFunction,
        ceilingDivision,
        ceilingDivision andThen predecessor()
    )
}

//(x,y) -> x / y; if x / y is a natural number
//(x,y) -> 0; else
fun division(): Function {
    //(n,x,y) -> (x - n * y) + (n * y - x)
    val g = addition().compose(
        subtraction().compose(
            p(1),
            multiplication().compose(
                p(0),
                p(2)
            )
        ),
        subtraction().compose(
            multiplication().compose(
                p(0),
                p(2)
            ),
            p(1)
        )
    )

    return boundedMuOperator(g).compose(
        p(0),
        p(0),
        p(1)
    )
}

//WARNING Due to the nature of recursive functions using log will likely result in a StackOverflowError
//x -> logBase(x); if logBase(x) is a natural number
//x -> 0; else
fun log(base: Long): Function {
    val firstTestFunction = subtraction().compose(
        p(1),
        exp().compose(
            c(base),
            p(0)
        )
    )

    val secondTestFunction = subtraction().compose(
        exp().compose(
            c(base),
            p(0)
        ),
        p(1)
    )

    return boundedMuOperator(
        addition().compose(
            firstTestFunction,
            secondTestFunction
        )
    ).compose(
        p(0),
        p(0)
    )
}
