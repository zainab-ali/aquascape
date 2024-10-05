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
import cats.effect.Async
import cats.syntax.all.*
import fs2.*

type Branch = String

private object Stack {
  opaque type Stack[F[_]] = Ref[F, Map[Branch, List[Label]]]

  def apply[F[_]: Async]: F[Stack[F]] =
    Ref.of[F, Map[Branch, (List[Label])]](Map.empty)

  extension [F[_]: MonadCancelThrow](stack: Stack[F]) {
    private[aquascape] def bracketF[A](branch: Branch, child: Label)(
        fa: F[A]
    ): F[A] =
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
    private[aquascape] def newRoot(root: Branch): F[Unit] = {
      stack.update { bs => bs + ((root, Nil)) }
    }

    private[aquascape] def forkTS(parent: Branch, child: Branch): F[Unit] = {
      val updateOrError = stack.modify { bs =>
        bs.get(parent) match {
          case None => (bs, Some(ParentBranchNotFound(parent, child)))
          case Some(parentLabels) => (bs + ((child, parentLabels)), None)
        }
      }
      updateOrError.flatMap {
        case Some(err) => err.raiseError
        case None      => ().pure
      }
    }

    private[aquascape] def peek(branch: Branch): F[List[Label]] = {
      stack.get.map(_.getOrElse(branch, Nil))
    }

    private[aquascape] def bracket[O, A](branch: Branch, child: Label)(
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
}
