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

package aquascape.drawing

import aquascape.*
import cats.*
import cats.effect.*
import cats.effect.testkit.*
import fs2.*
import munit.CatsEffectSuite
import munit.Location
import snapshot4s.generated.*
import snapshot4s.munit.SnapshotAssertions

class EventsToDiagramSuite extends CatsEffectSuite with SnapshotAssertions {
  import Item.*
  object Boom extends Throwable("BOOM!")
  implicit val showUnit: Show[Unit] = _ => "()"

  private def execute[O](io: IO[O]): IO[O] =
    TestControl.executeEmbed(io, seed = Some("MTIzNA=="))

  private def unchunked[O](f: Scape[IO] ?=> IO[O]): IO[Diagram] =
    execute(
      Scape.unchunked[IO].flatMap { t =>
        t.events(f(using t).attempt.void)
          .map { case (eventsAndTime, _) =>
            eventsToDiagram(eventsAndTime)
          }
      }
    )

  test("draws a single operator") {
    val program = unchunked {
      Stream("Mao")[IO]
        .stage("source")
        .compile
        .lastOrError
        .compileStage("last")
    }
    program.map { actual =>
      assertInlineSnapshot(
        actual,
        Diagram(
          labels = List("last", "source"),
          items = List(
            Pull(from = 0, to = 1),
            Output(value = "Mao", from = 1, to = 0, pullProgress = 0),
            Pull(from = 0, to = 1),
            Done(from = 1, to = 0, pullProgress = 2),
            Finished(value = "Mao", errored = false, at = 0)
          )
        )
      )
    }
  }

  test("traces multiple operators") {
    val program = unchunked {
      Stream("Mao")[IO]
        .stage("source")
        .map(_.toUpperCase)
        .stage("map")
        .compile
        .lastOrError
        .compileStage("last")
    }
    program.map { actual =>
      assertInlineSnapshot(
        actual,
        Diagram(
          labels = List("last", "map", "source"),
          items = List(
            Pull(from = 0, to = 1),
            Pull(from = 1, to = 2),
            Output(value = "Mao", from = 2, to = 1, pullProgress = 1),
            Output(value = "MAO", from = 1, to = 0, pullProgress = 0),
            Pull(from = 0, to = 1),
            Pull(from = 1, to = 2),
            Done(from = 2, to = 1, pullProgress = 5),
            Done(from = 1, to = 0, pullProgress = 4),
            Finished(value = "MAO", errored = false, at = 0)
          )
        )
      )
    }
  }

  test("traces zipped streams") {
    val program = unchunked {
      Stream("Mao")[IO]
        .stage("left")
        .zip(
          Stream("Popcorn")[IO]
            .stage("right")
        )
        .stage("zip")
        .compile
        .lastOrError
        .compileStage("last")
    }
    program.map { actual =>
      assertInlineSnapshot(
        actual,
        Diagram(
          labels = List("last", "zip", "right", "left"),
          items = List(
            Pull(from = 0, to = 1),
            Pull(from = 1, to = 3),
            Output(value = "Mao", from = 3, to = 1, pullProgress = 1),
            Pull(from = 1, to = 2),
            Output(value = "Popcorn", from = 2, to = 1, pullProgress = 3),
            Output(value = "(Mao,Popcorn)", from = 1, to = 0, pullProgress = 0),
            Pull(from = 0, to = 1),
            Pull(from = 1, to = 3),
            Done(from = 3, to = 1, pullProgress = 7),
            Done(from = 1, to = 0, pullProgress = 6),
            Finished(value = "(Mao,Popcorn)", errored = false, at = 0)
          )
        )
      )
    }
  }

  test("traces effect evaluation") {
    val program = unchunked {
      Stream
        .eval(IO("Mao").trace())
        .stage("source")
        .compile
        .lastOrError
        .compileStage("last")

    }
    program.map { actual =>
      assertInlineSnapshot(
        actual,
        Diagram(
          labels = List("last", "source"),
          items = List(
            Pull(from = 0, to = 1),
            Eval(value = "Mao"),
            Output(value = "Mao", from = 1, to = 0, pullProgress = 0),
            Pull(from = 0, to = 1),
            Done(from = 1, to = 0, pullProgress = 3),
            Finished(value = "Mao", errored = false, at = 0)
          )
        )
      )
    }
  }

  test("traces raising errors") {
    val program = unchunked {
      Stream("Mao")[IO]
        .stage("source")
        .evalTap(_ => IO.raiseError[String](Boom))
        .stage("eval")
        .compile
        .lastOrError
        .compileStage("last")
    }
    program.map { actual =>
      assertInlineSnapshot(
        actual,
        Diagram(
          labels = List("last", "eval", "source"),
          items = List(
            Pull(from = 0, to = 1),
            Pull(from = 1, to = 2),
            Output(value = "Mao", from = 2, to = 1, pullProgress = 1),
            Error(value = "BOOM!", from = 1, to = 0, pullProgress = 0),
            Finished(value = "BOOM!", errored = true, at = 0)
          )
        )
      )
    }
  }

  test("traces handling errors") {
    val program = unchunked {
      Stream("Mao")[IO]
        .stage("source")
        .evalTap(_ => IO.raiseError[String](Boom))
        .stage("eval")
        .handleErrorWith(_ => Stream.empty[IO].map(_.toString).stage("second"))
        .stage("handle")
        .compile
        .drain
        .compileStage("drain")
    }
    program.map { actual =>
      assertInlineSnapshot(
        actual,
        Diagram(
          labels = List("drain", "handle", "second", "eval", "source"),
          items = List(
            Pull(from = 0, to = 1),
            Pull(from = 1, to = 3),
            Pull(from = 3, to = 4),
            Output(value = "Mao", from = 4, to = 3, pullProgress = 2),
            Error(value = "BOOM!", from = 3, to = 1, pullProgress = 1),
            Pull(from = 1, to = 2),
            Done(from = 2, to = 1, pullProgress = 5),
            Done(from = 1, to = 0, pullProgress = 0),
            Finished(value = "()", errored = false, at = 0)
          )
        )
      )
    }
  }

  test("traces merged streams") {
    val program = unchunked {
      Stream("Mao")[IO]
        .stage("left", branch = "l")
        .fork("root", "l")
        .merge(
          Stream("Popcorn")[IO]
            .stage("right", branch = "r")
            .fork("root", "r")
        )
        .stage("merge")
        .compile
        .drain
        .compileStage("drain")
    }
    program.map { actual =>
      assertInlineSnapshot(
        actual,
        Diagram(
          labels = List("drain", "merge", "right", "left"),
          items = List(
            Pull(from = 0, to = 1),
            Pull(from = 1, to = 3),
            Pull(from = 1, to = 2),
            Output(value = "Popcorn", from = 2, to = 1, pullProgress = 2),
            Output(value = "Mao", from = 3, to = 1, pullProgress = 1),
            Output(value = "Mao", from = 1, to = 0, pullProgress = 0),
            Pull(from = 0, to = 1),
            Output(value = "Popcorn", from = 1, to = 0, pullProgress = 6),
            Pull(from = 0, to = 1),
            Pull(from = 1, to = 3),
            Done(from = 3, to = 1, pullProgress = 9),
            Pull(from = 1, to = 2),
            Done(from = 2, to = 1, pullProgress = 11),
            Done(from = 1, to = 0, pullProgress = 8),
            Finished(value = "()", errored = false, at = 0)
          )
        )
      )
    }
  }

}
