#filtering
## filter

@:example(FilteringFilter)

## filter (with chunks)

```scala
Stream('a', 'b', 'c').filter(_ == 'b').compile.toList

```
![diagram](filtering/filter--with-chunks-.png)

## exists

@:example(FilteringExists)

## exists (with chunks)

```scala
Stream('a', 'b', 'c').exists(_ == 'b').compile.toList

```
![diagram](filtering/exists--with-chunks-.png)

## forall

@:example(FilteringForall)

## forall (with chunks)

```scala
Stream('a', 'b', 'c').forall(_ == 'b').compile.toList

```
![diagram](filtering/forall--with-chunks-.png)

## changes

@:example(FilteringChanges)

## changes (with chunks)

```scala
Stream('a', 'b', 'b', 'a', 'c').changes.compile.toList

```
![diagram](filtering/changes--with-chunks-.png)
