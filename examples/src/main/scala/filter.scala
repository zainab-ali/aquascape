package docs.reference

import aquascape.*
import aquascape.examples.*
import cats.Show
import cats.effect.*
import cats.effect.IO
import cats.syntax.all.*
import fs2.*

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("DocsReferenceFilter")
object filter {

  @JSExport
  val filter = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .repeatN(2)
          .stage("Stream('a','b', 'c').repeatN(2)")
          .filter(_ == 'b')
          .stage("filter(_ == 'b')")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val filterNot = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .repeatN(2)
          .stage("Stream('a','b', 'c').repeatN(2)")
          .filterNot(_ == 'b')
          .stage("filterNot(_ == 'b')")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val mapFilter = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .repeatN(2)
          .stage("Stream('a','b', 'c').repeatN(2)")
          .mapFilter(c => if (c == 'b') Some(c.toInt) else None)
          .stage("mapFilter(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val filterWithPrevious = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .repeatN(2)
          .stage("Stream('a', 'b', 'c').repeatN(2)")
          .filterWithPrevious((p, c) => p <= c)
          .stage("filterWithPrevious(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val changes = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'a', 'b', 'c', 'c', 'b')
          .stage("Stream('a' … 'b')")
          .changes
          .stage("changes")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val filterChunked = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .repeatN(2)
          .stage("Stream('a', 'b', 'c').repeatN(2)")
          .filter(_ != 'b')
          .stage("filter(_ != 'b')")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

}
