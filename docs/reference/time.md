{%
  pageid = DocsReferenceTime
  helium.site.pageNavigation.keepOnSmallScreens = true
%}

# time

This is a reference guide to the time delay family of operators. It describes the behaviour of `sleep`, `delayBy`, `fixedDelay`, `awakeDelay`, `fixedRate`, `awakeEvery`, `spaced` and `metered` and their `startImmediately` variants.

## Behaviour

All operators introduce delays. They are distinguished by:
 - whether they introduce a delay after the pull or the last outputted element. The `fixedDelay` family of operators introduce delays after the pull, whereas `fixedRate` introduces a delay after the last outputted element.
 - Whether they accept an input stream, or construct a stream. `delayBy`, `spaced` and `metered` operate on an input stream, but all other operators construct a stream.
 - Whether they output a unit value `()` or the elapsed time. Operators prefixed by `fixed` output a unit value, but operators prefixed by `awake` output the elapsed time.
 
This is summarized in the table below.

| output value  | delay after pull | delay after output |
|---------------|-----------------|-------------------|
| unit          | `fixedDelay`    | `fixedRate`       |
| elapsed time  | `awakeDelay`    | `awakeEvery`      |
| input value   | `spaced`        | `metered`         |

### sleep

The `sleep` operator sleeps for a given amount of time. When pulled on, it waits for the given time to elapse before outputting the unit value `()`. The stream is then done.

@:example(sleep) {
  drawChunked = false
}

### delayBy

The `delayBy` operator sleeps for a given amount of time before pulling on its input stream. 

In the following example, the input stream of characters is delayed by one second. When the resulting stream is pulled on, one second elapses before `delayBy` pulls on the input stream. The input stream is then pulled on as normal. 


@:example(delayBy) {
  drawChunked = false
}

### fixedDelay

The `fixedDelay` operator repeatedly sleeps for a given amount of time.

Each time it is pulled on, it waits for the given time to elapse before outputting an element.

@:example(fixedDelay) {
  drawChunked = false
}


`Stream.fixedDelay(n)` is equivalent to `Stream.sleep(n).repeat`.

####  Slow element evaluation

The `fixedDelay` operator sleeps for the given amount of time, regardless of the time elapsed since its last output.

This is shown in the following example.  The resulting stream is operated on by `evalMap(_ => IO.sleep(2.seconds))`. When `fixedDelay` outputs an element, there is a delay of two seconds before it is next pulled on. This does not affect the time that `fixedDelay` sleeps for: after it is pulled on, it still delays for one second.

@:example(fixedDelaySlowElementEval) {
  drawChunked = false
}

### awakeDelay

The `awakeDelay` operator is similar to `fixedDelay`. It sleeps for a given amount of time before outputting a duration, then repeats infinitely. The duration corresponds to the elapsed time since the resulting stream was first pulled on.

@:example(awakeDelay) {
  drawChunked = false
}

### fixedRate

The `fixedRate` operator constructs a stream that outputs the unit value `()` at most every given time period.

#### Instant element evaluation
The following example constructs a stream that outputs `()` every second. 

@:example(fixedRate) {
  drawChunked = false
}

Note that when output evaluation is instantaneous, the `fixedRate` operator behaves similarly to `fixedDelay`.

#### Fast element evaluation

When pulled on, the `fixedRate` operator accounts for the time elapsed since its last output.

If the elapsed time is shorter than the given time period, `fixedRate` sleeps for the remainder of the period.

In the following example, `fixedRate` constructs a stream that outputs an element at most every three seconds.

The resulting stream is operated on by `evalMap(_ => IO.sleep(1.second))`. This introduces a delay of one second between the output and pull of `fixedRate`.

Note that the second time `fixedDelay` is pulled on, it delays by only two seconds. This ensures that the time elapsed between elements is the given rate of three seconds.

@:example(fixedRateFastElementEval) {
  drawChunked = false
}

#### Slow element evaluation

If the elapsed time is longer than the given time period, `fixedRate` outputs an element immediately.

