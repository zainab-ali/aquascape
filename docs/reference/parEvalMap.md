{%
  pageid = DocsReferenceParEvalMap
%}

# parEvalMap

This page is a reference guide for the `parEvalMap` family of operators. It describes:

 - The basic behaviour of `parEvalMap`, `parEvalMapUnbounded` and `parEvalMapUnordered`.
 - How `parEvalMap` operators destroy chunks.

## Behaviour

The `parEvalMap` family of operators are used to evaluate effects concurrently. They differ in two aspects:

 - Whether they limit the number of concurrent effects
 - Whether they output elements in order of their input, or as soon as they are calculated.

| output order            | bounded concurrency   | unbounded concurrency          |
|-------------------------|-----------------------|--------------------------------|
| in order of input       | `parEvalMap`          | `parEvalMapUnbounded`          |
| in order of calculation | `parEvalMapUnordered` | `parEvalMapUnorderedUnbounded` |

### parEvalMapUnbounded

`parEvalMapUnbounded` pulls an element from its input stream, constructs an effect for it, then evaluates it while pulling and constructs the subsequent effects. It evaluates the effects concurrently. 

In the following example, the elements `a`, `b` and `c` are pulled from the input stream. An `IO.sleep(1.second)` effect is evaluated for each of them. The effects are evaluated concurrently, so the total time taken is one second.

@:example(parEvalMapUnbounded) {
  drawChunked = false
}

### parEvalMap

`parEvalMap(n)` evaluates at most `n` effects concurrently. You can experiment with the number of concurrent effects below.

@:exampleWithInput(parEvalMapConcurrency) {
  drawChunked = false
}

#### Output order

The elements outputted by `parEvalMap` and `parEvalMapUnbounded` are always in the same order as their corresponding input. 

If an element takes a long time to evaluate, it will hold up the output of subsequent results.

In the following example, the effect for the first element `100` takes longer to be evaluated than that of the second element `5`. The second result is only outputted after the first result, even though it finishes long before it.

@:example(parEvalMapOrder) {
  drawChunked = false
}


### parEvalMapUnorderedUnbounded

`parEvalMapUnorderedUnbounded` is similar to `parEvalMapUnbounded`, but it outputs elements in the order that their effect evaluation completes.

In the following example, the first element `100` takes longer to be evaluated than the second element `5`. The second element is outputted before it.

@:example(parEvalMapUnorderedUnbounded) {
  drawChunked = false
}

### parEvalMapUnordered

`parEvalMapUnordered(n)` is similar to `parEvalMap`. It evaluates at most `n` effects concurrently, but outputs them in the order that their effect evaluation completes. You can experiment with the number of concurrent effects below.

@:exampleWithInput(parEvalMapUnorderedConcurrency) {
  drawChunked = false
}


# Chunk preservation

The `parEvalMap` family of operators always output singleton chunks.

The example below shows how a chunk of three elements is pulled from the input stream and outputted as three singleton chunks.

@:example(parEvalMapSingletonChunks) {
  suffix = chunked
  drawChunked = true
}

@:todo(cancellation, better descriptions)
