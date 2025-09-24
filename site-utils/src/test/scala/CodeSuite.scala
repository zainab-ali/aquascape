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
import cats.effect.IO
import cats.syntax.all.*
import fs2.Stream
import munit.*
import snapshot4s.generated.snapshotConfig
import snapshot4s.munit.*

class CodeSuite extends CatsEffectSuite with SnapshotAssertions {

  test("strips calls to stage") {
    Scape.chunked[IO].map { scape =>
      given Scape[IO] = scape
      val streamCode = code {
        Stream('a')
          .covary[IO]
          .stage("Stream('a')", branch = "left")
          .fork("root", "left")
          .merge(
            Stream('b')
              .evalMap(_.pure[IO].trace())
              .evalTap(_.pure[IO].trace_())
              .stage("Stream('b')", branch = "right")
              .fork("root", "right")
          )
          .stage("merge")
          .compile
          .toList
          .compileStage("compile.toList")
      }
      assertInlineSnapshot(
        streamCode.code,
        """Stream('a')
  .covary[IO]
  .merge(Stream('b').evalMap(_.pure[IO]).evalTap(_.pure[IO]))
  .compile
  .toList"""
      )
    }
  }

  test("strips the outer braces from block elements") {
    val streamCode = code {
      val ab = Stream('a', 'b').covary[IO]
      val xy = Stream('x', 'y').covary[IO]
      ab.merge(xy).compile.toList
    }
    assertInlineSnapshot(
      streamCode.code,
      """val ab = Stream('a', 'b').covary[IO]
val xy = Stream('x', 'y').covary[IO]

ab.merge(xy).compile.toList"""
    )
  }
}
