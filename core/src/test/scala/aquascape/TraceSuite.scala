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

import cats.effect.{Trace => _, *}
import fs2.*
import munit.CatsEffectSuite
import munit.Location

class TraceSuite extends CatsEffectSuite {
  object Boom extends Throwable("BOOM!")

  import Event.*

  def step(labels: List[String], e: Event)(using
      loc: munit.Location
  ): (Step, Location) =
    Step(labels, e) -> loc

  def assertSteps(actualIO: IO[List[Step]], expected: List[(Step, Location)])(
      using loc: munit.Location
  ): IO[Unit] = {
    actualIO.map { actual =>
      actual.zipWithIndex
        .zip(expected)
        .map { case ((a, i), (e, l)) =>
          val event = (a.e, e.e) match {
            case (Event.Pull(_), Event.Pull(t))           => Event.Pull(t)
            case (Event.Done(_), Event.Done(t))           => Event.Done(t)
            case (Event.Output(v, _), Event.Output(_, t)) => Event.Output(v, t)
            case (a: Event.Error, e: Event.Error) => a.copy(token = e.token)
            case (Event.OutputChunk(v, _), Event.OutputChunk(_, t)) =>
              Event.OutputChunk(v, t)
            case (a, _) => a
          }
          assertEquals(
            a.copy(e = event),
            e,
            s"Step $i is incorrect - ${pprint(actual)}"
          )(
            l,
            summon[Step <:< Step]
          )
        }
      assertEquals(
        actual.length,
        expected.length,
        s"Wrong number of steps obtained. ${pprint(actual)}"
      )
    }.void
  }

  val t = new Unique.Token()

  test("traces a single combinator") {
    val actual = Trace.simple { (_: Trace[IO]) ?=>
      Stream("Mao")[IO]
        .trace("source")
        .compile
        .lastOrError
        .traceCompile("last")
    }
    val expected = List(
      step(labels = List("last"), e = OpenScope(label = "source")),
      step(labels = List("source", "last"), e = Pull(t)),
      step(
        labels = List("source", "last"),
        e = Output(value = "Mao", token = t)
      ),
      step(labels = List("source", "last"), e = Pull(t)),
      step(labels = List("source", "last"), e = Done(t)),
      step(labels = List("last"), e = CloseScope(label = "source")),
      step(labels = List("last"), e = Finished(errored = false, value = "Mao"))
    )

    assertSteps(actual, expected)
  }
  test("traces chunks") {
    val actual = Trace.simpleChunked { (_: Trace[IO]) ?=>
      Stream("Mao", "Popcorn")[IO]
        .trace("source")
        .compile
        .lastOrError
        .traceCompile("last")
    }
    val expected = List(
      step(List("last"), OpenScope("source")),
      step(List("source", "last"), Pull(t)),
      step(
        List("source", "last"),
        OutputChunk(Chunk("Mao", "Popcorn"), token = t)
      ),
      step(List("source", "last"), Pull(t)),
      step(List("source", "last"), Done(t)),
      step(List("last"), CloseScope("source")),
      step(List("last"), Finished(errored = false, value = "Popcorn"))
    )
    assertSteps(actual, expected)
  }

  test("traces multiple combinators") {
    val actual = Trace.simple { (_: Trace[IO]) ?=>
      Stream("Mao")[IO]
        .trace("source")
        .map(_.toUpperCase)
        .trace("map")
        .compile
        .lastOrError
        .traceCompile("last")
    }
    val expected = List(
      step(List("last"), OpenScope("map")),
      step(List("map", "last"), Pull(t)),
      step(List("map", "last"), OpenScope("source")),
      step(List("source", "map", "last"), Pull(t)),
      step(List("source", "map", "last"), Output("Mao", t)),
      step(List("map", "last"), Output("MAO", t)),
      step(List("map", "last"), Pull(t)),
      step(List("source", "map", "last"), Pull(t)),
      step(List("source", "map", "last"), Done(t)),
      step(List("map", "last"), CloseScope("source")),
      step(List("map", "last"), Done(t)),
      step(List("last"), CloseScope("map")),
      step(List("last"), Finished(errored = false, value = "MAO"))
    )
    assertSteps(actual, expected)
  }

