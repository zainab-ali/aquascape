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

trait LowPriorityShow {
  given Show[Either[Throwable, Char]] = {
    case Left(Trace.Caught(err)) => s"Left(${err.getMessage})"
    case Left(err)               => s"Left(${err.getMessage})"
    case Right(c)                => s"Right(${c.show})"
  }
}

class Examples extends GoldenSuite with LowPriorityShow {

  given GroupName = GroupName(
    Path(s"${aquascape.BuildInfo.baseDirectory}/docs")
  )

  given Show[Nothing] = _ => sys.error("Unreachable code.")

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
      ),
      example("from an infinite stream")(
        range(
          Stream('a').repeat
            .trace("Stream('a').repeat")
            .take(2)
            .trace("take(2)")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      ),
      example("from a drained stream")(
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .drain
            .trace("drain")
            .take(2)
            .trace("take(2)")
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
    test("filtering")(
      example("filter")(
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .filter(_ == 'b')
            .trace("filter(_ == 'b')")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      ),
      example("exists")(
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .exists(_ == 'b')
            .trace("exists(_ == 'b')")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      ),
      example("forall")(
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .forall(_ == 'b')
            .trace("forall(_ == 'b')")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      ),
      example("changes")(
        range(
          Stream('a', 'b', 'b', 'a', 'c')
            .trace("Stream('a','b','b','a','c')")
            .changes
            .trace("changes")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      )
    )
  }
  group("chunking") {
    test("chunks")(
      example("chunkLimit")(
        range(
          Stream('a', 'b', 'c')
            .chunkLimit(2)
            .unchunks
            .trace("Stream('a','b','c')…unchunks")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      )
    )
    test("buffering")(
      example("buffer")(
        range(
          Stream('a', 'b', 'c')
            .chunkLimit(1)
            .unchunks
            .trace("Stream('a','b','c')…unchunks")
            .buffer(2)
            .trace("buffer(2)")
            .head
            .trace("head")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      ),
      example("bufferAll")(
        range(
          Stream('a', 'b', 'c')
            .chunkLimit(1)
            .unchunks
            .trace("Stream('a','b','c')…unchunks")
            .bufferAll
            .trace("bufferAll")
            .head
            .trace("head")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      )
    )
  }
  group("combining") {
    test("append")(
      example("append")(
        range(
          (Stream('a', 'b')
            .trace("Stream('a','b')")
            ++ Stream('c').trace("Stream('c')"))
            .trace("++")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      )
    )
    test("flatMap")(
      example("flatMap")(
        range(
          Stream("abc")
            .trace("""Stream("abc")""")
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
      example("error propagation")(
        range(
          Stream("abc")
            .trace("""Stream("abc")""")
            .flatMap { _ =>
              Stream
                .raiseError[IO](Err)
                .trace("Stream.raiseError(Err)")
            }
            .trace("flatMap {…}")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      ),
      example("error handling")(
        range(
          Stream("abc")
            .trace("""Stream("abc")""")
            .flatMap { _ =>
              Stream
                .raiseError[IO](Err)
                .trace("Stream.raiseError(Err)")
            }
            .trace("flatMap {…}")
            .handleError(_ => 'a')
            .trace("handleError(_ => 'a')")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      ),
      example("bracket")(
        range(
          Stream('a', 'b', 'c')
            .trace("""Stream('a','b','c')""")
            .flatMap { x =>
              Stream.bracket(IO(s"<$x").traceF())(_ => IO(s"$x>").traceF().void)
            }
            .trace("flatMap {…}")
            .compile
            .toList
            .traceCompile("compile.toList")
        )
      )
    )
  }
  group("effects") {
    test("effects")(
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
      ),
      example("exec")(
        range(
          Stream
            .exec(IO('a').traceF().void)
            .trace("Stream.exec(…)")
            .compile
            .last
            .traceCompile("compile.last")
        )
      ),
      example("eval")(
        range(
          Stream
            .eval(IO('a').traceF())
            .trace("Stream.eval(…)")
            .compile
            .last
            .traceCompile("compile.last")
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
      },
      example("attempts") {
        range(
          Stream('a', 'b', 'c')
            .trace("Stream('a','b','c')")
            .evalTap(x => IO.raiseWhen(x == 'b')(Err))
            .trace("evalTap(…)")
            .attempts(Stream.empty)
            .trace("attempts")
            .take(4)
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
