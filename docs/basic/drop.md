#drop
## fewer

@:example(DropFewer)

## fewer (with chunks)

```scala
Stream('a', 'b', 'c').drop(2).compile.toList

```
![diagram](drop/fewer--with-chunks-.png)

## more

@:example(DropMore)

## more (with chunks)

```scala
Stream('a', 'b', 'c').drop(5).compile.toList

```
![diagram](drop/more--with-chunks-.png)
