# How to write the diagrams

You can create aquascapes yourself by using the `aquascape` library.

If you're desigining a program with fs2, a custom aquascape is a good tool for understanding how it behaves.

If you write fs2 related technical articles, presentations or documentation, you can use an aquascape to enhance them.

## Quick start

Extend `AquascapeApp`:

```scala
// App.scala
//> using dep com.github.zainab-ali::aquascape::@VERSION@

import aquascape.*
import cats.effect.*
import fs2.*

object App extends AquascapeApp {

  def name: String = "aquascapeFrame" // The name of the png file (Scala) or HTML frame id (Scala.js)
  
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
### Write a PNG image file

Run the app with Scala `3.5.0` and above:
```sh
scala run App.scala
```

Congratulations! You've produced an `aquascapeFrame.png` aquascape.


### Embed an SVG in HTML

Package the app with Scala `3.5.0` and above to produce a `App.js` file.

```sh
scala --power package --js-version @SCALAJS_VERSION@ --js App.scala
```

Include this as a script:
```html
<html>
  <head>
    <script src="App.js" type="text/javascript"></script>
  </head>
  <body>
    <div id="aquascapeFrame"></div>
  </body>
</html>
```

Open up your HTML page to see your aquascape.

## How to draw chunks

By default, the `AquascapeApp` purposefully uses singleton chunks, and hides them from the generated images. This lets us pretend that a single element is pulled and outputted.

If you want to investigate chunk preservation properties, you can leave chunks as they are and display them by overriding the `chunked` function.

```scala mdoc:nest
import aquascape.*
import cats.effect.*

object App extends AquascapeApp {
  def name: String = "aquascapeFrame"
  override def chunked: Boolean = true
  def stream(using Scape[IO]) = ???
}
```
## How to draw concurrent processes

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

## How to draw a batch of aquascapes

You can generate multiple aquascapes in a single `App` by extending `AquascapeApp.Batch` and defining a list of `aquascapes`:

```scala mdoc:nest:silent
// App.scala
//> using dep com.github.zainab-ali::aquascape::@VERSION@

import aquascape.*
import cats.effect.*
import fs2.*

val firstAquascape = new Aquascape.Simple {
  def name: String = "firstFrame"
  def stream(using Scape[IO]): IO[Unit] = {
    Stream(1, 2, 3)
      .stage("Stream(1, 2, 3)")
      .compile
      .toList
      .compileStage(
        "compile.toList"
      )
      .void
  }
}

val secondAquascape = new Aquascape.Simple {
  def name: String = "secondFrame"
  def stream(using Scape[IO]): IO[Unit] = {
    Stream(4, 5, 6)
      .stage("Stream(4, 5, 6)")
      .compile
      .toList
      .compileStage(
        "compile.toList"
      )
      .void
  }
}

object App extends AquascapeApp.Batch {
  val aquascapes: List[Aquascape] = List(firstAquascape, secondAquascape)
}
```
### Embed a batch of SVGs in HTML

[Package the app](#embed-an-svg-in-html) as before, then include it in HTML with a `<div>` per aquascape:

```html
<html>
  <head>
    <script src="App.js" type="text/javascript"></script>
  </head>
  <body>
    <div id="firstFrame"></div>
    <div id="secondFrame"></div>
  </body>
</html>
```
## How to export code snippets

You can export code snippets along with their aquascapes using the `streamCode` and `code` functions. These come in handy when embedding aquascapes into blog posts and docs.

@:todo(This snippet must be manually checked due to classpath issues when running scalafmt)
```scala
// App.scala
//> using dep com.github.zainab-ali::aquascape::@VERSION@

import aquascape.*
import cats.effect.*
import fs2.*

val basicScape = new Aquascape { // Extend `Aquascape` instead of `Aquascape.Simple`
  def name: String = "aquascapeFrame"
  def streamCode(using Scape[IO]): StreamCode = code { // Call the `code` macro
    Stream(1, 2, 3)
      .stage("Stream(1, 2, 3)")
      .compile
      .toList
      .compileStage(
        "compile.toList"
      )
      .void
  }
}

object App extends AquascapeApp.Batch {
  val aquascapes: List[Aquascape] = List(basicScape)
}
```

### Embedding code snippets in HTML

[Package the app](#embed-an-svg-in-html) as before, then include it in HTML along with a `<code>` element. The `<code>` element must have an id corresponding to the aquascape:

```html
<html>
  <head>
    <script src="App.js" type="text/javascript"></script>
  </head>
  <body>
    <code id="aquascapeFrameCode"></code>
    <div id="aquascapeFrame"></div>
  </body>
</html>
```

## Best practices

A good aquascape is a simple, informative diagram. It helps readers understand how a stream system behaves.

Unfortunately, aquascapes can easily become too complex to follow.

Stick to these best practices to generate good aquascapes:

 - Showcase a specific behaviour. Think about what you want the reader to learn when viewing the aquascape. Remove any details that aren't needed to show it, such as extra stages and data.
 - Don't use a stream of real data. Use a stream of characters as input instead. Single characters are rendered predictably, and fit in the diagram.
 - Use as few stages as possible, ideally no more than four. You should try to describe an operator using an input, middle, and output stage.
 - Give stages short names that correspond to the code. Your aquascape shouldn't be full of long stage names.

Good luck, and enjoy aquascaping!
