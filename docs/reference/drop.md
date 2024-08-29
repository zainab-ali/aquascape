{%
  pageid = DocsReferenceDrop
%}

# drop

This page is a reference guide for the `drop` family of operators. It describes:

 - The behaviour of `drop`, `dropWhile`, `dropThrough`, `dropLast` and `dropLastIf`.
 - How `drop` operators propagate chunks.

As the `drop` family of operators have normal error propagation and finalizer handling, these are not described.

## Behaviour

### drop

The `drop` operator pulls a given number of elements from an input stream and discards them, then pulls and outputs the remaining elements. It is analogous to `drop` on lists.

The following example drops a varying number of elements from a stream of characters.

Experiment with dropping different numbers of elements.

@:exampleWithInput(basic) {
  drawChunked = false
}

Notice that after discarding the given number of elements, `drop` behaves as an identity operation.

`drop(1)` is equivalent to `tail`.

### dropWhile

The `dropWhile` operator discards elements that satisfy a given predicate. It is analogous to `dropWhile` on lists.

In the following example, `dropWhile` pulls a character from the input stream and discards it if it is not equal to `b`.

The first character `a` is discarded. The second character `b` tests `false`, so is outputted. The third character `c` is then pulled and outputted.

@:example(dropWhile) {
  drawChunked = false
}

### dropThrough

The `dropThrough` operator behaves similarly to `dropWhile`, however it also drops the element for which the predicate tests `false`.

In the following example, `dropWhile` pulls a character from the input stream and discards it if it is not equal to `b`.

The first character `a` is discarded. The second character `b` tests `false`, but is also discarded. The third character `c` is then pulled and outputted.

@:example(dropThrough) {
  drawChunked = false
}

`dropThrough(p)` is equivalent to `dropWhile(p).drop(1)`.

### dropLast

The `dropLast` operator drops the last element of the input stream. It is analogous to `init` on lists.

In the following example, `dropLast` pulls from an input stream of characters. The last character `c` is discarded.

The last character is dropped.

@:example(dropLast) {
  drawChunked = false
}

When it is first pulled on, `dropLast` pulls two characters from the input stream. It outputs `a` after pulling the second character `b`. It must do so to verify that `a` is not the last character.

Note that `dropLast` has no effect if the input stream is infinite. It is only useful on finite streams.

### dropLastIf

The `dropLastIf` operator behaves similarly to `dropLast`, but only discards the last element if it satisfies the given predicate.

In the following example, the `dropLastIf` operator discards the last element if it is not equal to `b`.

The input stream is constructed by taking a varying number of characters from a stream of `a`, `b` and `c`. Experiment with different numbers of characters to see how `dropLastIf` behaves.

@:exampleWithInput(dropLastIf) {
  drawChunked = false
}

If the number of characters taken is `1` or `3`, the last element outputted by the input stream is `a` or `c` respectively. The characters satisfy the predicate of `dropLastIf`, so are not outputted.

If the number of characters taken is `2`, the last element outputted by the input stream is `b`. The predicate is not satisfied and `b` is outputted.

# Chunk propagation

## drop

The `drop` operator preserves the chunks of its input stream. If the number of elements dropped falls between a chunk, the chunk is partitioned and the start of it is discarded.

In the following example, the input stream outputs two chunks of `[a, b]`. Experiment with varying the number of elements dropped.

@:exampleWithInput(dropChunks) {
  drawChunked = true
}

Notice that if an odd number of elements is dropped, the chunk is partitioned into `[a]` and `[b]`, and the last partition `[b]` is outputted.

The `dropWhile` and `dropThrough` operators propagate chunks similarly to `drop`.

## dropLast

The `dropLast` operator also preserves the chunks of its input.

In the following example, the input stream outputs three chunks of `[a, b]`.

@:example(dropLastChunks) {
  drawChunked = true
}

Note that when `dropLast` is pulled on for the first time, it pulls two chunks from the input stream before outputting the first chunk.

The `dropLastIf` operator propagates chunks similarly to `dropLast`.

@:todo(should `dropLast` behave like this? It doesn't need to pull two chunks, and could instead output the chunk `init`.)
