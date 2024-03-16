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
import doodle.core.format.*
import doodle.java2d.*
import doodle.syntax.all.*

object AquascapeApp {
  trait Core {
    def config: Config = Config.default

    def stream(using Scape[IO]): IO[Unit]
  }

  trait Simple extends IOApp.Simple with Core {
    def run: IO[Unit] = Scape
      .unchunked[IO]
      .flatMap { case t @ given Scape[IO] =>
        stream(using t).draw()
      }
      .flatMap(_.drawToIO())
  }
  object Simple {
    abstract class File(name: String) extends IOApp.Simple with Core {
      def run: IO[Unit] = Scape
        .unchunked[IO]
        .flatMap { case t @ given Scape[IO] =>
          stream(using t).draw()
        }
        .flatMap(_.writeToIO[Png](s"$name.png"))
    }
  }

  trait Chunked extends IOApp.Simple with Core {
    def run: IO[Unit] = Scape
      .unchunked[IO]
      .flatMap { case t @ given Scape[IO] =>
        stream(using t).draw()
      }
      .flatMap(_.drawToIO())
  }

  object Chunked {
    abstract class File(name: String) extends IOApp.Simple with Core {
      def run: IO[Unit] = Scape
        .chunked[IO]
        .flatMap { case t @ given Scape[IO] =>
          stream(using t).draw()
        }
        .flatMap(_.writeToIO[Png](s"$name.png"))
    }
  }
}
