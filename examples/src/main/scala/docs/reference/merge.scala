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

import scala.concurrent.duration.*
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("DocsReferenceMerge")
object merge {

  def secondsBeforeAbInputBox(max: Int): InputBox[Int] = InputBox.int(
    labelText = "n (seconds before 'a' and 'b')",
    defaultValue = 1,
    min = 0,
    max = max
  )
  def secondsBeforeErrorInputBox(max: Int): InputBox[Int] = InputBox.int(
    labelText = "n (seconds before error)",
    defaultValue = 1,
    min = 0,
    max = max
  )

  @JSExport
  val merge = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = secondsBeforeAbInputBox(3)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code {
        val ab = Stream('a', 'b').metered[IO](n.seconds)
        val xy = Stream('x', 'y').metered[IO](1.second)
        ab
          .stage("ab", branch = "left")
          .fork("root", "left")
          .merge(
            xy
              .stage("xy", branch = "right")
              .fork("root", "right")
          )
          .stage("merge")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }

  @JSExport
  val mergeHaltBoth = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = secondsBeforeAbInputBox(4)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code {
        val ab = Stream('a', 'b').metered[IO](n.seconds)
        val xy = Stream('x', 'y').metered[IO](1.second)
        ab
          .stage("ab", branch = "left")
          .fork("root", "left")
          .mergeHaltBoth(
            xy
              .stage("xy", branch = "right")
              .fork("root", "right")
          )
          .stage("mergeHaltBoth")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }

  @JSExport
  val mergeHaltL = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = secondsBeforeAbInputBox(3)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code {
        val ab = Stream('a', 'b').metered[IO](n.seconds)
        val xy = Stream('x', 'y').metered[IO](1.second)
        ab
          .stage("ab", branch = "left")
          .fork("root", "left")
          .mergeHaltL(
            xy
              .stage("xy", branch = "right")
              .fork("root", "right")
          )
          .stage("mergeHaltL")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }

  @JSExport
  val mergeHaltR = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = secondsBeforeAbInputBox(3)

    def apply(n: Int)(using Scape[IO]): StreamCode =
      code {
        val ab = Stream('a', 'b').metered[IO](n.seconds)
        val xy = Stream('x', 'y').metered[IO](1.second)
        ab
          .stage("ab", branch = "left")
          .fork("root", "left")
          .mergeHaltR(
            xy
              .stage("xy", branch = "right")
              .fork("root", "right")
          )
          .stage("mergeHaltR")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }

  @JSExport
  val mergeHaltBothExitCase = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = secondsBeforeAbInputBox(4)

    def apply(n: Int)(using Scape[IO]): StreamCode =
      code {
        val ab = Stream('a', 'b')
          .metered[IO](n.seconds)
          .onFinalizeCase(exitCase => IO(show"ab-$exitCase").trace_())
        val xy = Stream('x', 'y')
          .metered[IO](2.seconds)
          .onFinalizeCase(exitCase => IO(show"xy-$exitCase").trace_())
        ab
          .stage("ab", branch = "left")
          .fork("root", "left")
          .mergeHaltBoth(
            xy
              .stage("xy", branch = "right")
              .fork("root", "right")
          )
          .stage("mergeHaltBoth")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }

  @JSExport
  val takeExitCase = new Example {

    def apply(using Scape[IO]): StreamCode =
      code {
        val ab = Stream('a', 'b')
          .metered[IO](1.seconds)
          .onFinalizeCase(exitCase => IO(show"ab-$exitCase").trace_())
        val xy = Stream('x', 'y')
          .metered[IO](2.seconds)
          .onFinalizeCase(exitCase => IO(show"xy-$exitCase").trace_())
        ab
          .stage("ab", branch = "left")
          .fork("root", "left")
          .merge(
            xy
              .stage("xy", branch = "right")
              .fork("root", "right")
          )
          .head
          .stage("merge.head")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }

  @JSExport
  val errorExitCase = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = secondsBeforeErrorInputBox(4)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code {
        val ab = (Stream('a') ++ Stream.raiseError[IO](Err).delayBy(n.seconds))

        val xy = Stream('x', 'y')
          .onFinalizeCase(exitCase => IO(show"xy-$exitCase").trace_())
          .metered[IO](1.second)
        ab
          .stage("ab", branch = "left")
          .fork("root", "left")
          .merge(
            xy
              .stage("xy", branch = "right")
              .fork("root", "right")
          )
          .stage("merge")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }
  @JSExport
  val mergeError = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = secondsBeforeErrorInputBox(1)

    def apply(n: Int)(using Scape[IO]): StreamCode =
      code {
        val ab = (Stream('a') ++ Stream.raiseError[IO](Err).delayBy(n.seconds))

        val xy = Stream('x', 'y').metered[IO](1.second)
        ab
          .stage("ab", branch = "left")
          .fork("root", "left")
          .merge(
            xy
              .stage("xy", branch = "right")
              .fork("root", "right")
          )
          .stage("merge")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }

  @JSExport
  val resources = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = secondsBeforeAbInputBox(4)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code {
        val ab =
          Stream('a', 'b')
            .onFinalize(IO("ab").trace_())
            .metered[IO](n.second)

        val xy = Stream('x', 'y')
          .onFinalize(IO("xy").trace_())
          .metered[IO](1.second)
        ab
          .stage("ab", branch = "left")
          .fork("root", "left")
          .merge(
            xy
              .stage("xy", branch = "right")
              .fork("root", "right")
          )
          .stage("merge")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }

// TODO: Zainab - Experiment with different values for the max inputBox value

  @JSExport
  val resourcesError = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = secondsBeforeErrorInputBox(4)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code {
        val ab = (Stream('a') ++ Stream.raiseError[IO](Err).delayBy(n.seconds))
          .onFinalize(IO("ab").trace_())

        val xy = Stream('x', 'y')
          .onFinalize(IO("xy").trace_())
          .metered[IO](1.second)
        ab
          .stage("ab", branch = "left")
          .fork("root", "left")
          .merge(
            xy
              .stage("xy", branch = "right")
              .fork("root", "right")
          )
          .stage("merge")
          .compile
          .toList
          .compileStage("compile.toList")
      }
  }

}
