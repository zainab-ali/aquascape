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
import fs2.*
import munit.CatsEffectSuite
import munit.Location
class ScapeSuite extends CatsEffectSuite {
  object Boom extends Throwable("BOOM!")

  import Event.*

  implicit val showUnit: Show[Unit] = _ => "()"
  val t = new Unique.Token()
  private def replaceToken: Event => Event = {
    case Pull(to, from, _) => Pull(to, from, t)
    case Done(_)           => Done(t)
    case Output(v, _)      => Output(v, t)
    case e: Error          => e.copy(token = t)
    case OutputChunk(v, _) => OutputChunk(v, t)
    case other             => other
  }

  private def simple[O](f: Scape[IO] ?=> IO[O]): IO[List[Event]] =
    Scape.unchunked[IO].flatMap { t =>
      t.events(f(using t).attempt.void)
        .map(_._1.map(_._1).toList)
    }

  private def simpleChunked[O](
      f: Scape[IO] ?=> IO[O]
  ): IO[List[Event]] =
    Scape.chunked[IO].flatMap { t =>
      t.events(f(using t))
        .map(_._1.map(_._1).toList)
    }

  def assertContains(
      actual: List[Event],
      expected: List[(Event, Location)]
  ): Unit = {
    val _ = expected.foldLeft(actual) { (acc, el) =>
      val (e, l) = el
      assert(acc.contains(e))(l)
      acc.dropWhile(_ != e)
    }
  }
  def assertEvents(
      actualIO: IO[List[Event]],
      expected: List[(Event, Location)]
  )(using
      loc: munit.Location
  ): IO[Unit] = {
    actualIO.map { actual =>
      actual.zipWithIndex
        .zip(expected)
        .map { case ((a, i), (e, l)) =>
          assertEquals(
            replaceToken(a),
            e,
            s"Event $i is incorrect - ${pprint(actual)}"
          )(
            l,
            summon
          )
        }
      assertEquals(
        actual.length,
        expected.length,
        s"Wrong number of events obtained. ${pprint(actual)}"
      )
    }.void
  }

  private def loc[A](a: A)(using l: Location): (A, Location) = (a, l)

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

  test("traces a single combinator") {
    val actual = simple {
      Stream("Mao")[IO]
        .stage("source")
        .compile
        .lastOrError
        .compileStage("last")
    }
    val expected = List(
      loc(OpenScope(label = "source")),
      loc(Pull("source", "last", t)),
      loc(Output(value = "Mao", token = t)),
      loc(Pull("source", "last", t)),
      loc(Done(t)),
      loc(CloseScope(label = "source")),
      loc(Finished(at = "last", errored = false, value = "Mao"))
    )

    assertEvents(actual, expected)
  }
  test("traces chunks") {
    val actual = simpleChunked {
      Stream("Mao", "Popcorn")[IO]
        .stage("source")
        .compile
        .lastOrError
        .compileStage("last")
    }
    val expected = List(
      loc(OpenScope("source")),
      loc(Pull("source", "last", t)),
      loc(OutputChunk(Chunk("Mao", "Popcorn"), token = t)),
      loc(Pull("source", "last", t)),
      loc(Done(t)),
      loc(CloseScope("source")),
      loc(Finished(at = "last", errored = false, value = "Popcorn"))
    )
    assertEvents(actual, expected)
  }

  test("traces multiple combinators") {
    val actual = simple {
      Stream("Mao")[IO]
        .stage("source")
        .map(_.toUpperCase)
        .stage("map")
        .compile
        .lastOrError
        .compileStage("last")
    }
    val expected = List(
      loc(OpenScope("map")),
      loc(Pull("map", "last", t)),
      loc(OpenScope("source")),
      loc(Pull("source", "map", t)),
      loc(Output("Mao", t)),
      loc(Output("MAO", t)),
      loc(Pull("map", "last", t)),
      loc(Pull("source", "map", t)),
      loc(Done(t)),
      loc(CloseScope("source")),
      loc(Done(t)),
      loc(CloseScope("map")),
      loc(Finished(at = "last", errored = false, value = "MAO"))
    )
    assertEvents(actual, expected)
  }

