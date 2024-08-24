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

package aquascape.examples
import aquascape.Scape
import cats.*
import cats.effect.Resource
import cats.syntax.all.*

private trait LowPriorityShow {
  given Show[Unit] = _ => "()"

  given Show[Either[Throwable, Char]] = {
    case Left(Scape.Caught(err)) => s"Left(${err.getMessage})"
    case Left(err)               => s"Left(${err.getMessage})"
    case Right(c)                => s"Right(${c.show})"
  }
  given Show[Resource.ExitCase] = {
    case Resource.ExitCase.Canceled   => "Canceled"
    case _: Resource.ExitCase.Errored => "Errored"
    case Resource.ExitCase.Succeeded  => "Succeeded"
  }

}

object syntax extends LowPriorityShow {
  given Show[Nothing] = _ => sys.error("Unreachable code.")
}
