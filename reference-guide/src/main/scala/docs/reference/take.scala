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
import cats.Show
import cats.effect.*
import fs2.*

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("DocsReferenceTake")
object take {
  def takeFiniteInputBox(max: Int): InputBox[Int] = InputBox.int(
    labelText = "n (elements to take)",
    defaultValue = 1,
    min = 0,
    max = max
  )

  @JSExport
  val basic = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = takeFiniteInputBox(4)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b').repeat
          .stage("Stream('a', 'b').repeat")
          .take(n)
          .stage(s"take($n)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val takeWhile = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .stage("Stream('a', 'b', 'c')")
          .takeWhile(_ != 'b')
          .stage(s"takeWhile(_ != 'b')")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }
  @JSExport
  val takeThrough = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .stage("Stream('a', 'b', 'c')")
          .takeThrough(_ != 'b')
          .stage(s"takeThrough(_ != 'b')")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val takeRight = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = takeFiniteInputBox(7)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .repeatN(3)
          .stage("Stream('a', 'b').repeatN(3)")
          .takeRight(n)
          .stage(s"takeRight($n)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }
}
