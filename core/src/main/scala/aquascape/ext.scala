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
import cats.effect.Concurrent
import cats.syntax.all.*
import fs2.*

extension [F[_], A: Show](s: Stream[F, A])(using t: Trace[F]) {

  def trace(label: String, branch: String = root): Stream[F, A] =
    t.trace(label, branch)(s)

  def fork(from: String, to: String): Stream[F, A] = t.fork(from, to)(s)
}

extension [F[_]: Concurrent, O: Show](fo: F[O])(using t: Trace[F]) {
  def traceF(branch: String = root): F[O] = t.trace(fo, root)
  def traceCompile(label: String): F[Unit] = t.traceCompile(fo, label)
}

extension [F[_]: Concurrent, O](fo: F[O])(using t: Trace[F]) {
  def draw(config: Config = Config.default): F[Picture[Unit]] = {
    t.steps(fo)
      .compile
      .toList
      .map(_.toPicture(config))
  }
  def animate(config: Config = Config.default): Stream[F, Picture[Unit]] = {
    t.steps(fo)
      .scan(List.empty[Step])((acc, step) => acc :+ step)
      .map(_.toPicture(config))
  }
}
