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
import fs2.*

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("DocsReferenceCompile")
object compile {
  def takeFiniteInputBox(max: Int): InputBox[Int] = InputBox.int(
    labelText = "n (elements to take)",
    defaultValue = 1,
    min = 0,
    max = max
  )

  @JSExport
  val toList = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .stage("Stream('a','b')")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val last = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = takeFiniteInputBox(2)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .take(n)
          .stage(s"Stream('a','b').take($n)")
          .compile
          .last
          .compileStage("compile.last")
      )
  }

  @JSExport
  val onlyOrError = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = takeFiniteInputBox(2)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .take(n)
          .stage(s"Stream('a','b').take($n)")
          .compile
          .onlyOrError
          .compileStage("compile.onlyOrError")
      )
  }

  @JSExport
  val count = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .stage("Stream('a','b')")
          .compile
          .count
          .compileStage("compile.count")
      )
  }

  @JSExport
  val drain = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .evalTap(c => IO(c.toInt).trace().void)
          .stage("Stream('a','b').evalTap(…)")
          .compile
          .drain
          .compileStage("compile.drain")
      )
  }

}
