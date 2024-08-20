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

package aquascape.examples.merge

import aquascape.*
import aquascape.examples.*
import cats.Show
import cats.effect.*
import cats.effect.IO
import cats.syntax.all.*
import fs2.*

import scala.concurrent.duration.*
import scala.scalajs.js.annotation.JSExportTopLevel

import formCodecs.*

@JSExportTopLevel("CombiningStreamsMerge")
object CombiningStreamsMerge extends ExampleWithInput[Int] {
  given codec: FormCodec[Int] = intCodec(0, 3)
  def label: String = "n (seconds between ab elements)"
  def default: Int = 1
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

@JSExportTopLevel("CombiningStreamsMergeHaltL")
object CombiningStreamsMergeHaltL extends ExampleWithInput[Int] {

  given codec: FormCodec[Int] = intCodec(0, 3)
  def label: String = "n (seconds between ab elements)"
  def default: Int = 1

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

@JSExportTopLevel("CombiningStreamsMergeHaltR")
object CombiningStreamsMergeHaltR extends ExampleWithInput[Int] {

  given codec: FormCodec[Int] = intCodec(0, 3)
  def label: String = "n (seconds between ab elements)"
  def default: Int = 1

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

@JSExportTopLevel("CombiningStreamsMergeHaltBoth")
object CombiningStreamsMergeHaltBoth extends ExampleWithInput[Int] {

  given codec: FormCodec[Int] = intCodec(0, 4)
  def label: String = "n (seconds between ab elements)"
  def default: Int = 1

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

@JSExportTopLevel("CombiningStreamsMergeError")
object CombiningStreamsMergeError extends ExampleWithInput[Int] {

  given codec: FormCodec[Int] = intCodec(0, 1)
  def label: String = "n (seconds before error)"
  def default: Int = 1

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

@JSExportTopLevel("CombiningStreamsResources")
object CombiningStreamsResources extends ExampleWithInput[Int] {

  given codec: FormCodec[Int] = intCodec(0, 4)
  def label: String = "n (seconds between ab elements)"
  def default: Int = 1
  def apply(n: Int)(using Scape[IO]): StreamCode =
    code {
      val ab =
        Stream('a', 'b')
          .onFinalize(IO("AB").trace_())
          .metered[IO](n.second)

      val xy = Stream('x', 'y')
        .onFinalize(IO("XY").trace_())
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

// TODO: Zainab - Experiment with different values for the max codec value

@JSExportTopLevel("CombiningStreamsResourcesError")
object CombiningStreamsResourcesError extends ExampleWithInput[Int] {

  given codec: FormCodec[Int] = intCodec(0, 4)
  def label: String = "n (seconds before error)"
  def default: Int = 1
  def apply(n: Int)(using Scape[IO]): StreamCode =
    code {
      val ab = (Stream('a') ++ Stream.raiseError[IO](Err).delayBy(n.seconds))
        .onFinalize(IO("AB").trace_())

      val xy = Stream('x', 'y')
        .onFinalize(IO("XY").trace_())
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

@JSExportTopLevel("CombiningStreamsResourcesExitCase")
object CombiningStreamsResourcesExitCase extends ExampleWithInput[Int] {

  given codec: FormCodec[Int] = intCodec(0, 4)
  def label: String = "n (seconds before error)"
  def default: Int = 1
  given Show[Resource.ExitCase] = {
    case Resource.ExitCase.Canceled   => "Canceled"
    case _: Resource.ExitCase.Errored => "Errored"
    case Resource.ExitCase.Succeeded  => "Succeeded"
  }

  def apply(n: Int)(using Scape[IO]): StreamCode =
    code {
      val ab = (Stream('a') ++ Stream.raiseError[IO](Err).delayBy(n.seconds))

      val xy = Stream('x', 'y')
        .onFinalizeCase(exitCase => IO(exitCase).trace_())
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

@JSExportTopLevel("CombiningStreamsMergeHaltBothExitCase")
object CombiningStreamsMergeHaltBothExitCase extends ExampleWithInput[Int] {

  given codec: FormCodec[Int] = intCodec(0, 4)
  def label: String = "n (seconds between ab elements)"
  def default: Int = 1

  given Show[Resource.ExitCase] = {
    case Resource.ExitCase.Canceled   => "Canceled"
    case _: Resource.ExitCase.Errored => "Errored"
    case Resource.ExitCase.Succeeded  => "Succeeded"
  }

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

@JSExportTopLevel("CombiningStreamsTakeExitCase")
object CombiningStreamsTakeExitCase extends Example {

  given Show[Resource.ExitCase] = {
    case Resource.ExitCase.Canceled   => "Canceled"
    case _: Resource.ExitCase.Errored => "Errored"
    case Resource.ExitCase.Succeeded  => "Succeeded"
  }

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
