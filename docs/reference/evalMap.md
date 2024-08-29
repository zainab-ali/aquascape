{%
  pageid = DocsReferenceEvalMap
%}

# evalMap

This page is a reference guide to the `evalMap` family of operators. It describes:

 - The behaviour of `evalMap`.
 - The differences between `evalMap`, `evalTap`, `evalMapChunk` and `evalTapChunk`.
 - How each operator propagates chunks.
 - How each operator propagates errors. 

## Behaviour

### evalMap

The `evalMap` operator evaluates effects as part of a stream.

In the following example, an `IO(c.toInt)` effect is evaluated for each character in a stream. When pulled on, `evalMap` pulls on its input stream to obtain a character. It then evaluates the `IO` effect to produce an integer value. The integer is outputted.

@:example(basic) {
  drawChunked = false
}

### evalTap

The `evalTap` operator evaluates effects in a similar way, however "taps" instead of maps. It evaluates an effect, but discards the output, and instead outputs the originally pulled value.

The following example shows how `evalTap` behaves with the same effect.

@:example(evalTap) {
  drawChunked = false
}

Note that the characters `a` and `b` are outputted.

### evalMapChunk

`evalMapChunk` and `evalTap` chunk are similar to `evalMap` and `evalTap` respectively, but with different chunk propagation and error handling semantics. These are described later.

@:todo(this isn't true)
When no errors are raised, the `evalMapChunk` operator behaves identically to `evalMap`.

## Chunk propagation

### evalMap and evalTap

The `evalMap` operator does not chunk its output. When pulled on, it evaluates a single effect and outputs the result. 

The following example shows how chunks are outputted.

When pulled on, `evalMap` pulls a single chunk of `[a, b]`. It then evaluates `IO(a.toInt)` to output a singleton chunk of `[97]`. When next pulled on, it evaluates `IO(b.toInt)` to output a singleton chunk of `[98]`.

@:example(basic) {
  drawChunked = true
  suffix = chunked
}

This chunking strategy ensures that the number of effects evaluated matches the number of elements requested.

Experiment with evaluating a different number of effects with the example below. 

@:exampleWithInput(evalMapTake) {
  drawChunked = true
}

Note that the number of effects evaluated always matches the number of elements taken.

The `evalTap` operator has an identical chunking strategy to `evalMap`. It evaluates a single effect and outputs a singleton chunk.

### evalMapChunk and evalTapChunk

The `evalMapChunk` operator evaluates effects while preserving chunks.

When pulled on, it pulls a chunk from its input stream and evaluates its effect for all elements in the chunk.

This is shown in the example below. The `IO('a'.toInt)` and `IO('b'.toInt)` effects are evaluated together, and a single chunk of `[97, 98]` is outputted.

@:example(evalMapChunk) {
  drawChunked = true
}

### Excess effect evaluation

As a result of preserving chunks, the number of effects evaluated may not match the number of elements taken.

Consider taking a single element from the resulting stream, as demonstrated in the example below. Because effects are evaluated for entire chunks, the `IO('b'.toInt)` effect is still evaluated. However, since only the head element was requested, the result `98` is never outputted.

@:example(evalMapChunkHead) {
  drawChunked = true
}

As this can be misleading, it is recommended to use `evalMap` in cases where chunk preservation is not necessary.

The `evalTapChunk` operator preserves chunks in a similar way to `evalMapChunk`.

## Error propagation

### evalMap and evalTap

If the effect evaluated by `evalMap` or `evalTap` raises an error, this error is raised into the stream.

In the following example, an error is raised with `IO.raiseError`. This is propagated to the stream, and the entire program terminates with an error.

@:example(error) {
  drawChunked = false
}

### evalMapChunk and evalTapChunk

The `evalTapChunk` operator also raises errors into the resulting stream. Errors are raised when the chunk of effects is evaluated, so the termination behaviour is slightly different to `evalMap`.

To illustrate, consider chaining two `evalMap` operators. The first evaluates `c.toInt`, but raises an error on the second character `b`. The second evaluates an `isEven` effect.

@:example(errorEvalMap) {
  drawChunked = true
}

Because `evalMap` produces singleton chunks, the `isEven(97)` is evaluated and its result `[false]` is outputted as a singleton chunk.

This is not the case for `evalMapChunk`. Since the error is raised when evaluating effects for the entire chunk, `97` is never outputted and `isEven(97)` is never evaluated.

@:example(errorEvalMapChunk) {
  drawChunked = true
}
