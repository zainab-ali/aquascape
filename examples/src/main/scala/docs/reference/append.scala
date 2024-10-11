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

@JSExportTopLevel("DocsReferenceAppend")
object append {

  def takeFiniteInputBox(max: Int): InputBox[Int] = InputBox.int(
    labelText = "n (elements to take)",
    defaultValue = 1,
    min = 0,
    max = max
  )

  @JSExport
  val basic = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        (Stream('a', 'b')
          .stage("Stream('a','b')")
          ++ Stream('x', 'y').stage("Stream('x', 'y')"))
          .stage("++")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val finite = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = takeFiniteInputBox(5)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code(
        (Stream('a', 'b')
          .stage("Stream('a','b')")
          ++ Stream('x', 'y').stage("Stream('x', 'y')"))
          .take(n)
          .stage(s"(… ++ …).take($n)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val infinite = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = takeFiniteInputBox(5)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code {
        def repeatAb: Stream[IO, Char] =
          (Stream('a', 'b').stage("Stream('a','b')") ++ repeatAb)
        repeatAb
          .take(n)
          .stage(s"(… ++ …).take($n)")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }

  @JSExport
  val errors = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        (Stream('a', 'b')
          .stage("Stream('a','b')")
          ++ Stream.raiseError[IO](Err).stage("Stream.raiseError(Err)"))
          .stage("++")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val finalizers = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = takeFiniteInputBox(5)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code(
        (Stream('a', 'b')
          .onFinalize(IO("ab").trace_())
          .stage("Stream('a','b')")
          ++ Stream('x', 'y')
            .onFinalize(IO("xy").trace_())
            .stage("Stream('x', 'y')"))
          .take(n)
          .stage(s"(… ++ …).take($n)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

}
