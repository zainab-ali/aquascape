#take
## fewer

@:example(TakeFewer)

## fewer (with chunks)

```scala
Stream('a', 'b', 'c').take(2).compile.toList

```
![diagram](take/fewer--with-chunks-.png)

## more

@:example(TakeMore)

## more (with chunks)

```scala
Stream('a', 'b', 'c').take(5).compile.toList

```
![diagram](take/more--with-chunks-.png)

## from an infinite stream

@:example(TakeFromAnInfiniteStream)

## from an infinite stream (with chunks)

```scala
Stream('a').repeat.take(2).compile.toList

```
![diagram](take/from-an-infinite-stream--with-chunks-.png)

## from a drained stream

@:example(TakeFromADrainedStream)

## from a drained stream (with chunks)

```scala
Stream('a', 'b', 'c').drain.take(2).compile.toList

```
![diagram](take/from-a-drained-stream--with-chunks-.png)
