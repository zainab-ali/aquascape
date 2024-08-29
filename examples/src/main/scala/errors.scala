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

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("DocsReferenceError")
object errors {

  @JSExport
  val raiseError = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        val input = Stream.raiseError[IO](Err) ++ Stream('a')
        input
          .stage("input")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }

  @JSExport
  val handleError = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        val input = Stream.raiseError[IO](Err) ++ Stream('a')
        input
          .stage("input")
          .handleError(_ => 'x')
          .stage("handleError(_ => 'x')")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }

  @JSExport
  val handleErrorWith = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .raiseError[IO](Err)
          .stage("Stream.raiseError[IO](Err)")
          .handleErrorWith(_ => Stream('a', 'b').stage("Stream('a','b')"))
          .stage("handleErrorWith(_ => …)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val attempt = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        val input = Stream('a') ++ Stream.raiseError[IO](Err)
        input
          .stage("input")
          .attempt
          .stage("attempt")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }

  @JSExport
  val raiseErrorExitCase = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .raiseError[IO](Err)
          .stage("Stream.raiseError[IO](Err)")
          .onFinalizeCase(exitCase => IO(exitCase.show).trace_())
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val handleErrorExitCase = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .raiseError[IO](Err)
          .onFinalizeCase(exitCase => IO(show"raised-$exitCase").trace_())
          .stage("Stream.raiseError[IO](Err)")
          .handleError(_ => 'a')
          .onFinalizeCase(exitCase => IO(show"handled-$exitCase").trace_())
          .stage("handleError(_ => 'a')")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

}
