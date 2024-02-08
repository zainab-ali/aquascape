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
import cats.effect.MonadCancelThrow
import cats.effect.Ref
import cats.syntax.all.*
import fs2.Pull
import fs2.concurrent.Channel

// TODO: Opaque?
type Branch = String

type Stack[F[_]] = Ref[F, Map[Branch, List[Label]]]
extension [F[_]: MonadCancelThrow](stack: Stack[F]) {
  def bracketF[A](branch: Branch, child: Label)(fa: F[A]): F[A] =
    summon[MonadCancelThrow[F]].bracket(
      stack.update { bs =>
        bs.get(branch).fold(bs) { case xs =>
          bs + ((branch, (child :: xs)))
        }
      }
    )(_ => fa)(_ =>
      stack.update { bs =>
        bs.get(branch).fold(bs) { case xs =>
          bs + ((branch, (xs.tail)))
        }
      }
    )
}
extension [F[_]: Functor](stack: Stack[F]) {
  def forkTS(parent: Branch, child: Branch): F[Unit] =
    stack.update { bs =>
      val parentLabels = bs.getOrElse(parent, Nil)
      bs + ((child, (parentLabels)))
    }
  def peek(branch: Branch): F[List[Label]] = {
    stack.get.map(_.getOrElse(branch, Nil))
  }

  def bracket[O, A](branch: Branch, child: Label)(
      fa: Pull[F, O, A]
  ): Pull[F, O, A] =
    Pull.bracketCase(
      acquire = Pull.eval(
        stack.update { bs =>
          bs.get(branch).fold(bs) { case xs =>
            bs + ((branch, (child :: xs)))
          }
        }
      ),
      use = _ => fa,
      release = (_, _) =>
        Pull.eval(
          stack.update { bs =>
            bs.get(branch).fold(bs) { case xs =>
              bs + ((branch, (xs.tail)))
            }
          }
        )
    )
}

case class Pen[F[_]](stack: Stack[F], chan: Channel[F, Step])

import cats.effect.*

val root = "root"

object Pen {
  def apply[F[_]: Async]: F[Pen[F]] =
    (
      Ref.of[F, Map[Branch, (List[Label])]](Map(root -> (Nil))),
      Channel.synchronous[F, Step]
    ).mapN(Pen(_, _))
}
extension [F[_]: MonadCancelThrow](pen: Pen[F]) {
  def bracketF[A](branch: Branch, child: Label)(fa: F[A]): F[A] =
    pen.stack.bracketF(branch, child)(fa)
}
extension [F[_]: Monad](pen: Pen[F]) {
  def bracket[O, A](branch: Branch, child: Label)(
      fa: Pull[F, O, A]
  ): Pull[F, O, A] = pen.stack.bracket(branch, child)(fa)
  def write(branch: Branch, e: Event): F[Unit] =
    pen.stack.peek(branch).map(Step(_, e)) >>= (s => pen.chan.send(s).void)

  def forkF(parent: Branch, child: Branch): F[Unit] =
    pen.stack.forkTS(parent, child)
}
