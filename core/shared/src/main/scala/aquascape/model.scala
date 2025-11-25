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

import cats.Eq
import cats.effect.Async
import cats.effect.Ref
import cats.syntax.all.*

private final case class Label(label: String, lineNumber: Int)
implicit val labelEq: Eq[Label] = (x: Label, y: Label) => {
  x.label == y.label && x.lineNumber == y.lineNumber
}

private final case class Time(seconds: Int)

private object Token {
  private[aquascape] type Token = Int

  def generator[F[_]: Async]: F[F[Token]] = Ref.of[F, Token](0).map { counter =>
    counter.getAndUpdate(_ + 1)
  }
}

import Token.Token

private enum Event {

  case Pull(to: Label, from: Label, token: Token)
  case Done(token: Token)
  case Eval(value: String)
  case Error(value: String, token: Token, raisedHere: Boolean)
  case Output(value: String, token: Token)
  case OutputChunk(value: List[String], token: Token)
  case Finished(at: Label, errored: Boolean, value: String)
}
