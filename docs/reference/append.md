{%
  pageid = DocsReferenceAppend
%}


# append

The `++` operator is an alias for to `append`.

This page is a reference guide to `append`. It describes:

 - The behaviour of `append`.
 - How it preserves chunking.
 - How it propagates errors.
 - When finalizers are executed.

## Behaviour

The `append` operator constructs a resulting stream from a left and right input stream, by pulling on the left until it is done, then pulling on the right. It is analogous to appending lists with the `++` list concatenation operator.

In following example a stream of `a` and `b` characters is appended to a stream of `x` and `y`.

Note that the `xy` stream is only pulled on once the `ab` stream is done. 

@:example(basic) {
  drawChunked = false
}

### A finite resulting stream

If a limited number of elements are pulled from the resulting stream, the right input stream may never be executed. 

The example below uses the `take` operator to limit the resulting stream. Experiment with the number of elements taken. 

@:exampleWithInput(finite) {
  drawChunked = false
}

Note that for two or fewer elements, the right input stream is never pulled on.


### Lazy construction of infinite streams

The right input stream is lazily constructed. This `append` operator can therefore be used to create infinite streams.

The following example appends the resulting stream onto `ab` using a recursive function. This produces an infinite stream of `a` and `b` characters. Experiment with it by taking a finite number of elements.

@:exampleWithInput(infinite) {
  drawChunked = false
}

Using `append` recursively is equivalent to the `repeat` operator. As recursion can be easliy misused to create stack-unsafe code, using `repeat` is preferred to explicit recursion.

## Chunk preservation

The `append` operator preserves the chunking of its left and right streams.

The following example shows the chunks outputted. The `ab` stream outputs a single chunk of characters `[a, b]`. This is propagated to the resulting stream. Similarly, the `xy` stream outputs a single chunk of `[x, y]` that is also propagated.

@:example(basic) {
  drawChunked = true
  suffix = chunked
}


## Error propagation

The `append` operator has no special error propagation semantics. Errors in either input stream are propagated to the resulting stream.

In the following example, an error is raised in the right input stream. The entire program terminates with the error.

@:example(errors) {
  drawChunked = false
}


## Finalizers

Finalizers can be attached to the left of right stream. They are executed when the input stream is done.

In the following example, finalizers are attached to both the left and right stream. Experiment with taking a fixed number of elements from the resulting stream.

Notice that the `ab` finalizer is executed when the `ab` stream is done, and before `xy` is pulled on.

@:exampleWithInput(finalizers) {
  drawChunked = false
}
