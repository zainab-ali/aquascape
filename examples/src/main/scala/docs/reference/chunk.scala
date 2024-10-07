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
import fs2.*

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("DocsReferenceChunk")
object chunk {

  def chunkSizeInputBox(max: Int, default: Int = 1): InputBox[Int] =
    InputBox.int(
      labelText = "n (chunk size)",
      defaultValue = 1,
      min = 1,
      max = max
    )

  @JSExport
  val input = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        val input =
          Stream('a', 'b', 'c')
            .repeatN(2)
            .stage("Stream('a','b','c').repeatN(2)")

        input.compile.toList
          .compileStage("compile.toList")
      }
  }

  @JSExport
  val chunkLimit = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = chunkSizeInputBox(4, default = 2)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .repeatN(2)
          .stage("Stream('a','b','c').repeatN(2)")
          .chunkLimit(n)
          .stage(s"chunkLimit($n)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val chunkMin = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = chunkSizeInputBox(4, default = 3)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code(
        (Stream('a', 'b', 'c'))
          .repeatN(3)
          .stage("Stream('a','b','c').repeatN(3)")
          .chunkMin(n)
          .stage(s"chunkMin($n)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val chunkN = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = chunkSizeInputBox(5)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .repeatN(2)
          .stage("Stream('a','b','c').repeatN(2)")
          .chunkN(n)
          .stage(s"chunkN($n)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val chunks = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .repeatN(2)
          .stage("Stream('a','b','c').repeatN(2)")
          .chunks
          .stage(s"chunks")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val unchunks = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream(Chunk('a', 'b', 'c'))
          .repeatN(2)
          .stage("Stream(…)")
          .unchunks
          .stage("unchunks")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val repartition = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream("abc", "dbe")
          .repeatN(2)
          .stage("""Stream(…)…repeatN(2)""")
          .repartition(s => Chunk.array(s.split("b")))
          .stage("repartition(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

}
