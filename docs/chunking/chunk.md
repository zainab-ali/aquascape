{%
  pageid = DocsReferenceChunk
%}

# chunk

This page is a reference guide for the `chunk` family of operators. It describes the behaviour of `chunks`, `unchunks`, `chunkLimit`, `chunkMin` and `chunkN`.

The following examples operate on an input stream of charaters, shown below.

@:example(input) {
  drawChunked = true
}

The input stream has type `Stream[F, Char]`. It outputs two chunks of `[a, b, c]` characters.

## chunks

The `chunks` operator pulls chunks from its input stream and outputs them as values.

If the input stream has type `Stream[Pure, A]`, the resulting stream has type `Stream[Pure, Chunk[A]]`.

In the following example, the `chunks` operator pulls a chunk of `[a, b, c]` from its input stream and outputs a singleton chunk of `[[a, b, c]]`. 

@:example(chunks) {
  drawChunked = true
}

## unchunks

The `unchunks` operator pulls on an input stream of chunk values and flattens it by outputting the chunks.


If the input stream has type `Stream[Pure, Chunk[A]]`, the resulting stream has type `Stream[Pure, A]`.

In the following example, the input stream outputs two values of `[a, b, c]`. It has type `Stream[Pure, Chunk[Char]]`. 

The `unchunks` operator pulls these values and outputs them as chunks. The resulting stream is of type `Stream[Pure, Char]`, and is chunked according to the chunks of the input stream.

@:example(unchunks) {
  drawChunked = true
}

`unchunks` can be thought of as the reverse of the `chunks` operator. `chunks.unchunks` is equivalent to the identity function.

## chunkLimit

The `chunkLimit` operator can be used to limit the size of chunks in a stream.

If the input stream has type `Stream[Pure, A]`, the resulting stream has type `Stream[Pure, Chunk[A]]`. The chunk values in the resulting stream have a size that is less than or equal to the given limit.

Experiment with the limit used for `chunkLimit` in the following example.

@:exampleWithInput(chunkLimit) {
  drawChunked = true
}

Notice that `chunkLimit` always outputs singleton chunks. 

When the limit is `3`, the same as the input stream chunk size, the resulting stream outputs a chunk `[[a, b, c]]`. This is a singleton chunk containing a single `[a, b, c]` value of type `Chunk[Char]`.

When the limit is `2`, smaller than the input stream chunk size, the pulled chunks are partitioned into two values of `[a, b]` and `[c]` respectively.

The `chunkLimit` operator can be followd by `unchunks` to rechunk a stream. `chunkLimit(n).unchunks` rechunks a stream such that chunks do not exceed the specified size.

## chunkMin

The `chunkMin` operator can be used to guarantee the minimum size of chunks in a stream.

If the input stream has type `Stream[Pure, A]`, the resulting stream has type `Stream[Pure, Chunk[A]]`. The chunk values in the resulting stream have a size that is greater than or equal to the given minimum size.

Experiment with the minimum size used for `chunkMin` in the following example.

@:exampleWithInput(chunkMin) {
  drawChunked = true
}

`chunkMin` always outputs singleton chunks.


Note that when the minimum size is greater than `3`, the size of `[a, b, c]`, the chunks from the input stream are concatenated until their size exceeds the minimum size. The last chunk is outputted as is, as there are no remaining chunks to concatenate it with.

When the minimum size is less than `3`, the chunks pulled from the input stream are preserved. 

`chunkMin` can be used alongside `unchunks` to rechunk a stream. `chunkMin(n).unchunks` rechunks a stream such that all but the last chunk are guaranteed to have a minimum size.

The last chunk can instead be dropped if it is smaller than the minimum by setting the second parameter `allowFewerTotal` to `false`.

## chunkN

The `chunkN` operator can be used to set the exact size of chunks in a stream.

If the input stream has type `Stream[Pure, A]`, the resulting stream has type `Stream[Pure, Chunk[A]]`. The chunk values in the resulting stream have a size that is equal to the given size.

Experiment with the size used for `chunkN` in the following example.

@:exampleWithInput(chunkN) {
  drawChunked = true
}

Note that the last chunk outputted by `chunkN` may be less than the given size. It can instead be dropped by setting the second parameter `allowFewer` to `false`.
