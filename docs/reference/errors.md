{%
  pageid = DocsReferenceError
%}

# errors

This page is a reference guide for the `raiseError` and `handleError` family of operators. It describes:

 - The behaviour of `raiseError` and `handleError`.
 - The differences between `handleError`, `handleErrorWith` and `attempt`.
 - The exit cases on raising and handling errors.

As the chunk propagation of `handleError` operators is standard, it is not described.

To understand how errors in `IO` effects are lifted into streams, read the relevant section of [evalMap](http://localhost:4242/effects/evalMap.html#error-propagation).

## Behaviour

An error is a mechanism for terminating a stream. Errors can be raised with `raiseError` or through an effect with the `eval` family of operators.

The raised error can be thought of as propagating to each stage. As the error propagates, each stage terminates and their associated finalizers are executed.

An error can be handled using any of the `handleError` family of operators. Handling an error prevents the termination of later stages. 

The initial input stream in which the error is raised cannot ever be recovered.

### raiseError

The `raiseError` operator raises an error into a stream. The stream terminates when an error is raised. 

The stream constructed by `raiseError` cannot output elements, so has an output type of `Nothing`. The raised error is not considered an output. It is propagated via a separate error channel.

In the example below, a stream is constructed by appending `raiseError` and a stream of a single character `a`. The error `Err` is raised when the stream is pulled on.

@:example(raiseError) {
  drawChunked = false
}

Note that `Stream('a')` is never run. The stream terminates when `Err` is raised, and the entire program terminates with the error.

### handleError

The `handleError` operator captures an error and instead outputs an element. The resulting stream terminates after the element is outputted. The input stream in which the error is raised is not recovered.

In the following example, the input stream raises an error `Err`. The `handleError` function outputs the character `x` on encountering the error. 

@:example(handleError) {
  drawChunked = false
}

When the resulting stream is pulled on, `Err` is raised in the input stream, but not propagated further. Instead, the character `x` is outputted. The program terminates successfully with the value `List('x')`.

Note that `Stream('a')` of the input stream is never run.  The input stream terminated when `Err` was raised, and cannot be recreated.

### handleErrorWith

The `handleErrorWith` operator is similar to `handleError`, however on encountering an error it constructs a stream and pulls elements from it.

In the following example, the input stream raises an error `Err`. The `handleErrorWith` operator constructs the stream of characters `a` and `b` on encountering the error.

@:example(handleErrorWith) {
  drawChunked = false
}

When the resulting stream is pulled on, `Err` is raised in the input stream, but not propagated further. Similar to `handleError`, the character `a` is outputted. When the resulting stream is next pulled on, the character `b` is outputted.

The program terminates successfully after pulling both characters.

### attempt

The `attempt` operator captures a potential error and outputs it as an `Either.Left` value. The resulting stream terminates after the error is outputted.

`attempt` is equivalent to `map(Right(_)).handleError(Left(_))`.

In the following example, the input stream outputs a character `'a'` before raising an error.

@:example(attempt) {
  drawChunked = false
}

The resulting stream, when pulled on, outputs `Right(a)`.  When next pulled on, the input stream raises an error. This is captured and outputted as `Left(Err)`.

Note that the error is always the final element pulled from the resulting stream. It is impossible for the resulting stream to output `Left` and then `Right`.

## Exit cases

### raiseError

If an error is raised in a stream, it will terminate with an exit case of `Errored`. This can be observed with `onFinalizeCase`.

@:example(raiseErrorExitCase) {
  drawChunked = false
}

### handleError

If an error is handled with any of the `handleError` family of operators, the resulting stream exits successfully.

@:example(handleErrorExitCase) {
  drawChunked = false
}

Note that even though the error is handled, the input stream still an exit case of `Errored`.
