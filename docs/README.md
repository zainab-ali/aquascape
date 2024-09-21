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

The following code snippet corresponds to the diagram below it:

```scala
Stream('a', 'b', 'c')
  .take(2)
  .compile.toList
```

![diagram](basic-example.png)

See [how to read the diagrams](how-to-read-the-diagrams.md) for a full symbol reference.

## How to write the diagrams

Extend `AquascapeApp`:

```scala
// TestApp.scala
//> using dep com.github.zainab-ali::aquascape::@VERSION@

import aquascape.*
import cats.effect.*
import fs2.*

object App extends AquascapeApp {

  def name: String = "aquascapeFrame"
  
  def stream(using Scape[IO]): IO[Unit] = {
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

Run the app with Scala `3.5.0` and above:
```sh
scala run App.scala
```

This produces an `aquascapeFrame.png` image.


### Embedding SVGs in HTML

Package the app with Scala `3.5.0` and above to produce a `App.js` file.

```sh
scala --power package --js-version 1.16.0 --js App.scala
```

Include this as a script:
```
<html>
  <head>
    <script src="App.js" type="text/javascript"></script>
  </head>
  <body>
    <div id="aquascapeFrame">
  </body>
</html>
```

### Drawing chunks

By default, the `AquascapeApp` purposefully uses singleton chunks, and hides them from the generated images. This lets us pretend that a single element is pulled and outputted.

To display chunks, override the `chunked` function.

```scala mdoc:nest
import aquascape.*
import cats.effect.*

object App extends AquascapeApp {
  def name: String = "aquascapeFrame"
  override def chunked: Boolean = true
  def stream(using Scape[IO]) = ???
}
```
### Handling concurrency

Aquascape can track most pulls and outputs by itself. It needs a bit of manual intervention for operators requiring `Concurrent`.

To achieve this, we introduce the concept of a *branch*. A branch is a portion of the scape that behaves sequentially.

The `fork` function relates two branches to each other. It must be inserted directly before each `Concurrent` operator.

As an example, `parEvalMap` requires a `Concurrent` instance:

```scala mdoc:nest
import fs2.*
import cats.syntax.all.*

object App extends AquascapeApp {
  def name: String = "aquascapeFrame"
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
