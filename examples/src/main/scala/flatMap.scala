package docs.reference

import aquascape.*
import aquascape.examples.*
import cats.Show
import cats.effect.*
import cats.syntax.all.*
import cats.effect.IO
import fs2.*

import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.annotation.JSExport

@JSExportTopLevel("DocsReferenceFlatMap")
object flatMap {
  @JSExport
  val basic = new Example {

    def apply(using Scape[IO]): StreamCode =
      code(
        Stream("ab")
          .stage("""Stream("ab")""")
          .flatMap(str =>
            Stream
              .emits(str.toList)
              .stage("Stream.emits(str.toList)")
          )
          .stage("flatMap(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val multipleInputElements = new Example {

    def apply(using Scape[IO]): StreamCode =
      code(
        Stream("ab", "xy")
          .stage("""Stream("ab", "xy")""")
          .flatMap(str =>
            Stream
              .emits(str.toList)
              .stage("Stream.emits(str.toList)")
          )
          .stage("flatMap(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val errorPropagation = new Example {
    import EitherThrowableCharShow.given
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream("ab")
          .stage("""Stream("ab")""")
          .flatMap { _ =>
            Stream
              .raiseError[IO](Err)
              .stage("Stream.raiseError(Err)")
          }
          .stage("flatMap {…}")
          .compile
          .drain
          .compileStage("compile.drain")
      )
  }

  @JSExport
  val errorHandling = new Example {
    import NothingShow.given

    def apply(using Scape[IO]): StreamCode =
      code(
        Stream("ab")
          .stage("""Stream("ab")""")
          .flatMap { _ =>
            Stream
              .raiseError[IO](Err)
              .stage("Stream.raiseError(Err)")
          }
          .stage("flatMap {…}")
          .handleError(_ => 'z')
          .stage("handleError(_ => 'z')")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }
  @JSExport
  val errorHandlingInput = new Example {

    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .raiseError[IO](Err)
          .covaryOutput[Char]
          .stage("Stream.raiseError(Err)")
          .flatMap { _ =>
            Stream('a', 'b')
              .stage("Stream('a', 'b')")
          }
          .stage("flatMap {…}")
          .handleError(_ => 'z')
          .stage("handleError(_ => 'z')")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val finalizerInput = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream("ab")
          .onFinalize(IO("ab").trace_())
          .stage("""Stream("ab")""")
          .flatMap(str =>
            Stream.emits(str.toList).stage("Stream.emits(str.toList)")
          )
          .stage("flatMap(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }
  @JSExport
  val finalizerChild = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream("ab", "xy")
          .stage("""Stream("ab", "xy")""")
          .flatMap { str =>
            Stream
              .emits(str.toList)
              .stage("Stream.emits(str.toList)")
              .onFinalize(IO(str).trace_())
          }
          .stage("flatMap {…}")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }
}
