# Reference Guide

This is an aquascape-based reference guide to fs2.

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
