package aquascape.examples

import aquascape.*
import cats.effect.*
import cats.effect.IO
import fs2.*
import cats.syntax.all.*

import scala.scalajs.js.annotation.JSExportTopLevel
import cats.Show
given Show[Nothing] = _ => sys.error("Unreachable code.")

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
  // TODO: Zainab - We get undefined rendered
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
        .count
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

object Keys {
  val examples = List(TakeMore, TakeFewer).map { e =>
    e.getClass.getSimpleName.stripSuffix("$")
  }
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

@JSExportTopLevel("ChunkingChunkChunkLimit")
object ChunkingChunkChunkLimit extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .stage("Stream('a','b','c')")
      .chunkLimit(2)
      .unchunks
      .stage("chunkLimit(2).unchunks")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("ChunkingChunkChunkMin")
object ChunkingChunkChunkMin extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .chunkLimit(1)
      .unchunks
      .stage("Stream('a','b','c')…unchunks")
      .chunkMin(2)
      .unchunks
      .stage("chunkMin(2).unchunks")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("ChunkingChunkRepartition")
object ChunkingChunkRepartition extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream("Hel", "l", "o Wor", "ld")
      .chunkLimit(1)
      .unchunks
      .stage("""Stream(…)…unchunks""")
      .repartition(s => Chunk.array(s.split(" ")))
      .stage("repartition(…)")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("BufferingBuffer")
object BufferingBuffer extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .chunkLimit(1)
      .unchunks
      .stage("Stream('a','b','c')…unchunks")
      .buffer(2)
      .stage("buffer(2)")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("BufferingBufferAll")
object BufferingBufferAll extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .chunkLimit(1)
      .unchunks
      .stage("Stream('a','b','c')…unchunks")
      .bufferAll
      .stage("bufferAll")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("BufferingBufferBy")
object BufferingBufferBy extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .chunkLimit(1)
      .unchunks
      .stage("Stream('a','b','c')…unchunks")
      .bufferBy(_ != 'b')
      .stage("bufferBy")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("CombiningAppend")
object CombiningAppend extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      (Stream('a', 'b')
        .stage("Stream('a','b')")
        ++ Stream('c').stage("Stream('c')"))
      .stage("++")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("CombiningFlatMap")
object CombiningFlatMap extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream("abc")
      .stage("""Stream("abc")""")
      .flatMap { str =>
        Stream
          .emits(str.toList)
          .stage("Stream.emits(str.toList)")
      }
      .stage("flatMap {…}")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

object Err extends Throwable("Err")

@JSExportTopLevel("CombiningFlatMapErrorPropagation")
object CombiningFlatMapErrorPropagation extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream("abc")
      .stage("""Stream("abc")""")
      .flatMap { _ =>
        Stream
          .raiseError[IO](Err)
          .stage("Stream.raiseError(Err)")
      }
      .stage("flatMap {…}")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("CombiningFlatMapErrorHandling")
object CombiningFlatMapErrorHandling extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream("abc")
      .stage("""Stream("abc")""")
      .flatMap { _ =>
        Stream
          .raiseError[IO](Err)
          .stage("Stream.raiseError(Err)")
      }
      .stage("flatMap {…}")
      .handleError(_ => 'a')
      .stage("handleError(_ => 'a')")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("CombiningFlatMapBracket")
object CombiningFlatMapBracket extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .stage("""Stream('a','b','c')""")
      .flatMap { x =>
        Stream.bracket(IO(s"<$x").trace())(_ => IO(s"$x>").trace().void)
      }
      .stage("flatMap {…}")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("CombiningMerge")
object CombiningMerge extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a')
      .stage("Stream('a')", branch = "left")
      .fork("root", "left")
      .merge(
        Stream('b')
        .stage("Stream('b')", branch = "right")
        .fork("root", "right")
      )
      .stage("merge")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("CombiningParZip")
object CombiningParZip extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .stage("Stream('a','b','c')", "left")
      .fork("root", "left")
      .parZip(
        Stream('d', 'e')
        .stage("Stream('d','e')", "right")
        .fork("root", "right")
      )
      .stage("parZip(…)")
      .compile
      .drain
      .compileStage("compile.drain")
    )
}
