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
import aquascape.macros.LineNumber
import cats.*
import cats.effect.Async
import cats.effect.Concurrent
import cats.syntax.all.*
import fs2.*

private val defaultBranch: String = "root"

extension [F[_], A: Show](s: Stream[F, A])(using t: Scape[F], ln: LineNumber) {
  def stage(label: String, branch: String = defaultBranch): Stream[F, A] = {
    t.stage(Label(label, ln.lineNumber), branch)(s)
  }

  def fork(from: String, to: String): Stream[F, A] = t.fork(from, to)(s)
}

extension [F[_]: Concurrent, O: Show](fo: F[O])(using t: Scape[F]) {
  def trace(branch: String = defaultBranch): F[O] = t.trace(fo, branch)
  def trace_(branch: String = defaultBranch): F[Unit] = t.trace(fo, branch).void
  def compileStage(label: String, branch: String = defaultBranch)(using
      ln: LineNumber
  ): F[O] =
    t.compileStage(fo, Label(label, ln.lineNumber), branch)
}

extension [F[_]: Async, O](fo: F[O])(using t: Scape[F]) {
  def run(config: Config = Config.default): F[(Picture[Unit], O)] = {
    t.events(fo)
      .map { case (events, o) => (events.toPicture(config), o) }
  }

  def draw(config: Config = Config.default): F[Picture[Unit]] = {
    run(config).map(_._1)
  }
}
