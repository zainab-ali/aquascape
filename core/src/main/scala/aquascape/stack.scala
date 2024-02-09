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
import fs2.*

type Branch = String

opaque type Stack[F[_]] = Ref[F, Map[Branch, List[Label]]]
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
