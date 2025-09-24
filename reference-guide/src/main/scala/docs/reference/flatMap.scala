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
import cats.syntax.all.*
import fs2.*

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("DocsReferenceFlatMap")
object flatMap {
  @JSExport
  val basic = new Example {

    def apply(using Scape[IO]): StreamCode =
      code(
        Stream("ab")
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
  val multipleInputElements = new Example {

    def apply(using Scape[IO]): StreamCode =
      code(
        Stream("ab", "xy")
          .stage("""Stream("ab", "xy")""")
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
  val errorPropagation = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream("ab")
          .stage("""Stream("ab")""")
          .flatMap(_ =>
            Stream.raiseError[IO](Err).stage("Stream.raiseError(Err)")
          )
          .stage("flatMap(…)")
          .compile
          .drain
          .compileStage("compile.drain")
      )
  }

  @JSExport
  val errorHandling = new Example {

    def apply(using Scape[IO]): StreamCode =
      code(
        Stream("ab")
          .stage("""Stream("ab")""")
          .flatMap(_ =>
            Stream.raiseError[IO](Err).stage("Stream.raiseError(Err)")
          )
          .stage("flatMap(…)")
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
          .flatMap(_ => Stream('a', 'b').stage("Stream('a', 'b')"))
          .stage("flatMap(…)")
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
        Stream("ab", "xy")
          .onFinalize(IO("abxy").trace_())
          .stage("""Stream("ab", "xy")""")
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
