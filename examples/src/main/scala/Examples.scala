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

@JSExportTopLevel("TakeFewer")
object TakeFewer extends Example {
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

@JSExportTopLevel("TakeMore")
object TakeMore extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .stage("Stream('a','b','c')")
      .take(5)
      .stage("take(5)")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}
@JSExportTopLevel("TakeFromAnInfiniteStream")
object TakeFromAnInfiniteStream extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a').repeat
      .stage("Stream('a').repeat")
      .take(2)
      .stage("take(2)")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}
@JSExportTopLevel("TakeFromADrainedStream")
object TakeFromADrainedStream extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .stage("Stream('a','b','c')")
      .drain
      .stage("drain")
      .take(2)
      .stage("take(2)")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}
@JSExportTopLevel("DropFewer")
object DropFewer extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .stage("Stream('a','b','c')")
      .drop(2)
      .stage("drop(2)")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("DropMore")
object DropMore extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .stage("Stream('a','b','c')")
      .drop(5)
      .stage("drop(5)")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}
@JSExportTopLevel("FilteringFilter")
object FilteringFilter extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .stage("Stream('a','b','c')")
      .filter(_ == 'b')
      .stage("filter(_ == 'b')")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}
@JSExportTopLevel("FilteringExists")
object FilteringExists extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .stage("Stream('a','b','c')")
      .exists(_ == 'b')
      .stage("exists(_ == 'b')")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}
@JSExportTopLevel("FilteringForall")
object FilteringForall extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .stage("Stream('a','b','c')")
      .forall(_ == 'b')
      .stage("forall(_ == 'b')")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}
@JSExportTopLevel("FilteringChanges")
object FilteringChanges extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'b', 'a', 'c')
      .stage("Stream('a','b','b','a','c')")
      .changes
      .stage("changes")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}