In the following example, `fixedRate` constructs a stream that outputs at most every one second. The resulting stream is operated on by `evalMap(_ => IO.sleep(2.seconds))`. This introduces a two second delay between the output and pull of `fixedRate`.

Note that when the `fixedRate` operator is pulled on for the second time, it outputs `()` immediately. This is because over one second has passed since it previously outputted an element.

@:example(fixedRateSlowElementEval) {
  drawChunked = false
}

#### Damping

The fixedRate operator is damped. It will only output a single element, no matter the length of the elapsed time.

In the following example, `fixedRate` is used to construct a stream that outputs at most every two seconds. The resulting stream is operated on by `evalMap`, which introduces a delay of five seconds before outputting the first element. All remaining elements are outputted immediately.

@:example(fixedRateDamped) {
  drawChunked = false
}

By delaying for five seconds, over two periods have elapsed since the last output. The third unit value is still delayed.

This ensures that `fixedRate` outputs elements at as close as possible to the given time period. Elements are outputted at `2s`, `5s,` and `6s`.

#### Undamped

The `fixedRate` operator accepts a second `dampen` parameter. If it is set to `false`, the operator will output as many elements as periods that have elapsed.


The following example constructs the same stream as above, but `dampen` is set to `false`.

@:example(fixedRateUndamped) {
  drawChunked = false
}

Note that the third time `fixedRate` is pulled on, `()` is outputted immediately. Because over two periods have elapsed since the last output, two unit values are outputted immediately.

Elements are outputted at `2s`, `5s`, `5s` and `6s`. 

### fixedRateStartImmediately

The `fixedRateStartImmediately` operator is similar to `fixedRate`. It outputs elements at most every given time period. Unlike `fixedRate`, it outputs its first value without a delay.

@:example(fixedRateStartImmediately) {
  drawChunked = false
}

### awakeEvery

The `awakeEvery` operator is similar to `fixedRate`. It outputs a duration value at most every given time period. The duration corresponds to the time elapsed since the stream was first pulled on.

@:example(awakeEvery) {
  drawChunked = false
}

### spaced

The `spaced` operator delays by a given time period before pulling on its input stream. It does not account for the time elapsed since its last output.

@:example(spaced) {
  drawChunked = false
}

Note that `spaced` pulls on its input stream immediately.



### metered

The `metered` operator pulls on its input stream at most every given time period. When pulled on, it awaits until the given time period has elapsed since its last output before pulling on its input stream.

It has similar behaviour to `fixedRate` on slow output or input evaluation. 

#### Instant evaluation

If the input stream outputs elements instantly, and the resulting stream is pulled on directly after its outputs, then `metered` delays by the given time period.

@:example(metered) {
  drawChunked = false
}

Note that it behaves exactly as `spaced`.

#### Slow output evaluation

There may be a delay between the resulting stream outputting an element and being pulled on. In this case, `metered` behaves as `fixedRate` and accounts for the time elapsed since its last output.

@:example(meteredSlowElementEval) {
  drawChunked = false
}

#### Slow input stream evaluation

Similarly, there may be a delay between the input stream being pulled on and outputting an element. `metered` behaves accounts for the time elapsed since its last output.

@:example(meteredSlowInputEval) {
  drawChunked = false
}

### meteredStartImmediately

The `meteredStartImmediately` operator is similar to `metered`, however pulls on its input stream immediately when it is first pulled on.

@:example(meteredStartImmediately) {
  drawChunked = false
}

## Chunk propagation

By necessity, operators that introduce time delays output singleton chunks.

### delayBy

The `delayBy` operator preserves the chunks of its input stream.

@:example(delayBy) {
  prefix = chunked
  drawChunked = true
}

### spaced

The `spaced` operator outputs singleton chunks.

@:example(spaced) {
  prefix = chunked
  drawChunked = true
}

### metered

The `metered` operator outputs singleton chunks.

@:example(metered) {
  prefix = chunked
  drawChunked = true
}

