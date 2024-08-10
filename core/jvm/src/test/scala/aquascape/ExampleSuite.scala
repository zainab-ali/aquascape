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

import aquascape.*
import aquascape.golden.*
import cats.Show
import cats.effect.*
import cats.syntax.all.*
import fs2.*
import fs2.io.file.Path
import munit.*

import scala.concurrent.duration.*

trait LowPriorityShow {
  given Show[Either[Throwable, Char]] = {
    case Left(Scape.Caught(err)) => s"Left(${err.getMessage})"
    case Left(err)               => s"Left(${err.getMessage})"
    case Right(c)                => s"Right(${c.show})"
  }
}


class Examples extends GoldenSuite with LowPriorityShow {

  given GroupName = GroupName(
    Path(s"${aquascape.BuildInfo.baseDirectory}/docs")
  )

  given Show[Nothing] = _ => sys.error("Unreachable code.")

  object Err extends Throwable("Err")
}
