{%
  pageid = DocsReferenceScan
%}

# scan

This page is a reference guide for the `scan` family of operators. It describes:

 - The behaviour of `scan`, `scan1`, `scanMap` and `scanMonoid`.
 - How `scan` operators propagate chunks.

As the `scan` family of operators have normal error propagation and finalizer handling, these are not described.

## Behaviour

The `scan` family of operators perform a left fold over an input stream and output intermediate results.

### scan

The `scan` operator pulls an element from its input stream, evaluates a function with its previous result, then outputs the result. It is analogous to `scan` on lists.

The following example scans over an input stream of characters to produce a string. A character `ch` is pulled and combined with an accumulated string `str` with the `s"$str-$ch"` function.

@:example(scan) {
  drawChunked = false
}

### scan1

The `scan1` operator is similar to `scan`, but uses the first element pulled from the input stream to evaluate the result.

@:example(scan1) {
  drawChunked = false
}


### scanMonoid

The `scanMonoid` operator scans over its input with a `Monoid` typeclass instance.

The following example uses the `String` monoid instance. The empty element of the instance `""` is outputted first. The `combine` function of the instance concatenates two strings.

@:example(scanMonoid) {
  drawChunked = false
}

### scanMap

The `scanMap` operator maps over its input then scans with a `Monoid` typeclass instance. `input.scanMap(f)` is equivalent to `input.map(f).scanMonoid`.

@:example(scanMap) {
  drawChunked = false
}

## Chunk propagation

## scan

The `scan` operator preserves the chunks of its input. The first argument to `scan` is outputted as a singleton chunk.

@:example(scanChunkPropagation) {
  drawChunked = true
}

## scan1

`scan1` preserves the chunks of its input, with the exception of the first chunk. The first element of the first chunk is outputted as a singleton chunk.

@:example(scan1) {
  suffix = chunked
  drawChunked = true
}
