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
import cats.syntax.all.*
import fs2.*

import scala.concurrent.duration.*
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("DocsReferenceTime")
object time {

  given Show[FiniteDuration] = duration => s"${duration.toSeconds}s"

  @JSExport
  val sleep = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .sleep[IO](1.second)
          .stage("Stream.sleep(1.s)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val delayBy = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .stage("Stream('a', 'b')")
          .delayBy[IO](1.second)
          .stage("delayBy(1.s)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val fixedDelay = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .fixedDelay[IO](1.second)
          .stage("Stream.fixedDelay(1.s)")
          .take(2)
          .compile
          .toList
          .compileStage("take(2)…toList")
      )
  }

  @JSExport
  val fixedDelaySlowElementEval = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .fixedDelay[IO](1.second)
          .stage("Stream.fixedDelay(1.s)")
          .evalMap(_ => IO.sleep(2.seconds))
          .stage("evalMap(…)")
          .take(2)
          .compile
          .toList
          .compileStage("take(2)…toList")
      )
  }

  @JSExport
  val awakeDelay = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .awakeDelay[IO](1.second)
          .stage("Stream.awakeDelay(1.s)")
          .take(2)
          .compile
          .toList
          .compileStage("take(2)…toList")
      )
  }

  @JSExport
  val fixedRate = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .fixedRate[IO](1.second)
          .stage("Stream.fixedRate(1.s)")
          .take(2)
          .compile
          .toList
          .compileStage("take(2)…toList")
      )
  }

  @JSExport
  val fixedRateFastElementEval = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .fixedRate[IO](3.second)
          .stage("Stream.fixedRate(3.s)")
          .evalMap(_ => IO.sleep(1.seconds))
          .stage("evalMap(…)")
          .take(2)
          .compile
          .toList
          .compileStage("take(2)…toList")
      )
  }

  @JSExport
  val fixedRateSlowElementEval = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .fixedRate[IO](1.second)
          .stage("Stream.fixedRate(1.s)")
          .evalMap(_ => IO.sleep(2.seconds))
          .stage("evalMap(…)")
          .take(2)
          .compile
          .toList
          .compileStage("take(2)…toList")
      )
  }

  @JSExport
  val fixedRateDamped = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .fixedRate[IO](2.seconds)
          .stage("Stream.fixedRate(2s)")
          .zipWithIndex
          .evalMap { case (_, i) => IO.sleep(5.seconds).whenA(i == 0) }
          .stage("evalMap(…)")
          .take(4)
          .compile
          .toList
          .compileStage("take(4)…toList")
      )
  }

  @JSExport
  val fixedRateUndamped = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .fixedRate[IO](2.seconds, dampen = false)
          .stage("Stream.fixedRate(…)")
          .zipWithIndex
          .evalMap { case (_, i) => IO.sleep(5.seconds).whenA(i == 0) }
          .stage("evalMap(…)")
          .take(4)
          .compile
          .toList
          .compileStage("take(4)…toList")
      )
  }

  @JSExport
  val fixedRateStartImmediately = new Example {
    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .fixedRateStartImmediately[IO](1.second)
          .stage("Stream.fixedRateStartImmediately(1.s)")
          .take(2)
          .compile
          .toList
          .compileStage("take(2)…toList")
      )
  }

  @JSExport
  val awakeEvery = new Example {

    def apply(using Scape[IO]): StreamCode =
      code(
        Stream
          .awakeEvery[IO](1.seconds)
          .stage("Stream.awakeEvery(1.s)")
          .take(2)
          .compile
          .toList
          .compileStage("take(2)…toList")
      )
  }
  @JSExport
  val spaced = new Example {

    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .stage("Stream('a', 'b')")
          .spaced(1.second)
          .stage("spaced(1.s)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val metered = new Example {

    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .stage("Stream('a','b')")
          .metered[IO](1.second)
          .stage("metered(1.s)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val meteredSlowElementEval = new Example {

    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .stage("Stream('a','b')")
          .metered[IO](1.second)
          .stage("metered(1.s)")
          .evalTap(_ => IO.sleep(2.seconds))
          .stage("evalTap(…)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

  @JSExport
  val meteredSlowInputEval = new Example {

    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .evalTap(_ => IO.sleep(2.seconds))
          .stage("Stream(…).evalTap(…)")
          .metered[IO](1.second)
          .stage("metered(1.s)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }
  @JSExport
  val meteredStartImmediately = new Example {

    def apply(using Scape[IO]): StreamCode =
      code(
        Stream('a', 'b')
          .stage("Stream('a','b')")
          .meteredStartImmediately[IO](1.second)
          .stage("meteredStartImmediately(1.s)")
          .compile
          .toList
          .compileStage("compile.toList")
      )
  }

}
