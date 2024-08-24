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
import cats.effect.IO
import fs2.*

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("DocsReferenceDrop")
object drop {
  def takeFiniteInputBox(max: Int): InputBox[Int] = InputBox.int(
    labelText = "n (elements to take)",
    defaultValue = 1,
    min = 0,
    max = max
  )
  def dropFiniteInputBox(max: Int): InputBox[Int] = InputBox.int(
    labelText = "n (elements to drop)",
    defaultValue = 1,
    min = 0,
    max = max
  )

  @JSExport
  val basic = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = dropFiniteInputBox(3)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .stage("Stream('a', 'b', 'c')")
          .drop(n)
          .stage(s"drop($n)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val dropWhile = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .stage("Stream('a', 'b')")
          .dropWhile(_ != 'b')
          .stage("dropWhile(_ != 'b')")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }
  @JSExport
  val dropThrough = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .stage("Stream('a', 'b', 'c')")
          .dropThrough(_ != 'b')
          .stage("dropThrough(_ != 'b')")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }
  @JSExport
  val dropLast = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .stage("Stream('a', 'b', 'c')")
          .dropLast
          .stage("dropLast")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val dropLastIf = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = takeFiniteInputBox(4)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .take(n)
          .stage(s"Stream('a', 'b', 'c').take($n)")
          .dropLastIf(_ != 'b')
          .stage("dropLastIf(_ != 'b')")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val dropChunks = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = dropFiniteInputBox(3)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .repeatN(2)
          .stage("Stream('a', 'b').repeatN(2)")
          .drop(n)
          .stage(s"drop($n)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val dropLastChunks = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .repeatN(3)
          .stage("Stream('a', 'b').repeatN(3)")
          .dropLast
          .stage(s"dropLast")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

}
