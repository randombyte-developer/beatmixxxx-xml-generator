package de.randombyte.xmlgenerator

fun <T, R> Iterable<T>.flatMapIndexed(transform: (Int, T) -> Iterable<R>): List<R> = this
        .toList()
        .mapIndexed { index, t -> Pair(index, t) }
        .flatMap { (index, t) -> transform(index, t) }