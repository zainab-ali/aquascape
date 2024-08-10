# compile
## toList

@:example(BasicCompileToList)

## toList (with chunks)

```scala
Stream('a', 'b', 'c').compile.toList

```
![diagram](compile/toList--with-chunks-.png)

## drain

@:example(BasicCompileDrain)

## drain (with chunks)

```scala
Stream('a', 'b', 'c').compile.drain

```
![diagram](compile/drain--with-chunks-.png)

## last

@:example(BasicCompileLast)

## last (with chunks)

```scala
Stream('a', 'b', 'c').compile.last

```
![diagram](compile/last--with-chunks-.png)

## count

@:example(BasicCompileCount)

## count (with chunks)

```scala
Stream('a', 'b', 'c').compile.count

```
![diagram](compile/count--with-chunks-.png)

## onlyOrError

@:example(BasicCompileOnlyOrError)

## onlyOrError (with chunks)

```scala
Stream('a', 'b', 'c').compile.onlyOrError

```
![diagram](compile/onlyOrError--with-chunks-.png)
