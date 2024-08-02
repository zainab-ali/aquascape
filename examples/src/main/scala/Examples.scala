package aquascape.examples

import aquascape.*
import cats.effect.*
import cats.effect.IO
import fs2.*

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("BasicCompileToList")
object BasicCompileToList extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("BasicCompileDrain")
object BasicCompileDrain extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .compile
        .drain
        .compileStage("compile.drain")
    )
}

@JSExportTopLevel("BasicCompileLast")
object BasicCompileLast extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .compile
        .last
        .compileStage("compile.last")
    )
}

@JSExportTopLevel("BasicCompileCount")
object BasicCompileCount extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .compile
        .last
        .compileStage("compile.count")
    )
}

@JSExportTopLevel("BasicCompileOnlyOrError")
object BasicCompileOnlyOrError extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .compile
        .onlyOrError
        .compileStage("compile.onlyOrError")
    )
}

@JSExportTopLevel("TakeScape")
object TakeScape extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .take(2)
        .stage("take(2)")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}
