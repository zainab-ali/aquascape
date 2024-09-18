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

import cats.*
import cats.effect.*
import cats.syntax.all.*
import fs2.*
import fs2.concurrent.Channel

private trait Pen[F[_], E] {
  def bracket[A](branch: Branch, child: Label)(fa: F[A]): F[A]
  def bracket[O, A](branch: Branch, child: Label)(
      fa: Pull[F, O, A]
  ): Pull[F, O, A]
  def writeWithLast(branch: Branch, f: Label => E): F[Unit]
  def writeWithLastTwo(branch: Branch, f: (Label, Label) => E): F[Unit]
  def write(branch: Branch, e: E): F[Unit]
  def newRoot(parent: Branch): F[Unit]
  def fork(parent: Branch, child: Branch): F[Unit]
  def events: Stream[F, E]
  def close: F[Unit]
}

private object Pen {

  def apply[F[_]: Async, E]: F[Pen[F, E]] =
    (
      Ref.of[F, Map[Branch, (List[Label])]](Map.empty),
      Channel.synchronous[F, E]
    ).mapN { case (stack, chan) =>
      new {
        def bracket[A](branch: Branch, child: Label)(fa: F[A]): F[A] =
          stack.bracketF(branch, child)(fa)
        def bracket[O, A](branch: Branch, child: Label)(
            fa: Pull[F, O, A]
        ): Pull[F, O, A] = stack.bracket(branch, child)(fa)
        def writeWithLast(branch: Branch, f: Label => E): F[Unit] =
          stack
            .peek(branch)
            .flatMap(_.headOption.liftTo[F](MissingStageException))
            .map(f)
            .flatMap(s => chan.send(s).void)

        def writeWithLastTwo(branch: Branch, f: (Label, Label) => E): F[Unit] =
          stack.peek(branch).flatMap {
            case h :: t :: _ => f(h, t).pure
            case _           => MissingStageException.raiseError
          } >>= (s => chan.send(s).void)
        def write(branch: Branch, e: E): F[Unit] = chan.send(e).void

        def fork(parent: Branch, child: Branch): F[Unit] =
          stack.forkTS(parent, child)

        def newRoot(parent: Branch): F[Unit] = stack.newRoot(parent)

        def events: Stream[F, E] = chan.stream
        def close: F[Unit] = chan.close.void
      }

    }
}
