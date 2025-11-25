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

package aquascape

import cats.*
import cats.effect.*
import cats.effect.testkit.*
import fs2.*
import munit.CatsEffectSuite
import munit.Location
import snapshot4s.generated.*
import snapshot4s.munit.SnapshotAssertions

class ScapeSuite extends CatsEffectSuite with SnapshotAssertions {

  implicit val showUnit: Show[Unit] = _ => "()"

  private def execute[O](io: IO[O]): IO[O] =
    TestControl.executeEmbed(io, seed = Some("MTIzNA=="))

  private def unchunked[O](f: Scape[IO] ?=> IO[O]): IO[List[Event]] =
    execute(
      Scape.unchunked[IO].flatMap { t =>
        t.events(f(using t).attempt.void)
          .map(_._1.map(_._1).toList)
      }
    )

  private def chunked[O](
      f: Scape[IO] ?=> IO[O]
  ): IO[List[Event]] =
    execute(
      Scape.chunked[IO].flatMap { t =>
        t.events(f(using t))
          .map(_._1.map(_._1).toList)
      }
    )

  object Boom extends Throwable("BOOM!")

  import Event.*

  test("raises an error when compileStage is not present") {
    val program = Scape.unchunked[IO].flatMap { case t @ (given Scape[IO]) =>
      t.events(
        Stream("Mao")[IO]
          .stage("source")
          .compile
          .lastOrError
      ).attempt
    }
    assertIO(
      program,
      Left(MissingStageException)
    )
  }

  test("raises an error when compileStage is not present") {
    val program = Scape.unchunked[IO].flatMap { case t @ (given Scape[IO]) =>
      t.events(
        Stream("Mao")[IO]
          .stage("source")
          .fork("nonexistent", "child")
          .compile
          .lastOrError
          .compileStage("lastOrError")
      ).attempt
    }
    assertIO(
      program,
      Left(ParentBranchNotFound("nonexistent", "child"))
    )
  }

