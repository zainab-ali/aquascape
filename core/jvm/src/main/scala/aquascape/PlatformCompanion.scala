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
import doodle.core.format.*
import doodle.java2d.*
import doodle.syntax.all.*
import fs2.io.file.*
import cats.syntax.all.*
import com.monovore.decline.*
trait PlatformCompanion {
  def draw(picture: Picture[Unit], name: String): IO[Unit] =
    Path(name).parent.traverse_(Files[IO].createDirectories) >> picture
      .writeToIO[Png](s"$name.png")

  def parseOutputPrefix(args: List[String]): String = {
    val outputDirOpt = Opts.option[String](
      "output",
      short = "o",
      metavar = "output",
      help = "The directory in which aquascapes are written."
    ).orNone
    val cmd = Command(
      name = "AquascapeApp.Batch",
      header = "Writes aquascape PNG images"
    )(outputDirOpt)
    cmd.parse(args) match {
      case Left(help) =>
        println(help)
        sys.exit(0)
      case Right(Some(outputDir)) => s"$outputDir/"
      case Right(None) => ""
    }
  }

}
