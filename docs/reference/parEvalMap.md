{%
  pageid = DocsReferenceParEvalMap
%}

# parEvalMap

This page is a reference guide for the `parEvalMap` family of operators. It describes:

 - The basic behaviour of `parEvalMap`, `parEvalMapUnbounded` and `parEvalMapUnordered`.
 - How `parEvalMap` operators destroy chunks.

## Behaviour

The `parEvalMap` family of operators are used to evaluate effects concurrently.

### parEvalMapUnbounded

`parEvalMapUnbounded` pulls an element from its input stream, constructs an effect for it, then evaluates it while pulling and constructs the subsequent effects. It evaluates the effects concurrently. 

In the following example, the elements `a`, `b` and `c` are pulled from the input stream. An `IO.sleep(1.second)` effect is evaluated for each of them. The effects are evaluated concurrently, so the total time taken is one second.

@:example(parEvalMapUnbounded) {
  drawChunked = false
}

### parEvalMap

`parEvalMap(n)` evaluates at most `n` effects concurrently. Experiment with the number of concurrent effects below.

@:exampleWithInput(parEvalMapConcurrency) {
  drawChunked = false
}

#### Output order

The elements outputted by `parEvalMap` and `parEvalMapUnbounded` are in the same order as their corresponding input. 

In the following example, the effect for the first element `3` takes longer to be evaluated than that of the second element `2`. The second result is only outputted after the first result, even though it finishes before it.

@:example(parEvalMapOrder) {
  drawChunked = false
}

### parEvalMapUnordered

`parEvalMapUnordered` is similar to `parEvalMap`, but it outputs elements in the order that their effect evaluation completes.

@:example(parEvalMapUnordered) {
  drawChunked = false
}

# Chunk preservation

The `parEvalMap` family of operators do not preserve chunks. They always output singleton chunks.

@:example(parEvalMapSingletonChunks) {
  suffix = chunked
  drawChunked = true
}