  test("traces a single operator") {
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
        List(
          Pull(to = Label("source", 92), from = Label("last", 95), token = 0),
          Output(value = "Mao", token = 0),
          Pull(to = Label("source", 92), from = Label("last", 95), token = 1),
          Done(token = 1),
          Finished(at = Label("last", 95), errored = false, value = "Mao")
        )
      )
    }
  }

  test("traces chunks") {
    val program = chunked {
      Stream("Mao", "Popcorn")[IO]
        .stage("source")
        .compile
        .lastOrError
        .compileStage("last")
    }
    program.map { actual =>
      assertInlineSnapshot(
        actual,
        List(
          Pull(to = Label("source", 114), from = Label("last", 117), token = 0),
          OutputChunk(value = List("Mao", "Popcorn"), token = 0),
          Pull(to = Label("source", 114), from = Label("last", 117), token = 1),
          Done(token = 1),
          Finished(at = Label("last", 117), errored = false, value = "Popcorn")
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
        List(
          Pull(to = Label("map", 138), from = Label("last", 141), token = 0),
          Pull(to = Label("source", 136), from = Label("map", 138), token = 1),
          Output(value = "Mao", token = 1),
          Output(value = "MAO", token = 0),
          Pull(to = Label("map", 138), from = Label("last", 141), token = 2),
          Pull(to = Label("source", 136), from = Label("map", 138), token = 3),
          Done(token = 3),
          Done(token = 2),
          Finished(at = Label("last", 141), errored = false, value = "MAO")
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
        List(
          Pull(to = Label("zip", 169), from = Label("last", 172), token = 0),
          Pull(to = Label("left", 164), from = Label("zip", 169), token = 1),
          Output(value = "Mao", token = 1),
          Pull(to = Label("right", 167), from = Label("zip", 169), token = 2),
          Output(value = "Popcorn", token = 2),
          Output(value = "(Mao,Popcorn)", token = 0),
          Pull(to = Label("zip", 169), from = Label("last", 172), token = 3),
          Pull(to = Label("left", 164), from = Label("zip", 169), token = 4),
          Done(token = 4),
          Done(token = 3),
          Finished(
            at = Label("last", 172),
            errored = false,
            value = "(Mao,Popcorn)"
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
        List(
          Pull(to = Label("source", 202), from = Label("last", 205), token = 0),
          Eval(value = "Mao"),
          Output(value = "Mao", token = 0),
          Pull(to = Label("source", 202), from = Label("last", 205), token = 1),
          Done(token = 1),
          Finished(at = Label("last", 205), errored = false, value = "Mao")
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
        List(
          Pull(to = Label("eval", 228), from = Label("last", 231), token = 0),
          Pull(to = Label("source", 226), from = Label("eval", 228), token = 1),
          Output(value = "Mao", token = 1),
          Error(value = "BOOM!", token = 0, raisedHere = true),
          Finished(at = Label("last", 231), errored = true, value = "BOOM!")
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
        List(
          Pull(
            to = Label("handle", 254),
            from = Label("drain", 257),
            token = 0
          ),
          Pull(to = Label("eval", 252), from = Label("handle", 254), token = 1),
          Pull(to = Label("source", 250), from = Label("eval", 252), token = 2),
          Output(value = "Mao", token = 2),
          Error(value = "BOOM!", token = 1, raisedHere = true),
          Pull(
            to = Label("second", 253),
            from = Label("handle", 254),
            token = 3
          ),
          Done(token = 3),
          Done(token = 0),
          Finished(at = Label("drain", 257), errored = false, value = "()")
        )
      )
    }
  }

  def bracket(suffix: String = "")(using t: Scape[IO]): Stream[IO, Unit] =
    Stream
      .bracket(IO(s"acquire$suffix").trace().void)(_ =>
        IO(s"release$suffix").trace().void
      )

  test("traces resources and errors") {
    val program = unchunked {
      bracket()
        .stage("source")
        .evalTap(_ => IO.raiseError[String](Boom))
        .stage("eval")
        .compile
        .drain
        .compileStage("drain")
    }
    program.map { actual =>
      assertInlineSnapshot(
        actual,
        List(
          Pull(to = Label("eval", 296), from = Label("drain", 299), token = 0),
          Pull(to = Label("source", 294), from = Label("eval", 296), token = 1),
          Eval(value = "acquire"),
          Output(value = "()", token = 1),
          Error(value = "BOOM!", token = 0, raisedHere = true),
          Eval(value = "release"),
          Finished(at = Label("drain", 299), errored = true, value = "BOOM!")
        )
      )
    }
  }

  test("traces resources and error handling") {
    val program = unchunked {
      bracket()
        .stage("source")
        .evalTap(_ => IO.raiseError[String](Boom))
        .stage("eval")
        .handleErrorWith(_ => Stream.empty[IO].as(()).stage("second"))
        .stage("handle")
        .compile
        .drain
        .compileStage("drain")
    }
    program.map { actual =>
      assertInlineSnapshot(
        actual,
        List(
          Pull(
            to = Label("handle", 324),
            from = Label("drain", 327),
            token = 0
          ),
          Pull(to = Label("eval", 322), from = Label("handle", 324), token = 1),
          Pull(to = Label("source", 320), from = Label("eval", 322), token = 2),
          Eval(value = "acquire"),
          Output(value = "()", token = 2),
          Error(value = "BOOM!", token = 1, raisedHere = true),
          Eval(value = "release"),
          Pull(
            to = Label("second", 323),
            from = Label("handle", 324),
            token = 3
          ),
          Done(token = 3),
          Done(token = 0),
          Finished(at = Label("drain", 327), errored = false, value = "()")
        )
      )
    }
  }

//  test("stage parallel execution".ignore) {
//    val actualIO = unchunked {
//      Stream("Mao", "Popcorn", "Trouble")[IO]
//        .stage("source", branch = "s")
//        .fork("root", "s")
//        .parEvalMap(2)(IO(_).trace())
//        .stage("parEvalMap")
//        .compile
//        .drain
//        .compileStage("drain")
//    }
//    assertEvents(actualIO, Nil)
//  }

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
        List(
          Pull(to = Label("merge", 381), from = Label("drain", 384), token = 0),
          Pull(to = Label("left", 374), from = Label("merge", 381), token = 1),
          Pull(to = Label("right", 378), from = Label("merge", 381), token = 2),
          Output(value = "Popcorn", token = 2),
          Output(value = "Mao", token = 1),
          Output(value = "Mao", token = 0),
          Pull(to = Label("merge", 381), from = Label("drain", 384), token = 3),
          Output(value = "Popcorn", token = 3),
          Pull(to = Label("merge", 381), from = Label("drain", 384), token = 5),
          Pull(to = Label("left", 374), from = Label("merge", 381), token = 4),
          Done(token = 4),
          Pull(to = Label("right", 378), from = Label("merge", 381), token = 6),
          Done(token = 6),
          Done(token = 5),
          Finished(at = Label("drain", 384), errored = false, value = "()")
        )
      )
    }
  }
}