  test("traces zipped streams") {
    val actual = Trace.simple { (_: Trace[IO]) ?=>
      Stream("Mao")[IO]
        .trace("left")
        .zip(
          Stream("Popcorn")[IO]
            .trace("right")
        )
        .trace("zip")
        .compile
        .lastOrError
        .traceCompile("last")
    }
    val expected = List(
      step(List("last"), OpenScope("zip")),
      step(List("zip", "last"), Pull(t)),
      step(List("zip", "last"), OpenScope("left")),
      step(List("left", "zip", "last"), Pull(t)),
      step(List("left", "zip", "last"), Output("Mao", t)),
      step(List("zip", "last"), OpenScope("right")),
      step(List("right", "zip", "last"), Pull(t)),
      step(List("right", "zip", "last"), Output("Popcorn", t)),
      step(List("zip", "last"), Output("(Mao,Popcorn)", t)),
      step(List("zip", "last"), Pull(t)),
      step(List("left", "zip", "last"), Pull(t)),
      step(List("left", "zip", "last"), Done(t)),
      step(List("zip", "last"), CloseScope("left")),
      step(List("zip", "last"), CloseScope("right")),
      step(List("zip", "last"), Done(t)),
      step(List("last"), CloseScope("zip")),
      step(List("last"), Finished(errored = false, value = "(Mao,Popcorn)"))
    )
    assertSteps(actual, expected)
  }

  test("traces effect evaluation") {
    val actual = Trace.simple { (_: Trace[IO]) ?=>
      Stream
        .eval(IO("Mao").traceF())
        .trace("source")
        .compile
        .lastOrError
        .traceCompile("last")

    }
    val expected = List(
      step(List("last"), OpenScope("source")),
      step(List("source", "last"), Pull(t)),
      step(List("source", "last"), Eval("Mao")),
      step(List("source", "last"), Output("Mao", t)),
      step(List("source", "last"), Pull(t)),
      step(List("source", "last"), Done(t)),
      step(List("last"), CloseScope("source")),
      step(List("last"), Finished(errored = false, value = "Mao"))
    )
    assertSteps(actual, expected)
  }

  test("traces raising errors") {
    val actual = Trace.simple { (_: Trace[IO]) ?=>
      Stream("Mao")[IO]
        .trace("source")
        .evalTap(_ => IO.raiseError[String](Boom))
        .trace("eval")
        .compile
        .lastOrError
        .traceCompile("last")
    }
    val expected = List(
      step(labels = List("last"), e = OpenScope(label = "eval")),
      step(labels = List("eval", "last"), e = Pull(t)),
      step(labels = List("eval", "last"), e = OpenScope(label = "source")),
      step(labels = List("source", "eval", "last"), e = Pull(t)),
      step(
        labels = List("source", "eval", "last"),
        e = Output(value = "Mao", t)
      ),
      step(
        labels = List("eval", "last"),
        e = Error(value = "BOOM!", t, raisedHere = true)
      ),
      step(labels = List("last"), e = CloseScope(label = "source")),
      step(labels = List("last"), e = CloseScope(label = "eval")),
      step(labels = List("last"), e = Finished(errored = true, value = "BOOM!"))
    )

    assertSteps(actual, expected)
  }

  test("traces handling errors") {
    val actual = Trace.simple { (_: Trace[IO]) ?=>
      Stream("Mao")[IO]
        .trace("source")
        .evalTap(_ => IO.raiseError[String](Boom))
        .trace("eval")
        .handleErrorWith(_ => Stream.empty[IO].map(_.toString).trace("second"))
        .trace("handle")
        .compile
        .drain
        .traceCompile("drain")
    }
    val expected = List(
      step(List("drain"), OpenScope("handle")),
      step(List("handle", "drain"), Pull(t)),
      step(List("handle", "drain"), OpenScope("eval")),
      step(List("eval", "handle", "drain"), Pull(t)),
      step(List("eval", "handle", "drain"), OpenScope("source")),
      step(List("source", "eval", "handle", "drain"), Pull(t)),
      step(List("source", "eval", "handle", "drain"), Output("Mao", t)),
      step(
        List("eval", "handle", "drain"),
        Error("BOOM!", t, raisedHere = true)
      ),
      step(List("handle", "drain"), CloseScope("source")),
      step(List("handle", "drain"), CloseScope("eval")),
      step(List("handle", "drain"), OpenScope("second")),
      step(List("second", "handle", "drain"), Pull(t)),
      step(List("second", "handle", "drain"), Done(t)),
      step(List("handle", "drain"), CloseScope("second")),
      step(List("handle", "drain"), Done(t)),
      step(List("drain"), CloseScope("handle")),
      step(List("drain"), Finished(errored = false, value = "()"))
    )
    assertSteps(actual, expected)
  }

