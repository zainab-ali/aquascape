package aquascape.examples

import aquascape.*
import cats.effect.*
import cats.effect.IO
import fs2.*
import cats.syntax.all.*

import scala.scalajs.js.annotation.JSExportTopLevel
import cats.Show
import scala.concurrent.duration.*

object EitherThrowableCharShow {
  given Show[Either[Throwable, Char]] = {
    case Left(Scape.Caught(err)) => s"Left(${err.getMessage})"
    case Left(err)               => s"Left(${err.getMessage})"
    case Right(c)                => s"Right(${c.show})"
  }
}

object NothingShow {
  given Show[Nothing] = _ => sys.error("Unreachable code.")
}

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
  import NothingShow.given
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
  import EitherThrowableCharShow.given
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
  import NothingShow.given

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
@JSExportTopLevel("BroadcastThroughBasic")
object BroadcastThroughBasic extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .chunkLimit(1)
        .unchunks
        .stage("Stream('a','b','c')", branch = "upstream")
        .evalTap(x => IO.raiseWhen(x == 'b')(Err))
        .stage("evalTap(…)", branch = "upstream")
        .fork("root", "upstream")
        .broadcastThrough(in =>
          in.metered(1.second)
            .stage("in.metered(…)", branch = "broadcast")
            .fork("root", "broadcast")
        )
        .stage("broadcastThrough")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("BroadcastThroughErrorPropagation")
object BroadcastThroughErrorPropagation extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .chunkLimit(1)
        .unchunks
        .stage("Stream('a','b','c')", branch = "upstream")
        .evalTap(x => IO.raiseWhen(x == 'b')(Err))
        .stage("evalTap(…)", branch = "upstream")
        .fork("root", "upstream")
        .broadcastThrough(in =>
          in.metered(1.second)
            .stage("in.metered(…)", branch = "broadcast")
            .fork("root", "broadcast")
        )
        .stage("broadcastThrough")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("BroadcastThroughDifferentRates")
object BroadcastThroughDifferentRates extends Example {
  // TODO: Zainab - This is not rendering correctly
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .chunkLimit(1)
        .unchunks
        .stage("Stream('a','b', 'c')…", branch = "upstream")
        .fork("root", "upstream")
        .broadcastThrough(
          in =>
            in.metered(1.second)
              .stage("in.metered(1s)", branch = "broadcast1")
              .fork("root", "broadcast1"),
          in =>
            in.metered(100.second)
              .stage("in.metered(100s)", branch = "broadcast2")
              .fork("root", "broadcast2")
        )
        .stage("broadcastThrough")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("EffectsEvalMap")
object EffectsEvalMap extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .evalMap(_.pure[IO].trace())
        .stage("evalMap")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("EffectsEvalMap2")
object EffectsEvalMap2 extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .evalTap(char => IO(s"$char 1").trace())
        .stage("evalTap1")
        .evalTap(char => IO(s"$char 2").trace())
        .stage("evalTap2")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("EffectsExec")
object EffectsExec extends Example {
  import EitherThrowableCharShow.given
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream
        .exec(IO('a').trace().void)
        .stage("Stream.exec(…)")
        .compile
        .last
        .compileStage("compile.last")
    )
}

@JSExportTopLevel("EffectsEval")
object EffectsEval extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream
        .eval(IO('a').trace())
        .stage("Stream.eval(…)")
        .compile
        .last
        .compileStage("compile.last")
    )
}

@JSExportTopLevel("EffectsParEvalMap")
object EffectsParEvalMap extends Example {
  // TODO: Zainab - This isn't rendering
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c', 'd', 'e')
        .stage("Stream('a','b','c', 'd', 'e')", "upstream")
        .fork("root", "upstream")
        .parEvalMap(2)(char =>
          IO.sleep((105 - char.toInt).seconds).as(char).trace()
        )
        .stage("parEvalMap(2)(…)")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("EffectsParEvalMapUnordered")
object EffectsParEvalMapUnordered extends Example {
  // TODO: Zainab - This isn't rendering
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c', 'd', 'e')
        .stage("Stream('a','b','c', 'd', 'e')", "upstream")
        .fork("root", "upstream")
        .parEvalMapUnordered(2)(char =>
          IO.sleep((105 - char.toInt).seconds).as(char).trace()
        )
        .stage("parEvalMapUnordered(2)(…)")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("ErrorsRaisingErrorsRaiseError")
object ErrorsRaisingErrorsRaiseError extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      (Stream.raiseError[IO](Err) ++ Stream('a'))
        .stage("Stream.raiseError[IO](Err)")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("ErrorsRaisingErrorsInEvalMap")
object ErrorsRaisingErrirsInEvalMap extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .evalMap(x => IO.raiseWhen(x == 'b')(Err).trace())
        .stage("evalMap(IO.raiseWhen(_ == 'b')(Err))")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("ErrorsRaisingErrorsPropagation")
object ErrorsRaisingErrorsPropagation extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .evalMap(x => IO.raiseWhen(x == 'b')(Err))
        .stage("evalMap(IO.raiseWhen(_ == 'b')(Err))")
        .map(identity)
        .stage("map(identity)")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("ErrorsHandlingErrorsHandleError")
object ErrorsHandlingErrorsHandleError extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .evalTap(x => IO.raiseWhen(x == 'b')(Err))
        .stage("evalTap(IO.raiseWhen(_ == 'b')(Err))")
        .handleError(_ => 'd')
        .stage("handleError(_ => 'd')")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}
@JSExportTopLevel("ErrorsHandlingErrorsHandleError2")
object ErrorsHandlingErrorsHandleError2 extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .evalMap(x =>
          IO.raiseWhen(x == 'b')(Err)
            .as(x)
            .handleError(_ => 'd')
            .trace()
        )
        .stage("evalTap(…)")
        .handleError(_ => 'd')
        .stage("handleError(_ => 'd')")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}
