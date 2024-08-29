{%
  pageid = DocsReferenceFilter
%}

# filter
This page is a reference guide for the `filter` family of operators. It describes:

 - The behaviour of the `filter`, `filterNot`, `filterWithPrevious`, `changes` and `mapFilter` operators.
 - How `filter` operators propagate chunks.

As the `filter` family of operators have normal error propagation and finalizer handling, these are not described.

## Behaviour
### filter

The `filter` operator pulls elements from an input stream and outputs them if they satisfy a given predicate. It is analogous to `filter` on lists.

In the following example, `filter` operates on an input stream of characters. It filters all characters that are equal to `b`.

@:example(filter) {
  drawChunked = false
}

### filterNot

The `filter` operator pulls elements from an input stream and outputs them if they do not satisfy the given predicate. It is analogous to `filterNot` on lists.

In the following example, `filter` operates on an input stream of characters. It discards all characters that are equal to `b`.

@:example(filterNot) {
  drawChunked = false
}

`filterNot(p)` is equivalent to `filter(a => !p(a))`


### filterWithPrevious

The `filterWithPrevious` operator is similar to `filter`. It pulls elements from an input stream and outputs them if they satisfy a given predicate. It uses the previously outputted element to evaluate the predicate.

In the following example, `filterWithPrevious` pulls from an input stream of characters. It outputs characters that are greater than or equal to the previously outputted character.

@:example(filterWithPrevious) {
  drawChunked = false
}

Each element is pulled and evaluated as follows:

- `a` is outputted because there is no previous value. 
- `b` satisfies `a <= b`, so is outputted.
- `c` satisfies `b <= c`, so is outputted.
- `a` does not satisfy `c <= a`, so is discarded.
- `b` does not satisfy `c <= b`, so is discarded. Note that the comparison is made with the previously outputted `c`, and not the previously pulled `a`.
- `c` satisfies `c <= c`, so is outputted.

### changes

The `changes` operator discards elements that are equal to the previously outputted value.

In the following example, the `changes` operator pulls from an input stream of characters.

Note that the repeated characters are not outputted.

@:example(changes) {
  drawChunked = false
}

`changes` is equivalent to `filterWithPrevious(_ != _)`.

@:todo(changesBy)

### mapFilter

The `mapFilter` operator maps each element to an option, and outputs the value if defined.

The following example maps an input stream of characters to their integer values if the character is equal to `b`.

@:example(mapFilter) {
  drawChunked = false
}

`mapFilter(f)` is equivalent to `map(f).unNone`.

## Chunk propagation

### filter
The `filter`, `filterNot` and `mapFilter` operators preserve the chunking of their input stream.

In the following example, two chunks of `[a, b, c]` are pulled. The `filter` operator discards all `b` characters to output `[a, c]`.

@:example(filterChunked) {
  drawChunked = true
}
 
### filterWithPrevious

The `filterWithPrevious` operator also preserves the chunking of its input stream, with the exception of the first chunk.

The first element of the input stream is pulled and outputted as a singleton chunk. The remaining elements in the chunk are filtered according to the predicate and outputted as one chunk. Subsequent chunks are preserved.

This is shown in the following example. The `filterWithPrevious` operator pulls a chunk `[a, b, c]` from its input. It outputs a singleton chunk `[a]`. When next pulled, it outputs `[b, c]` as these satisfy the predicate. It then pulls the next chunk `[a, b, c]` and outputs `[c]` as the only element satisfying the predicate.

@:example(filterWithPrevious) {
  drawChunked = true
  suffix = chunked
}
 
The chunk propagation of `changes` is similar to `filterWithPrevious`.