  def bracket(suffix: String = "")(using t: Trace[IO]): Stream[IO, Unit] =
    Stream
      .bracket(IO(s"acquire$suffix").traceF().void)(_ =>
        IO(s"release$suffix").traceF().void
      )

  test("traces resources and errors") {
    val actual = Trace.simple { (_: Trace[IO]) ?=>
      bracket()
        .trace("source")
        .evalTap(_ => IO.raiseError[String](Boom))
        .trace("eval")
        .compile
        .drain
        .traceCompile("drain")
    }
    val expected = List(
      step(List("drain"), OpenScope("eval")),
      step(List("eval", "drain"), Pull(t)),
      step(List("eval", "drain"), OpenScope("source")),
      step(List("source", "eval", "drain"), Pull(t)),
      step(List("source", "eval", "drain"), Eval("acquire")),
      step(List("source", "eval", "drain"), Output("()", t)),
      step(List("eval", "drain"), Error("BOOM!", t, raisedHere = true)),
      step(List("drain"), Eval("release")),
      step(List("drain"), CloseScope("source")),
      step(List("drain"), CloseScope("eval")),
      step(List("drain"), Finished(errored = true, value = "BOOM!"))
    )
    assertSteps(actual, expected)
  }

  test("traces resources and error handling") {
    val actual = Trace.simple { (_: Trace[IO]) ?=>
      bracket()
        .trace("source")
        .evalTap(_ => IO.raiseError[String](Boom))
        .trace("eval")
        .handleErrorWith(_ => Stream.empty[IO].as(()).trace("second"))
        .trace("handle")
        .compile
        .drain
        .traceCompile("drain")
    }
    val expected = List(
      step(List("drain"), OpenScope("handle")),
      step(List("handle", "drain"), Pull(t)),
      step(List("handle", "drain"), OpenScope("eval")),
      step(List("eval", "handle", "drain"), Pull(t)),
      step(List("eval", "handle", "drain"), OpenScope("source")),
      step(List("source", "eval", "handle", "drain"), Pull(t)),
      step(List("source", "eval", "handle", "drain"), Eval("acquire")),
      step(List("source", "eval", "handle", "drain"), Output("()", t)),
      step(
        List("eval", "handle", "drain"),
        Error("BOOM!", t, raisedHere = true)
      ),
      step(List("handle", "drain"), Eval("release")),
      step(List("handle", "drain"), CloseScope("source")),
      step(List("handle", "drain"), CloseScope("eval")),
      step(List("handle", "drain"), OpenScope("second")),
      step(List("second", "handle", "drain"), Pull(t)),
      step(List("second", "handle", "drain"), Done(t)),
      step(List("handle", "drain"), CloseScope("second")),
      step(List("handle", "drain"), Done(t)),
      step(List("drain"), CloseScope("handle")),
      step(List("drain"), Finished(errored = false, value = "()"))
    )
    assertSteps(actual, expected)
  }

  test("scope: error in parent") {
    val actual = Trace.simple { (_: Trace[IO]) ?=>
      bracket("Left")
        .trace("left")
        .zip(
          bracket("Right")
            .trace("right")
        )
        .evalTap(_ => IO.raiseError[String](Boom))
        .trace("eval")
        .compile
        .drain
        .traceCompile("drain")
    }
    val expected = List(
      step(List("drain"), OpenScope("eval")),
      step(List("eval", "drain"), Pull(t)),
      step(List("eval", "drain"), OpenScope("left")),
      step(List("left", "eval", "drain"), Pull(t)),
      step(List("left", "eval", "drain"), Eval("acquireLeft")),
      step(List("left", "eval", "drain"), Output("()", t)),
      step(List("eval", "drain"), OpenScope("right")),
      step(List("right", "eval", "drain"), Pull(t)),
      step(List("right", "eval", "drain"), Eval("acquireRight")),
      step(List("right", "eval", "drain"), Output("()", t)),
      step(List("eval", "drain"), Error("BOOM!", t, raisedHere = true)),
      step(List("drain"), Eval("releaseRight")),
      step(List("drain"), CloseScope("right")),
      step(List("drain"), Eval("releaseLeft")),
      step(List("drain"), CloseScope("left")),
      step(List("drain"), CloseScope("eval")),
      step(labels = List("drain"), Finished(errored = true, value = "BOOM!"))
    )
    assertSteps(actual, expected)
  }

