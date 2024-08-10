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
import cats.effect.*
import cats.syntax.all.*
import fs2.*
import fs2.io.file.Path
import munit.*

import scala.concurrent.duration.*

trait LowPriorityShow {
  given Show[Either[Throwable, Char]] = {
    case Left(Scape.Caught(err)) => s"Left(${err.getMessage})"
    case Left(err)               => s"Left(${err.getMessage})"
    case Right(c)                => s"Right(${c.show})"
  }
}


class Examples extends GoldenSuite with LowPriorityShow {

  given GroupName = GroupName(
    Path(s"${aquascape.BuildInfo.baseDirectory}/docs")
  )

  given Show[Nothing] = _ => sys.error("Unreachable code.")

  group("effects") {
    test("effects")(
      example("evalMap")(
        range(
          Stream('a', 'b', 'c')
            .stage("Stream('a','b','c')")
            .evalMap(_.pure[IO].trace())
            .stage("evalMap")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      ),
      example("evalMap2")(
        range(
          Stream('a', 'b', 'c')
            .stage("Stream('a','b','c')")
            .evalTap(char => IO(s"$char 1").trace())
            .stage("evalTap1")
            .evalTap(char => IO(s"$char 2").trace())
            .stage("evalTap2")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      ),
      example("exec")(
        range(
          Stream
            .exec(IO('a').trace().void)
            .stage("Stream.exec(…)")
            .compile
            .last
            .compileStage("compile.last")
        )
      ),
      example("eval")(
        range(
          Stream
            .eval(IO('a').trace())
            .stage("Stream.eval(…)")
            .compile
            .last
            .compileStage("compile.last")
        )
      ),
      example("parEvalMap")(
        range(
          Stream('a', 'b', 'c', 'd', 'e')
            .stage("Stream('a','b','c', 'd', 'e')", "upstream")
            .fork("root", "upstream")
            .parEvalMap(2)(char =>
              IO.sleep((105 - char.toInt).seconds).as(char).trace()
            )
            .stage("parEvalMap(2)(…)")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      ),
      example("parEvalMapUnordered")(
        range(
          Stream('a', 'b', 'c', 'd', 'e')
            .stage("Stream('a','b','c', 'd', 'e')", "upstream")
            .fork("root", "upstream")
            .parEvalMapUnordered(2)(char =>
              IO.sleep((105 - char.toInt).seconds).as(char).trace()
            )
            .stage("parEvalMapUnordered(2)(…)")
            .compile
            .toList
            .compileStage("compile.toList")
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
            .stage("Stream.raiseError[IO](Err)")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      ),
      example("in evalMap") {
        range(
          Stream('a', 'b', 'c')
            .stage("Stream('a','b','c')")
            .evalMap(x => IO.raiseWhen(x == 'b')(Err).trace())
            .stage("evalMap(IO.raiseWhen(_ == 'b')(Err))")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      },
      example("propagation") {
        range(
          Stream('a', 'b', 'c')
            .stage("Stream('a','b','c')")
            .evalMap(x => IO.raiseWhen(x == 'b')(Err))
            .stage("evalMap(IO.raiseWhen(_ == 'b')(Err))")
            .map(identity)
            .stage("map(identity)")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      }
    )
    test("handling errors")(
      example("handleError") {
        range(
          Stream('a', 'b', 'c')
            .stage("Stream('a','b','c')")
            .evalTap(x => IO.raiseWhen(x == 'b')(Err))
            .stage("evalTap(IO.raiseWhen(_ == 'b')(Err))")
            .handleError(_ => 'd')
            .stage("handleError(_ => 'd')")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      },
      example("handleError2") {
        range(
          Stream('a', 'b', 'c')
            .stage("Stream('a','b','c')")
            .evalMap(x =>
              IO.raiseWhen(x == 'b')(Err)
                .as(x)
                .handleError(_ => 'd')
                .trace()
            )
            .stage("evalTap(…)")
            .handleError(_ => 'd')
            .stage("handleError(_ => 'd')")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      },
      example("handleErrorWith") {
        range(
          Stream('a', 'b', 'c')
            .stage("Stream('a','b','c')")
            .evalTap(x => IO.raiseWhen(x == 'b')(Err))
            .stage("evalTap(…)")
            .handleErrorWith(_ =>
              Stream('d', 'e', 'f').stage("Stream('d','e','f')")
            )
            .stage("handleErrorWith(_ => …)")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      },
      example("attempt") {
        range(
          Stream('a', 'b', 'c')
            .stage("Stream('a','b','c')")
            .evalTap(x => IO.raiseWhen(x == 'b')(Err))
            .stage("evalTap(…)")
            .attempt
            .stage("attempt")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      },
      example("attempts") {
        range(
          Stream('a', 'b', 'c')
            .stage("Stream('a','b','c')")
            .evalTap(x => IO.raiseWhen(x == 'b')(Err))
            .stage("evalTap(…)")
            .attempts(Stream.empty)
            .stage("attempts")
            .take(4)
            .compile
            .toList
            .compileStage("compile.toList")
        )
      }
    )
  }
  group("resources") {
    test("bracket")(
      example("bracket")(
        range(
          Stream
            .bracket(IO("abc").trace())(_ => IO("d").trace().void)
            .stage("Stream.bracket(…)")
            .flatMap { str =>
              Stream
                .emits(str.toList)
                .stage("Stream.emits(str.toList)")
            }
            .stage("flatMap {…}")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      ),
      example("raising errors")(
        range(
          Stream
            .bracket(IO("abc").trace())(_ => IO("d").trace().void)
            .stage("Stream.bracket(…)")
            .flatMap { str =>
              Stream
                .emits(str.toList)
                .stage("Stream.emits(str.toList)")
                .evalTap(x => IO.raiseWhen(x == 'b')(Err))
                .stage("evalTap(…)")
            }
            .stage("flatMap {…}")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      ),
      example("handing errors")(
        range(
          Stream
            .bracket(IO("abc").trace())(_ => IO("d").trace().void)
            .stage("Stream.bracket(…)")
            .flatMap { str =>
              Stream
                .emits(str.toList)
                .stage("Stream.emits(str.toList)")
                .evalTap(x => IO.raiseWhen(x == 'b')(Err))
                .stage("evalTap(…)")
            }
            .stage("flatMap1")
            .flatMap { str =>
              Stream(str)
                .repeatN(2)
                .stage("Stream.(str).repeatN(2)")
                .evalTap(x => IO.raiseWhen(x == 'b')(Err))
                .stage("evalTap1(…)")
            }
            .stage("flatMap {…}")
            .handleErrorWith(_ => Stream('e', 'f').stage("Stream('e','f')"))
            .stage("handleErrorWith(…)")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      )
    )
  }
  group("time") {
    test("time")(
      example("awakeEvery", DrawChunked.OnlyChunked)(
        range(
          Stream
            .awakeEvery[IO](5.seconds)
            .map(_.toSeconds)
            .stage("Stream.awakeEvery(5.seconds).map(…)")
            .take(2)
            .stage("take(2)")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      ),
      example("delayBy", DrawChunked.OnlyChunked)(
        range(
          Stream('a', 'b', 'c')
            .chunkLimit(1)
            .unchunks
            .stage("Stream('a','b','c')…unchunks")
            .delayBy(5.seconds)
            .stage("delayBy(5.seconds)")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      ),
      example("metered", DrawChunked.OnlyChunked)(
        range(
          Stream('a', 'b', 'c')
            .chunkLimit(1)
            .unchunks
            .stage("Stream('a','b','c')…unchunks")
            .metered(5.seconds)
            .stage("metered(5.seconds)")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      ),
      example("debounce", DrawChunked.OnlyChunked)(
        range(
          Stream('a', 'b', 'c')
            .chunkLimit(1)
            .unchunks
            .stage("Stream('a','b','c')…unchunks")
            .debounce(5.seconds)
            .stage("debounce(5.seconds)")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      ),
      example("debounce awake", DrawChunked.OnlyChunked)(
        range(
          Stream
            .awakeEvery[IO](1.seconds)
            .map(_.toSeconds)
            .stage("Stream.awakeEvery(1.seconds)…take(5)", "upstream")
            .fork("root", "upstream")
            .debounce(2.seconds)
            .stage("debounce(2.seconds)")
            .take(2)
            .stage("take(2)")
            .compile
            .toList
            .compileStage("compile.toList")
        )
      )
    )
  }
}
