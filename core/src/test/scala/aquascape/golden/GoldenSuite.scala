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

package aquascape.golden

import aquascape.Trace
import cats.effect.IO
import cats.syntax.all.*
import fs2.*
import fs2.io.file.Files
import munit.*

trait GoldenSuite extends CatsEffectSuite {

  def group(
      name: String
  )(f: GroupName ?=> Unit)(using parent: GroupName): Unit = {
    f(using GroupName(parent.value / name))
  }

  def test(
      name: String
  )(fs: (TestName ?=> IO[Unit])*)(using parent: GroupName): Unit = {
    val testName = TestName(name, parent.value)
    val run =
      Files[IO].deleteIfExists(testName.markdownFile) >> writeHeader(
        testName
      ) >> fs.traverse(
        _.apply(using testName)
      )
    super.test(name)(run)
  }

  def example(name: String, drawChunked: DrawChunked = DrawChunked.Yes)(
      f: Trace[IO] ?=> StreamCode
  )(using parent: TestName): IO[Unit] = {
    Trace
      .unchunked[IO]
      .flatMap { case given Trace[IO] =>
        val streamCode: StreamCode = f
        val exampleName: ExampleName = ExampleName(s"${name}", parent)
        writeSource(streamCode.pos, exampleName) >> drawStream(
          streamCode.stream,
          exampleName
        )
      }
      .unlessA(drawChunked == DrawChunked.OnlyChunked) >>
      Trace
        .chunked[IO]
        .flatMap { case given Trace[IO] =>
          val streamCode: StreamCode = f
          val exampleName: ExampleName =
            ExampleName(s"${name} (with chunks)", parent)
          writeSource(streamCode.pos, exampleName) >> drawStream(
            streamCode.stream,
            exampleName
          )
        }
        .unlessA(drawChunked == DrawChunked.No)
  }
}

private def drawStream(
    stream: IO[Any],
    exampleName: ExampleName
)(using Trace[IO]): IO[Unit] =
  import aquascape.*
  import doodle.core.format.*
  import doodle.java2d.*
  import doodle.syntax.all.*
  val targetDir = exampleName.parent.imageDir
  Files[IO].createDirectories(targetDir) >>
    stream.draw().flatMap(_.writeToIO[Png](exampleName.imageFile.toString))
