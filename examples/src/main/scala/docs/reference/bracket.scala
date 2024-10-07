/*
 * Copyright 2023 Zainab Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package docs.reference

import aquascape.*
import aquascape.examples.*
import aquascape.examples.syntax.given
import cats.Show
import cats.effect.*
import cats.effect.IO
import cats.syntax.all.*
import fs2.*

import scala.concurrent.duration.*
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("DocsReferenceBracket")
object bracket {
  @JSExport
  val bracket = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .bracket(IO('a').trace())(_ => IO('b').trace().void)
          .stage("Stream.bracket(…)(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val bracketAppend = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        val ab = Stream
          .bracket(IO('a').trace())(_ => IO('b').trace().void)
          .stage("ab")
        val xy = Stream
          .bracket(IO('x').trace())(_ => IO('y').trace().void)
          .stage("xy")
        (ab ++ xy)
          .stage("ab ++ xy")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }

  @JSExport
  val resource = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        val res = Resource.make(IO('a').trace())(_ => IO('b').trace().void)
        Stream
          .resource(res)
          .stage("Stream.resource(res)")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }

  @JSExport
  val bracketCase = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .bracketCase(IO('a').trace())((_, exitCase) =>
            IO(exitCase.show).trace().void
          )
          .stage("Stream.bracketCase(…)(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val bracketCaseErrored = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .bracketCase(IO('a').trace())((_, exitCase) =>
            IO(exitCase.show).trace().void
          )
          .stage("Stream.bracketCase(…)(…)")
          .flatMap(_ => Stream.raiseError[IO](Err))
          .stage("flatMap(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val bracketCaseCanceled = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .bracketCase(IO('a').trace())((_, exitCase) =>
            IO(exitCase.show).trace().void
          )
          .stage("Stream.bracketCase(…)(…)")
          .flatMap(_ => Stream.sleep[IO](10.seconds))
          .stage("flatMap(…)")
          .interruptAfter(1.second)
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val onFinalize = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a')
          .onFinalize(IO('b').trace().void)
          .stage("Stream('a').onFinalize(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val onFinalizeCase = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a')
          .onFinalizeCase(exitCase => IO(exitCase.show).trace().void)
          .stage("Stream('a').onFinalizeCase(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val bracketAcquireError = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .bracket(IO.raiseError[Char](Err))(_ => IO('b').trace().void)
          .stage("Stream.bracket(…)(…)")
          .onFinalizeCase(exitCase => IO(exitCase.show).trace().void)
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val bracketReleaseError = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .bracket(IO('a'))(_ => IO.raiseError(Err).trace().void)
          .stage("Stream.bracket(…)(…)")
          .onFinalizeCase(exitCase => IO(exitCase.show).trace().void)
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }
}
