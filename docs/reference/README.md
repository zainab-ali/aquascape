# Reference Guide

This is an aquascape-based reference guide to fs2.

It describes the semantics of fs2 operators - the technical details of how they behave.

Consult this guide when you need detailed information about an operator, such as when it terminates, how it propagates errors, when it executes finalizers, and how it manages chunks.

## Operators

Learn [how to read aquascapes](../README.md#how-to-read-the-diagrams), then learn about an operator:

 - [compile.toList, compile.last, compile.onlyOrError, compile.count, compile.drain](compile.md)
 - [take, takeWhile, takeThrough, takeRight](take.md)
 - [drop, dropWhile, dropThrough, dropLast, dropLastIf](drop.md)
 - [filter, filterNot, filterWithPrevious, changes, mapFilter](filter.md)
 - [evalMap, evalTap, evalMapChunk, evalTapChunk](evalMap.md)
 - [bracket, bracketCase, resource, onFinalize, onFinalizeCase](bracket.md)
 - [merge, mergeHaltL, mergeHaltR](merge.md)
 - [flatMap](flatMap.md)
 - [append, `++`](append.md)
 - [raiseError, handleError, handleErrorWith, attempt](errors.md)
 - [chunks, unchunks, chunkLimit, chunkMin, chunkN](chunk.md)
 - [sleep, delayBy, fixedDelay, awakeDelay, fixedRate, fixedRateStartImmediately, awakeEvery, spaced, metered, meteredStartImmediately](time.md)

Not all operators are documented yet. If you want to learn about one that's not listed, raise an issue on [GitHub](https://github.com/zainab-ali/aquascape/issues/new).

## Concurrency primitives
This guide also describes the technical details of concurrency primitives. These are used to communicate between streams.

Learn [how to read aquascapes](../README.md#how-to-read-the-diagrams), then learn about a primitive:

 - [Topic](topic.md)
