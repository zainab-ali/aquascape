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

import aquascape.drawing.Config
import cats.effect.*
import cats.syntax.all.*

trait Aquascape {
  def name: String

  def chunked: Boolean = false

  def config: Config = Config.default

  def stream(using Scape[IO]): IO[Unit]
}

object AquascapeApp extends PlatformCompanion {

  final class Args(
      val name: String,
      val chunked: Boolean,
      val config: Config,
      val stream: Scape[IO] ?=> IO[Unit]
  )

  def run(args: Args): IO[Unit] = for {
    scape <- if (args.chunked) Scape.chunked[IO] else Scape.unchunked[IO]
    given Scape[IO] = scape
    picture <- args.stream(using scape).draw(args.config)
    _ <- draw(picture, args.name)
  } yield ()

  trait Batch extends IOApp {
    def aquascapes: List[Aquascape]

    final def run(args: List[String]): IO[ExitCode] = parseArgs(args).flatMap {
      case Left(exitCode) => exitCode.pure
      case Right(prefix)  =>
        aquascapes
          .traverse_(aquascape =>
            AquascapeApp.run(
              Args(
                s"$prefix${aquascape.name}",
                aquascape.chunked,
                aquascape.config,
                aquascape.stream
              )
            )
          )
          .as(ExitCode.Success)
    }
  }
}

trait AquascapeApp extends IOApp with Aquascape {
  final def run(args: List[String]): IO[ExitCode] = {
    AquascapeApp.parseArgs(args).flatMap {
      case Left(exitCode) => exitCode.pure
      case Right(prefix)  =>
        AquascapeApp
          .run(AquascapeApp.Args(s"$prefix${name}", chunked, config, stream))
          .as(ExitCode.Success)

    }
  }
}