  test("scope: error in child") {
    val actual = Trace.simple { (_: Trace[IO]) ?=>
      bracket("Left")
        .trace("left")
        .zip(
          bracket("Right")
            .evalTap(_ => IO.raiseError[String](Boom))
            .trace("right")
        )
        .trace("zip")
        .compile
        .drain
        .traceCompile("drain")
    }
    val expected = List(
      step(List("drain"), OpenScope("zip")),
      step(List("zip", "drain"), Pull(t)),
      step(List("zip", "drain"), OpenScope("left")),
      step(List("left", "zip", "drain"), Pull(t)),
      step(List("left", "zip", "drain"), Eval("acquireLeft")),
      step(List("left", "zip", "drain"), Output("()", t)),
      step(List("zip", "drain"), OpenScope("right")),
      step(List("right", "zip", "drain"), Pull(t)),
      step(List("right", "zip", "drain"), Eval("acquireRight")),
      step(List("right", "zip", "drain"), Error("BOOM!", t, raisedHere = true)),
      step(List("zip", "drain"), Eval("releaseRight")),
      step(List("zip", "drain"), CloseScope("right")),
      step(List("zip", "drain"), Eval("releaseLeft")),
      step(List("zip", "drain"), CloseScope("left")),
      step(List("zip", "drain"), Error("BOOM!", t, raisedHere = false)),
      step(List("drain"), CloseScope("zip")),
      step(labels = List("drain"), Finished(errored = true, value = "BOOM!"))
    )
    assertSteps(actual, expected)
  }

  // TODO: Use TestContext?
  // test("traces merged streams".ignored) {
  //   val actual = Trace.simple[IO] { (_: Trace[IO]) ?=>
  //     Stream("Mao")[IO]
  //       .trace("left", branch = "left")
  //       .fork("root", "left")
  //       .merge(
  //         Stream("Popcorn")[IO]
  //           .trace("right", branch = "right")
  //           .fork("root", "right")
  //       )
  //       .trace("merge")
  //   }
  //   // FIXME: There is a bug in the test or code.
  //   val expected = List(
  //     step(labels = List(), e = OpenScope(label = "merge")),
  //     step(labels = List("merge"), e = Pull),
  //     step(labels = List("merge"), e = OpenScope(label = "left")),
  //     step(labels = List("left", "merge"), e = Pull),
  //     step(labels = List("left", "merge"), e = OpenScope(label = "right")),
  //     step(labels = List("left", "merge"), e = Output(value = "Mao")),
  //     step(labels = List("right", "left", "merge"), e = Pull),
  //     step(
  //       labels = List("right", "left", "merge"),
  //       e = Output(value = "Popcorn")
  //     ),
  //     step(labels = List("merge"), e = Output(value = "Mao")),
  //     step(labels = List("merge"), e = Pull),
  //     step(labels = List("left", "merge"), e = Pull),
  //     step(labels = List("left", "merge"), e = Done),
  //     step(labels = List("merge"), e = CloseScope(label = "left")),
  //     step(labels = List("merge"), e = Output(value = "Popcorn")),
  //     step(labels = List("merge"), e = Pull),
  //     step(labels = List("right", "merge"), e = Pull),
  //     step(labels = List("right", "merge"), e = Done),
  //     step(labels = List("merge"), e = CloseScope(label = "right")),
  //     step(labels = List("merge"), e = Done),
  //     step(labels = List(), e = CloseScope(label = "merge")),
  //     step(labels = List(), e = Finished(errored = false))
  //   )
  //   assertSteps(actual, expected)
  // }
}
