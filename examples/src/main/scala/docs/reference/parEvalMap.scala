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
import scala.concurrent.duration.*

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("DocsReferenceParEvalMap")
object parEvalMap {
  def concurrencyInputBox(max: Int): InputBox[Int] = InputBox.int(
    labelText = "n (concurrent effects)",
    defaultValue = 2,
    min = 1,
    max = max
  )

  @JSExport
  val parEvalMapUnbounded = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .stage("Stream('a','b','c')", "upstream")
          .fork("root", "upstream")
          .parEvalMapUnbounded(ch => IO.sleep(1.second).as(ch).trace())
          .stage("parEvalMapUnbounded(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val parEvalMapConcurrency = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = concurrencyInputBox(4)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c', 'd')
          .stage("Stream('a','b','c', 'd')", "upstream")
          .fork("root", "upstream")
          .parEvalMap(n)(ch => IO.sleep(1.second).as(ch).trace())
          .stage(s"parEvalMap($n)(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val parEvalMapOrder = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream(100, 5)
          .stage("Stream(100, 5)", "upstream")
          .fork("root", "upstream")
          .parEvalMap(2)(n => IO.sleep(n.second).as(n).trace())
          .stage("parEvalMap(2)(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val parEvalMapUnorderedUnbounded = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream(100, 5)
          .stage("Stream(100, 5)", "upstream")
          .fork("root", "upstream")
          .parEvalMapUnorderedUnbounded(n => IO.sleep(n.second).as(n).trace())
          .stage("parEvalMapUnorderedUnbounded(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val parEvalMapUnorderedConcurrency = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = concurrencyInputBox(4)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code(
        Stream(5, 4, 3, 2)
          .stage("Stream(5, 4, 3, 2)", "upstream")
          .fork("root", "upstream")
          .parEvalMapUnordered(n)(i => IO.sleep(i.second).as(i).trace())
          .stage(s"parEvalMapUnordered($n)(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val parEvalMapSingletonChunks = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .stage("Stream('a','b','c')", "upstream")
          .fork("root", "upstream")
          .parEvalMap(2)(ch => IO.sleep(1.second).as(ch).trace())
          .stage("parEvalMap(2)(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

}
