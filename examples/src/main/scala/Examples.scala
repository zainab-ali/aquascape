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

package aquascape.examples

import aquascape.*
import aquascape.examples.syntax.given
import cats.Show
import cats.effect.*
import cats.effect.IO
import fs2.*

import scala.concurrent.duration.*
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("BasicCompileToList")
object BasicCompileToList extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("BasicCompileDrain")
object BasicCompileDrain extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .compile
        .drain
        .compileStage("compile.drain")
    )
}

@JSExportTopLevel("BasicCompileLast")
object BasicCompileLast extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .compile
        .last
        .compileStage("compile.last")
    )
}

@JSExportTopLevel("BasicCompileCount")
object BasicCompileCount extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .compile
        .count
        .compileStage("compile.count")
    )
}

@JSExportTopLevel("BasicCompileOnlyOrError")
object BasicCompileOnlyOrError extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')")
        .compile
        .onlyOrError
        .compileStage("compile.onlyOrError")
    )
}

@JSExportTopLevel("ChunkingChunkRepartition")
object ChunkingChunkRepartition extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream("Hel", "l", "o Wor", "ld")
        .chunkLimit(1)
        .unchunks
        .stage("""Stream(…)…unchunks""")
        .repartition(s => Chunk.array(s.split(" ")))
        .stage("repartition(…)")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("BufferingBuffer")
object BufferingBuffer extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .chunkLimit(1)
        .unchunks
        .stage("Stream('a','b','c')…unchunks")
        .buffer(2)
        .stage("buffer(2)")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("BufferingBufferAll")
object BufferingBufferAll extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .chunkLimit(1)
        .unchunks
        .stage("Stream('a','b','c')…unchunks")
        .bufferAll
        .stage("bufferAll")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("BufferingBufferBy")
object BufferingBufferBy extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .chunkLimit(1)
        .unchunks
        .stage("Stream('a','b','c')…unchunks")
        .bufferBy(_ != 'b')
        .stage("bufferBy")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

object Err extends Throwable("Err")

@JSExportTopLevel("CombiningParZip")
object CombiningParZip extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .stage("Stream('a','b','c')", "left")
        .fork("root", "left")
        .parZip(
          Stream('d', 'e')
            .stage("Stream('d','e')", "right")
            .fork("root", "right")
        )
        .stage("parZip(…)")
        .compile
        .drain
        .compileStage("compile.drain")
    )
}
@JSExportTopLevel("BroadcastThroughBasic")
object BroadcastThroughBasic extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .chunkLimit(1)
        .unchunks
        .stage("Stream('a','b','c')", branch = "upstream")
        .evalTap(x => IO.raiseWhen(x == 'b')(Err))
        .stage("evalTap(…)", branch = "upstream")
        .fork("root", "upstream")
        .broadcastThrough(in =>
          in.metered(1.second)
            .stage("in.metered(…)", branch = "broadcast")
            .fork("root", "broadcast")
        )
        .stage("broadcastThrough")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("BroadcastThroughErrorPropagation")
object BroadcastThroughErrorPropagation extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .chunkLimit(1)
        .unchunks
        .stage("Stream('a','b','c')", branch = "upstream")
        .evalTap(x => IO.raiseWhen(x == 'b')(Err))
        .stage("evalTap(…)", branch = "upstream")
        .fork("root", "upstream")
        .broadcastThrough(in =>
          in.metered(1.second)
            .stage("in.metered(…)", branch = "broadcast")
            .fork("root", "broadcast")
        )
        .stage("broadcastThrough")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("BroadcastThroughDifferentRates")
object BroadcastThroughDifferentRates extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream('a', 'b', 'c')
        .chunkLimit(1)
        .unchunks
        .stage("Stream('a','b', 'c')…", branch = "upstream")
        .fork("root", "upstream")
        .broadcastThrough(
          in =>
            in.metered(1.second)
              .stage("in.metered(1s)", branch = "broadcast1")
              .fork("root", "broadcast1"),
          in =>
            in.metered(100.second)
              .stage("in.metered(100s)", branch = "broadcast2")
              .fork("root", "broadcast2")
        )
        .stage("broadcastThrough")
        .compile
        .toList
        .compileStage("compile.toList")
    )
}

@JSExportTopLevel("EffectsExec")
object EffectsExec extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream
        .exec(IO('a').trace().void)
        .stage("Stream.exec(…)")
        .compile
        .last
        .compileStage("compile.last")
    )
}

@JSExportTopLevel("EffectsEval")
object EffectsEval extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
      Stream
        .eval(IO('a').trace())
        .stage("Stream.eval(…)")
        .compile
        .last
        .compileStage("compile.last")
    )
}

@JSExportTopLevel("EffectsParEvalMap")
object EffectsParEvalMap extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
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
}

@JSExportTopLevel("EffectsParEvalMapUnordered")
object EffectsParEvalMapUnordered extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
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
}

@JSExportTopLevel("TimeAwakeEvery")
object TimeAwakeEvery extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
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
}

@JSExportTopLevel("TimeDelayBy")
object TimeDelayBy extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
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
}

@JSExportTopLevel("TimeMetered")
object TimeMetered extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
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
}

@JSExportTopLevel("TimeDebounce")
object TimeDebounce extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
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
}

@JSExportTopLevel("TimeDebounceAwake")
object TimeDebounceAwake extends Example {
  def apply(using Scape[IO]): StreamCode =
    code(
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
}
