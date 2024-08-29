## aquascape

Aquascape is a diagramming tool for [fs2](https://github.com/typelevel/fs2).

The diagrams can be used to illustrate the behaviour of fs2 operators. By reading the diagrams, you will learn more about the operators and streams.

This website contains an aquascape-based reference guide to fs2, based on aquascapes. Read it [here](reference/README.md).

## The basic idea

A stream is built from **stages**. When evaluated:

 - Each stage **pulls** on the stage above.
 - It **outputs** an element to the stage below.
 - If there are no more elements to output, the stage is **done**.
 - It might also evaluate an **effect**.
 - That effect might raise an **error**.

## How to read the diagrams

### Basics

```scala
Stream('a', 'b', 'c')
  .take(2)
  .compile.toList
```

![diagram](basic-example.png)

### Effects and errors

```scala
Stream('a', 'b', 'c')
  .evalMap(x => IO.raiseWhen(x == 'b')(Err))
  .compile.toList
```

![diagram](error-example.png)

## How to write the diagrams

Include aquascape as a library dependency:

```scala
libraryDependencies += "com.github.zainab-ali" %% "aquascape" % "@VERSION@"
```

Create an `AquascapeApp`:

```scala mdoc
import aquascape.*
import cats.effect.*
import fs2.*

object App extends AquascapeApp.Simple {

  def stream(using Scape[IO]) = {
    Stream(1, 2, 3)
      .stage("Stream(1, 2, 3)")       // `stage` introduces a stage.
      .evalMap(x => IO(x).trace())    // `trace` traces a side effect.
      .stage("evalMap(…)")
      .compile
      .toList
      .compileStage("compile.toList") // `compileStage` is used for the final stage.
      .void
  }
}
```

### Writing images

You can create a png by extending `AquascapeApp.Simple.File`.

```scala mdoc:nest
object App extends AquascapeApp.Simple.File("scape") {
  def stream(using Scape[IO]) = ???
}
```

Running the app creates `scape.png`.

### Drawing chunks

The `Aquascape.Simple` variants purposefully destroy chunks and hide them from the generated images. This lets us pretend that a single element is pulled and outputted.

To display chunks, extend the `Aquascape.Chunked` variants instead.

```scala mdoc:nest
object App extends AquascapeApp.Chunked {
  def stream(using Scape[IO]) = ???
}
```
### Handling concurrency

Aquascape can track most pulls and outputs by itself. It needs a bit of manual intervention for operators requiring `Concurrent`.

To achieve this, we introduce the concept of a *branch*. A branch is a portion of the scape that behaves sequentially.

The `fork` function relates two branches to each other. It must be inserted directly before each `Concurrent` operator.

As an example, `parEvalMap` requires a `Concurrent` instance:

```scala mdoc:nest
import cats.syntax.all.*

object App extends AquascapeApp.Simple {

  def stream(using Scape[IO]) = {
    Stream('a', 'b', 'c')
      .stage("Stream('a','b','c')", "upstream") // This stage is part of the `upstream` branch.
      .fork("root", "upstream")                 // Relate the `root` branch to the `upstream` branch.
      .parEvalMap(2)(_.pure[IO].trace())
      .stage("parEvalMap(2)(…)")                // This stage is part of the default `root` branch.
      .compile
      .drain
      .compileStage("compile.drain")            // Introduce a default branch named `root`.
  }
}
```

## More details

Watch the presentation on [The Pierian Stream, ScalaDays Seattle 2023](https://www.youtube.com/watch?v=q85Wi_485Es).
