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

import aquascape.drawing.*
import cats.*
import cats.effect.Async
import cats.effect.Concurrent
import cats.syntax.all.*
import fs2.*

extension [F[_], A: Show](s: Stream[F, A])(using t: Stage[F]) {

  def stage(label: String, branch: String = root): Stream[F, A] =
    t.stage(label, branch)(s)

  def fork(from: String, to: String): Stream[F, A] = t.fork(from, to)(s)
}

extension [F[_]: Concurrent, O: Show](fo: F[O])(using t: Stage[F]) {
  def trace(branch: String = root): F[O] = t.trace(fo, branch)
  def compileStage(label: String): F[O] = t.compileStage(fo, label)
}

extension [F[_]: Async, O](fo: F[O])(using t: Stage[F]) {
  def draw(config: Config = Config.default): F[Picture[Unit]] = {
    t.events(fo)
      .compile
      .toVector
      .map(_.toPicture(config))
  }
}
