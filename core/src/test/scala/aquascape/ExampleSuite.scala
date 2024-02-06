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

import aquascape.*
import aquascape.golden.*
import cats.Show
import cats.effect.{Trace => _, *}
import cats.syntax.all.*
import fs2.*
import fs2.io.file.Path
import munit.*

class Examples extends GoldenSuite {

  given GroupName = GroupName(
    Path(s"${aquascape.BuildInfo.baseDirectory}/docs")
  )

  given Show[Nothing] = Show.fromToString
  given Show[Either[Throwable, Char]] = {
    case Left(Trace.Caught(err)) => s"Left(${err.getMessage})"
    case Left(err)               => s"Left(${err.getMessage})"
    case Right(c)                => s"Right(${c.show})"
  }

  group("basic") {
    test("compile")(
      example("toList")(
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      ),
      example("drain")(
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .compile
            .drain
            .traceCompile("compile.drain")
        )
      ),
      example("last")(
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .compile
            .last
            .traceCompile("compile.last")
        )
      ),
      example("count")(
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .compile
            .count
            .traceCompile("compile.count")
        )
      ),
      example("onlyOrError")(
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .compile
            .onlyOrError
            .traceCompile("compile.onlyOrError")
        )
      )
    )

    test("take")(
      example("fewer")(
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .take(2)
            .trace("take(2)")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      ),
      example("more")(
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .take(5)
            .trace("take(5)")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      )
    )

    test("drop")(
      example("fewer")(
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .drop(2)
            .trace("drop(2)")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      ),
      example("more")(
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .drop(5)
            .trace("drop(5)")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      )
    )
  }
  group("effects") {
    test("evalMap")(
      example("evalMap")(
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .evalMap(_.pure[IO].traceF())
            .trace("evalMap")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      )
    )
  }

  object Err extends Throwable("Err")
  group("errors") {
    test("raising errors")(
      example("raiseError")(
        range(
          (Stream.raiseError[IO](Err) ++ Stream('a'))
            .trace("Stream.raiseError[IO](Err)")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      ),
      example("in evalMap") {
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .evalMap(x => IO.raiseWhen(x == 'b')(Err).traceF())
            .trace("evalMap(IO.raiseWhen(_ == 'b')(Err))")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      },
      example("propagation") {
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .evalMap(x => IO.raiseWhen(x == 'b')(Err))
            .trace("evalMap(IO.raiseWhen(_ == 'b')(Err))")
            .map(identity)
            .trace("map(identity)")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      }
    )
    test("handling errors")(
      example("handleError") {
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .evalTap(x => IO.raiseWhen(x == 'b')(Err))
            .trace("evalTap(IO.raiseWhen(_ == 'b')(Err))")
            .handleError(_ => 'd')
            .trace("handleError(_ => 'd')")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      },
      example("handleErrorWith") {
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .evalTap(x => IO.raiseWhen(x == 'b')(Err))
            .trace("evalTap(…)")
            .handleErrorWith(_ =>
              Stream('d', 'e', 'f').trace("Stream('d','e','f')")
            )
            .trace("handleErrorWith(_ => …)")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      },
      example("attempt") {
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .evalTap(x => IO.raiseWhen(x == 'b')(Err))
            .trace("evalTap(…)")
            .attempt
            .trace("attempt")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      }
    )
  }
  group("resources") {
    test("bracket")(
      example("bracket")(
        range(
          Stream
            .bracket(IO("abc").traceF())(_ => IO("d").traceF().void)
            .trace("Stream.bracket(…)")
            .flatMap { str =>
              Stream
                .emits(str.toList)
                .trace("Stream.emits(str.toList)")
            }
            .trace("flatMap {…}")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      ),
      example("raising errors")(
        range(
          Stream
            .bracket(IO("abc").traceF())(_ => IO("d").traceF().void)
            .trace("Stream.bracket(…)")
            .flatMap { str =>
              Stream
                .emits(str.toList)
                .trace("Stream.emits(str.toList)")
                .evalTap(x => IO.raiseWhen(x == 'b')(Err))
                .trace("evalTap(…)")
            }
            .trace("flatMap {…}")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      ),
      example("handing errors")(
        range(
          Stream
            .bracket(IO("abc").traceF())(_ => IO("d").traceF().void)
            .trace("Stream.bracket(…)")
            .flatMap { str =>
              Stream
                .emits(str.toList)
                .trace("Stream.emits(str.toList)")
                .evalTap(x => IO.raiseWhen(x == 'b')(Err))
                .trace("evalTap(…)")
            }
            .trace("flatMap {…}")
            .handleErrorWith(_ => Stream('e', 'f').trace("Stream('e','f')"))
            .trace("handleErrorWith(…)")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      )
    )
  }
}