  test("traces zipped streams") {
    val actual = simple {
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
    val expected = List(
      loc(OpenScope("zip")),
      loc(Pull("zip", "last", t)),
      loc(OpenScope("left")),
      loc(Pull("left", "zip", t)),
      loc(Output("Mao", t)),
      loc(OpenScope("right")),
      loc(Pull("right", "zip", t)),
      loc(Output("Popcorn", t)),
      loc(Output("(Mao,Popcorn)", t)),
      loc(Pull("zip", "last", t)),
      loc(Pull("left", "zip", t)),
      loc(Done(t)),
      loc(CloseScope("left")),
      loc(CloseScope("right")),
      loc(Done(t)),
      loc(CloseScope("zip")),
      loc(Finished(at = "last", errored = false, value = "(Mao,Popcorn)"))
    )
    assertEvents(actual, expected)
  }

  test("traces effect evaluation") {
    val actual = simple {
      Stream
        .eval(IO("Mao").trace())
        .stage("source")
        .compile
        .lastOrError
        .compileStage("last")

    }
    val expected = List(
      loc(OpenScope("source")),
      loc(Pull("source", "last", t)),
      loc(Eval("Mao")),
      loc(Output("Mao", t)),
      loc(Pull("source", "last", t)),
      loc(Done(t)),
      loc(CloseScope("source")),
      loc(Finished(at = "last", errored = false, value = "Mao"))
    )
    assertEvents(actual, expected)
  }

  test("traces raising errors") {
    val actual = simple {
      Stream("Mao")[IO]
        .stage("source")
        .evalTap(_ => IO.raiseError[String](Boom))
        .stage("eval")
        .compile
        .lastOrError
        .compileStage("last")
    }
    val expected = List(
      loc(OpenScope(label = "eval")),
      loc(Pull("eval", "last", t)),
      loc(OpenScope(label = "source")),
      loc(Pull("source", "eval", t)),
      loc(Output(value = "Mao", t)),
      loc(Error(value = "BOOM!", t, raisedHere = true)),
      loc(CloseScope(label = "source")),
      loc(CloseScope(label = "eval")),
      loc(Finished(at = "last", errored = true, value = "BOOM!"))
    )

    assertEvents(actual, expected)
  }

  test("traces handling errors") {
    val actual = simple {
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
    val expected = List(
      loc(OpenScope("handle")),
      loc(Pull("handle", "drain", t)),
      loc(OpenScope("eval")),
      loc(Pull("eval", "handle", t)),
      loc(OpenScope("source")),
      loc(Pull("source", "eval", t)),
      loc(Output("Mao", t)),
      loc(
        Error("BOOM!", t, raisedHere = true)
      ),
      loc(CloseScope("source")),
      loc(CloseScope("eval")),
      loc(OpenScope("second")),
      loc(Pull("second", "handle", t)),
      loc(Done(t)),
      loc(CloseScope("second")),
      loc(Done(t)),
      loc(CloseScope("handle")),
      loc(Finished(at = "drain", errored = false, value = "()"))
    )
    assertEvents(actual, expected)
  }

  def bracket(suffix: String = "")(using t: Scape[IO]): Stream[IO, Unit] =
    Stream
      .bracket(IO(s"acquire$suffix").trace().void)(_ =>
        IO(s"release$suffix").trace().void
      )

  test("traces resources and errors") {
    val actual = simple {
      bracket()
        .stage("source")
        .evalTap(_ => IO.raiseError[String](Boom))
        .stage("eval")
        .compile
        .drain
        .compileStage("drain")
    }
    val expected = List(
      loc(OpenScope("eval")),
      loc(Pull("eval", "drain", t)),
      loc(OpenScope("source")),
      loc(Pull("source", "eval", t)),
      loc(Eval("acquire")),
      loc(Output("()", t)),
      loc(Error("BOOM!", t, raisedHere = true)),
      loc(Eval("release")),
      loc(CloseScope("source")),
      loc(CloseScope("eval")),
      loc(Finished(at = "drain", errored = true, value = "BOOM!"))
    )
    assertEvents(actual, expected)
  }

  test("traces resources and error handling") {
    val actual = simple {
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
    val expected = List(
      loc(OpenScope("handle")),
      loc(Pull("handle", "drain", t)),
      loc(OpenScope("eval")),
      loc(Pull("eval", "handle", t)),
      loc(OpenScope("source")),
      loc(Pull("source", "eval", t)),
      loc(Eval("acquire")),
      loc(Output("()", t)),
      loc(
        Error("BOOM!", t, raisedHere = true)
      ),
      loc(Eval("release")),
      loc(CloseScope("source")),
      loc(CloseScope("eval")),
      loc(OpenScope("second")),
      loc(Pull("second", "handle", t)),
      loc(Done(t)),
      loc(CloseScope("second")),
      loc(Done(t)),
      loc(CloseScope("handle")),
      loc(Finished(at = "drain", errored = false, value = "()"))
    )
    assertEvents(actual, expected)
  }

  test("scope: error in parent") {
    val actual = simple {
      bracket("Left")
        .stage("left")
        .zip(
          bracket("Right")
            .stage("right")
        )
        .evalTap(_ => IO.raiseError[String](Boom))
        .stage("eval")
        .compile
        .drain
        .compileStage("drain")
    }
    val expected = List(
      loc(OpenScope("eval")),
      loc(Pull("eval", "drain", t)),
      loc(OpenScope("left")),
      loc(Pull("left", "eval", t)),
      loc(Eval("acquireLeft")),
      loc(Output("()", t)),
      loc(OpenScope("right")),
      loc(Pull("right", "eval", t)),
      loc(Eval("acquireRight")),
      loc(Output("()", t)),
      loc(Error("BOOM!", t, raisedHere = true)),
      loc(Eval("releaseRight")),
      loc(CloseScope("right")),
      loc(Eval("releaseLeft")),
      loc(CloseScope("left")),
      loc(CloseScope("eval")),
      loc(Finished(at = "drain", errored = true, value = "BOOM!"))
    )
    assertEvents(actual, expected)
  }

  test("scope: error in child") {
    val actual = simple {
      bracket("Left")
        .stage("left")
        .zip(
          bracket("Right")
            .evalTap(_ => IO.raiseError[String](Boom))
            .stage("right")
        )
        .stage("zip")
        .compile
        .drain
        .compileStage("drain")
    }
    val expected = List(
      loc(OpenScope("zip")),
      loc(Pull("zip", "drain", t)),
      loc(OpenScope("left")),
      loc(Pull("left", "zip", t)),
      loc(Eval("acquireLeft")),
      loc(Output("()", t)),
      loc(OpenScope("right")),
      loc(Pull("right", "zip", t)),
      loc(Eval("acquireRight")),
      loc(Error("BOOM!", t, raisedHere = true)),
      loc(Eval("releaseRight")),
      loc(CloseScope("right")),
      loc(Eval("releaseLeft")),
      loc(CloseScope("left")),
      loc(Error("BOOM!", t, raisedHere = false)),
      loc(CloseScope("zip")),
      loc(Finished(at = "drain", errored = true, value = "BOOM!"))
    )
    assertEvents(actual, expected)
  }

  test("stage parallel execution".ignore) {
    val actualIO = simple {
      Stream("Mao", "Popcorn", "Trouble")[IO]
        .stage("source", branch = "s")
        .fork("root", "s")
        .parEvalMap(2)(IO(_).trace())
        .stage("parEvalMap")
        .compile
        .drain
        .compileStage("drain")
    }
    assertEvents(actualIO, Nil)
  }

  test("traces merged streams") {
    val actualIO = simple {
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
    actualIO.map { actualWithTokens =>
      val actual = actualWithTokens.map(replaceToken)
      val beginning = List(
        loc(OpenScope(label = "merge")),
        loc(Pull("merge", "drain", t))
      )
      assertEquals(actual.take(beginning.size), beginning.map(_._1))
      val left = List(
        loc(OpenScope(label = "left")),
        loc(Pull("left", "merge", t)),
        loc(
          Output(value = "Mao", t)
        ),
        loc(Output(value = "Mao", t)),
        loc(Pull("left", "merge", t)),
        loc(Done(t)),
        loc(CloseScope(label = "left"))
      )
      assertContains(actual.drop(beginning.size), left)
      val right = List(
        loc(OpenScope(label = "right")),
        loc(Pull("right", "merge", t)),
        loc(Output(value = "Popcorn", t)),
        loc(Output(value = "Popcorn", t)),
        loc(Pull("right", "merge", t)),
        loc(Done(t)),
        loc(CloseScope(label = "right"))
      )
      assertContains(actual.drop(beginning.size), right)
    }
  }
}