@JSExportTopLevel("ErrorsHandlingErrorsHandleErrorWith")
object ErrorsHandlingErrorsHandleErrorWith extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .evalTap(x => IO.raiseWhen(x == 'b')(Err))
        .stage("evalTap(…)")
        .handleErrorWith(_ =>
          Stream('d', 'e', 'f').stage("Stream('d','e','f')")
        )
        .stage("handleErrorWith(_ => …)")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("ErrorsHandlingErrorsAttempt")
object ErrorsHandlingErrorsAttempt extends Example {
  import EitherThrowableCharShow.given
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .evalTap(x => IO.raiseWhen(x == 'b')(Err))
        .stage("evalTap(…)")
        .attempt
        .stage("attempt")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("ErrorsHandlingErrorsAttempts")
object ErrorsHandlingErrorsAttempts extends Example {

  import EitherThrowableCharShow.given
  def apply(using Scape[IO]): StreamCode = {
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .evalTap(x => IO.raiseWhen(x == 'b')(Err))
        .stage("evalTap(…)")
        .attempts(Stream.empty)
        .stage("attempts")
        .take(4)
        .compile
        .toList
        .compileStage("compile.toList")
    )

  }
}

@JSExportTopLevel("ResourcesBracket")
object ResourcesBracket extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream
      .bracket(IO("abc").trace())(_ => IO("d").trace().void)
      .stage("Stream.bracket(…)")
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
@JSExportTopLevel("ResourcesBracketRaisingErrors")
object ResourcesBracketRaisingErrors extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream
      .bracket(IO("abc").trace())(_ => IO("d").trace().void)
      .stage("Stream.bracket(…)")
      .flatMap { str =>
        Stream
          .emits(str.toList)
          .stage("Stream.emits(str.toList)")
          .evalTap(x => IO.raiseWhen(x == 'b')(Err))
          .stage("evalTap(…)")
      }
      .stage("flatMap {…}")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("ResourcesBracketHandlingErrors")
object ResourcesBracketHandlingErrors extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream
      .bracket(IO("abc").trace())(_ => IO("d").trace().void)
      .stage("Stream.bracket(…)")
      .flatMap { str =>
        Stream
          .emits(str.toList)
          .stage("Stream.emits(str.toList)")
          .evalTap(x => IO.raiseWhen(x == 'b')(Err))
          .stage("evalTap(…)")
      }
      .stage("flatMap1")
      .flatMap { str =>
        Stream(str)
          .repeatN(2)
          .stage("Stream.(str).repeatN(2)")
          .evalTap(x => IO.raiseWhen(x == 'b')(Err))
          .stage("evalTap1(…)")
      }
      .stage("flatMap {…}")
      .handleErrorWith(_ => Stream('e', 'f').stage("Stream('e','f')"))
      .stage("handleErrorWith(…)")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("TimeAwakeEvery")
object TimeAwakeEvery extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream
      .awakeEvery[IO](5.seconds)
      .map(_.toSeconds)
      .stage("Stream.awakeEvery(5.seconds).map(…)")
      .take(2)
      .stage("take(2)")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("TimeDelayBy")
object TimeDelayBy extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .chunkLimit(1)
      .unchunks
      .stage("Stream('a','b','c')…unchunks")
      .delayBy(5.seconds)
      .stage("delayBy(5.seconds)")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("TimeMetered")
object TimeMetered extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .chunkLimit(1)
      .unchunks
      .stage("Stream('a','b','c')…unchunks")
      .metered(5.seconds)
      .stage("metered(5.seconds)")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("TimeDebounce")
object TimeDebounce extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
      .chunkLimit(1)
      .unchunks
      .stage("Stream('a','b','c')…unchunks")
      .debounce(5.seconds)
      .stage("debounce(5.seconds)")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}

@JSExportTopLevel("TimeDebounceAwake")
object TimeDebounceAwake extends Example {
  // TODO: Zainab - Run time examples with cats-effect-testkit
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream
      .awakeEvery[IO](1.seconds)
      .map(_.toSeconds)
      .stage("Stream.awakeEvery(1.seconds)…take(5)", "upstream")
      .fork("root", "upstream")
      .debounce(2.seconds)
      .stage("debounce(2.seconds)")
      .take(2)
      .stage("take(2)")
      .compile
      .toList
      .compileStage("compile.toList")
    )
}
