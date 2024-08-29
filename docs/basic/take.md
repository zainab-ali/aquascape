{%
  pageid = DocsReferenceTake
%}

# take

This page is a reference guide to the `take` family of operators. It describes:

 - The basic behaviour of `take`, `takeWhile`, `takeThrough` and `takeRight`.
 - How `take` operators propagate chunks.

As `take` operators have no special error handling or finalizer handling semantics, these are not described.

## Behaviour

### take

The `take` operator pulls a number of elements from an input stream. It is analogous to `take` on lists.

The following example takes a varying number of elements from an infinite stream of `a` characters.

Experiment with taking different numbers of elements.

@:exampleWithInput(basic) {
  drawChunked = false
}

Note that the resulting stream is always finite.

### takeWhile

The `takeWhile` operator pulls and outputs elements from an input stream, provided the elements satisfy a given predicate. It is analogous to `takeWhile` on lists.

In the following example, `takeWhile` pulls characters from an input stream and tests that they are not equal to `b`. The first character `a` passes the test and is outputted. The second character `b` fails the test. The resulting stream is then done. Note that `c` is never outputted by the input stream.

@:example(takeWhile) {
  drawChunked = false
}

### takeThrough

The `takeThrough` operator behaves similarly to `takeWhile`. It pulls and outputs elements from an input stream, provided they satisfy a given predicate. Unlike `takeWhile`, it outputs the first element that tests `false`.

In the following example, `takeThrough` pulls characters from an input stream and tests that they are not equal to `b`. The first character `a` passes the test and is outputted. The second character `b` fails the test, but is also outputted. 

The resulting stream is then done. 

@:example(takeThrough) {
  drawChunked = false
}

Note that, as with `takeWhile`, the character `c` is never pulled.

### takeRight

The `takeRight` operator drops all but the given number of elements from an input stream. It is analogous to `takeRight` on lists.

The following example shows how `takeRight` operates on an input stream of six characters. Experiment with varying the number of elements taken.

@:exampleWithInput(takeRight) {
  drawChunked = false
}

Note that `takeRight` only outputs elements after the input stream is done. As a consequence, it should only be used with finite input streams. 

Internally, `takeRight(n)` keeps an in-memory buffer of `n` elements. Using `takeRight` with large values of `n` may therefore cause memory performance problems.

## Chunk propagation

### take

`take` preserves the chunking of its input stream. If the  number of elements to take falls between a chunk, part of the chunk is outputted and the rest discarded.

The following example shows an input stream that repeatedly outputs chunks of `[a, b]`. Experiment with taking different numbers of elements from it.

@:exampleWithInput(basic) {
  drawChunked = true
  suffix = chunked
}

The `takeWhile` and `takeThrough` operators propagate chunks similarly to `take`.

### takeRight

The `takeRight` operator outputs a single chunk of its elements, regardless of how its input stream is chunked. Experiment with this using the example below.

@:exampleWithInput(takeRight) {
  drawChunked = true
  suffix = chunked
}

