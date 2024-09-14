{%
  pageid = DocsReferenceCompile
%}

# compile

This page is a reference guide for the `compile` operations. It describes the behaviour of `compile.toList`, `compile.last`, `compile.onlyOrError`, `compile.count` and `compile.drain`.

## Behaviour

The `compile` operations are used to convert a stream into a single value or effect. Compiling a stream is analagous to compiling a program.

Streams with no side effects are termed pure streams and have the effect type of `Pure`. These are converted directly into a value. For example, `Stream('a','b').compile.toList` is an expression of type `List[Char]` which evaluates to the value `List('a','b')`.

Streams with side effects are converted into their effect type. For example, `Stream[IO, Char]('a','b').compile.toList` is an expression of type `IO[List[Char]]`. When run, it evaluates to `List('a', 'b')`.

### toList

The `compile.toList` operation repeatedly pulls on a stream until it is done, then results in a list of outputted values.

The following example, a pure input stream outputting the characters `'a'` and `'b'` is compiled to a value of `List('a', 'b')`.

@:example(toList) {
  drawChunked = false
}


`compile.toList` can be used on pure or side-effecting streams for which the output can be held in memory.

Using `compile.toList` on large streams will result in memory issues as the output is accumulated in a list.

### last

The `compile.last` operation repeatedly pulls on a stream until it is done, then results in the last outputted value.

Experiment with the behaviour of `compile.last` on non-empty and empty streams using the following example.

@:exampleWithInput(last) {
  drawChunked = false
}

Note that if the stream outputs no elements, the result of `compile.last` is `None`.

`compile.last` can be used on pure or side-effecting streams. The input stream must be finite for `compile.last` to terminate.


### onlyOrError

The `compile.onlyOrError` operation pulls on a stream and outputs the first value.

It expects a singleton stream. If the stream outputs more than one element, or does not output any elements, an error is raised. 

Experiment with the behaviour of `compile.onlyOrError` on empty, singleton, and non-empty streams using the following example.

@:exampleWithInput(onlyOrError) {
  drawChunked = false
}

`compile.onlyOrError` can be used on effectful streams with a monad error instance. The input stream must be finite for `compile.onlyOrError` to terminate.

### count

The `compile.count` operation repeatedly pulls on a stream until it is done, then results in a count of the number of elements outputted.

@:example(count) {
  drawChunked = false
}

`compile.count` can be used on pure or side-effecting streams. The input stream must be finite for `compile.count` to terminate.

### drain

The `compile.drain` operation repeatedly oulls on a stream until it is done, then results in a `()` value.

In the following example, the input stream executes side effect `97` before outputting `a`, then executes side-effect `98` before outputting `b`. Both `a` and `b` are discarded, but the side effects are still run.

@:example(drain) {
  drawChunked = false
}

`compile.drain` is used for input streams which are side-effecting. The side-effects in the stream are run, and the output is discarded. If the input is infinite, it will continue to pull on the stream and will never terminate.
