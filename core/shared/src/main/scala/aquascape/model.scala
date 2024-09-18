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
import cats.effect.Unique.Token
type Label = String

private final case class Time(seconds: Int)

private enum Event {
  case Pull(to: String, from: String, token: Token)
  case Done(token: Token)
  case Eval(at: String, value: String)
  case EvalError(value: String)
  case Error(value: String, token: Token, raisedHere: Boolean)
  case Output(value: String, token: Token)
  case OutputChunk(value: fs2.Chunk[String], token: Token)
  case OpenScope(label: Label)
  case CloseScope(label: Label)
// TODO: Zainab - This should have a label associated with it.
  case Finished(at: String, errored: Boolean, value: String)
}
