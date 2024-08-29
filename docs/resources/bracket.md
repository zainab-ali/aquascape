{% pageid = DocsReferenceBracket %}

# bracket

This page is a reference guide to the `bracket` family of operators. It describes:

 - The behaviour of `Stream.bracket`, `Stream.bracketCase`, `Stream.resource`, `onFinalize` and `onFinalizeCase`.
 - The error propagation of bracket operators.
 
As bracket operators have standard chunk propagation, this is not described.

## Behaviour

### bracket

The `Stream.bracket(acquire)(release)` operator constructs a stream from an `acquire` and `release` effect. The stream outputs a single element as a result of running `acquire`. When the stream terminates, the `release` effect is guaranteed to run, regardless of whether the stream terminates successfully, errors or is cancelled.

The following example shows how `Stream.bracket` constructs a single element stream. The `acquire` effect evaluates to a character `a`, which is outputted. When the stream is done, the `release` effect `IO('b')` is evaluated.

@:example(bracket) {
  drawChunked = false
}

#### Releasing resources mid-program

The `release` effect is run at the end of the stream's lifetime, not at the end of the entire program.

This is shown in the following example.
The streams `ab` and `xy` are appended using the `++` operator. Both streams are constructed with `release` effects.


@:example(bracketAppend) {
  drawChunked = false
}

When stream `ab` terminates, its release effect `IO('b')` is evaluated. Note this this is mid-program: the stream resulting from `++` has not terminated.

### resource

The `Stream.resource` operator constructs a stream from a `cats.effect.Resource`. The `acquire` and `release` effects of the resource are run similarly to `Stream.bracket`.

The following example lifts a resource with an acquire effect of `IO('a')` and release effect of `IO('b')` into a stream.

@:example(resource) {
  drawChunked = false
}

### bracketCase

The `Stream.bracketCase` operator behaves similarly to `Stream.bracket`, but uses the `ExitCase` to construct the `release` effect. The exit case describes how the stream terminated. It is either `Errored`, `Succeeded` or `Canceled`.

The following example shows the exit case of a successful stream.

@:example(bracketCase) {
  drawChunked = false
}

If an error is raised over the lifetime of the stream, its exit case will be `Errored`. In the following example, a stream is constructed from `bracketCase`, and then composed with an error-raising stream using `flatMap`.

The stream terminates with an exit case of `Errored` due to the raised error.

@:example(bracketCaseErrored) {
  drawChunked = false
}

If the stream is canceled due to fiber cancelation or interruption, its exit case will be `Canceled`. In the following example, the stream constructed from `bracketCase` is composed with a long-running `Stream.sleep`. It is then composed with a short-running `interruptAfter` operator. The resulting stream is canceled after 1 second.

@:example(bracketCaseCanceled) {
  drawChunked = false
}

Note that the exit case printed by the release effect is `Canceled`.

### onFinalize

The `onFinalize` operator evaluates a release effect when its input stream terminates.

`input.onFinalize(release)` is equivalent to `Stream.bracket(IO.unit)(release).flatMap(_ => input)`

In the following example, an input stream outputs a single `a` character. Once it is done, the resource effect `IO('b')`, termed a finalizer, is run.

@:example(onFinalize) {
  drawChunked = false
}

### onFinalizeCase

The `onFinalizeCase` operator behaves similarly to `onFinalize`, but uses the exit case of the input stream to construct the release effect. The exit case is calculated similarly to `bracketCase`.

@:example(onFinalizeCase) {
  drawChunked = false
}

## Error propagation

The acquire and release effects may raise errors. If so, these are propagated to the resulting stream.

### Errors raised in acquire

The release effect is only evaluated if the acquire effect was successful. In the following example, the acquire effect raises an error `Err`.

@:example(bracketAcquireError) {
  drawChunked = false
}

Note that the release effect `IO('b')` is never run and the resulting stream terminates with an exit case of `Errored`.


### Errors raised in release

Raising errors in the release effect is not recommended.

If an error is raised in the release effect, the exit case of the resulting stream is `Errored`. Any finalizers in the resulting stream are executed.

@:example(bracketReleaseError) {
  drawChunked = false
}
