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

import cats.effect.*
import doodle.svg.*
import doodle.syntax.all.*

trait PlatformCompanion {
  def draw(picture: Picture[Unit], name: String): IO[Unit] =
    picture.drawWithFrameToIO(Frame(name))

  def parseArgs(args: List[String]): IO[Either[ExitCode, String]] = IO(
    Right("")
  )
}
