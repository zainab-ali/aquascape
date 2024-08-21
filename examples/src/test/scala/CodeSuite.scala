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

import munit.*
import cats.effect.IO
import fs2.Stream
import snapshot4s.munit.*
import snapshot4s.generated.snapshotConfig
class CodeSuite extends FunSuite with SnapshotAssertions {
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

ab.merge(xy).compile.toList
"""
    )
  }
}
