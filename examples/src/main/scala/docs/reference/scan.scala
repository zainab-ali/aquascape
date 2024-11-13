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

@JSExportTopLevel("DocsReferenceScan")
object scan {

  @JSExport
  val scan = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b', 'c')
          .stage("Stream('a', 'b', 'c')")
          .scan("")((str, ch) => s"$str-$ch")
          .stage("scan(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val scan1 = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream("a", "b", "c")
          .stage("""Stream("a", "b", "c")""")
          .scan1((str, ch) => s"$str-$ch")
          .stage("scan1(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val scanMonoid = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream("a", "b", "c")
          .stage("""Stream("a", "b", "c")""")
          .scanMonoid
          .stage("scanMonoid")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val scanMap = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream("a", "b", "c")
          .stage("""Stream("a", "b", "c")""")
          .scanMap(_.toUpperCase)
          .stage("scanMap(_.toUpperCase)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val scanChunkPropagation = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        (Stream("a", "b") ++ Stream("c"))
          .stage("Stream(…) ++ Stream(…)")
          .scan("")((str, ch) => s"$str-$ch")
          .stage("scan(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }
}
