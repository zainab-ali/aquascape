{%
  pageid = DocsReferenceFlatMap
%}

# flatMap

This is a reference guide to `flatMap`. It describes:

 - The basic behaviour of `flatMap`.
 - How `flatMap` propagates chunks.
 - How `flatMap` propagates errors.
 - When resource finalizers are executed.

## Basic behaviour

The `flatMap` operator pulls on an input stream and constructs a child stream for each element. The child stream is pulled on by the resulting stream.

In the following example, the input stream contains a single `"ab"` element. When pulled on by `flatMap` a child stream is constructed. When pulled on, this outputs elements `'a'` and `'b'`. The final output is a list of the pulled characters.

@:example(basic) {
  drawChunked = false
}

### Termination of the child stream

If the child stream is done, the next element of the input stream is pulled and a new child stream is constructed.

The following example shows an input stream with two elements `"ab"` and `"xy"`. Note that the pull for the second `"xy"` element occurs after elements `'a'` and `'b'` have been outputted by the first child stream.

@:example(multipleInputElements) {
  drawChunked = false
}

## Chunk propagation

The `flatMap` operator preserves chunks from the child stream. 

The following example shows a chunked view of the same input stream with `"ab"` and `"xy"` elements.

@:todo(we want the code snippet here too, and remove the chunked heading)

@:example(multipleInputElements) {
  drawChunked = true
}

Note that the chunks from the child streams `[a, b]` and `[x,y]` are outputted by the `flatMap` operator.

## Error propagation

Errors raised in the child stream are propagated to the resulting stream.

In the following example, an error is raised when the child stream is pulled on. This is propagated to the resulting stream, and the entire program terminates with the error.

@:example(errorPropagation) {
  drawChunked = false
}

### Handling errors raised in the child stream

Errors raised in the child stream can be handled in the resulting stream.

In the following example, the raised error is handled by outputting the characer `'z'`.

@:example(errorHandling) {
  drawChunked = false
}

### Handling errors raised in the input stream

Errors raised in the input stream can also be handled in the resulting stream.

In the following example, the input stream raises an error when pulled on. The error is handled in the resulting stream by outputting `'z'`. Note that the child stream is never constructed.

@:example(errorHandlingInput) {
  drawChunked = false
}

## Resource finalizers

### Input stream finalizers
The input stream may have finalizers associated with it. These are executed when the resulting stream terminates.

The following example has a finalizer associated with the input stream. Note that the finalizer is executed after all child streams are done.

@:example(finalizerInput) {
  drawChunked = false
}

### Child stream finalizers

Finalizers may also be associated with a child stream. If so, it is executed once the child stream is done.

The following example associates finalizers with both child streams. Note that the `ab` finalizer is executed when the first child stream is done, not when the resulting stream is done.

@:example(finalizerChild) {
  drawChunked = false
}
