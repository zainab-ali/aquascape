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

package aquascape.golden
import cats.effect.IO

import scala.quoted.*

private def rangeImpl(
    stream: Expr[IO[Any]]
)(using q: Quotes): Expr[StreamCode] = {
  import q.reflect.*
  val startAt = Expr(stream.asTerm.pos.start)
  val endAt = Expr(stream.asTerm.pos.end)
  val path = Expr(Position.ofMacroExpansion.sourceFile.getJPath.get.toString)
  '{
    StreamCode(
      pos = RangePos(source = $path, startAt = $startAt, endAt = $endAt),
      stream = $stream
    )
  }
}

inline def range(stream: IO[Any]): StreamCode = ${ rangeImpl('stream) }
