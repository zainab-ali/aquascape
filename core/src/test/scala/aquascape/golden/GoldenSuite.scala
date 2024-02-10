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
    test(new TestOptions(name))(fs*)
  }
  def test(
      name: TestOptions
  )(fs: (TestName ?=> IO[Unit])*)(using parent: GroupName): Unit = {
    val testName = TestName(name.name, parent.value)
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
    def write(suffix: String)(using Trace[IO]): IO[Unit] = {
      import doodle.java2d.*
      val streamCode: StreamCode = f
      val exampleName: ExampleName =
        ExampleName(s"${name}${suffix}", parent, "png")
      writeSource(streamCode.pos, exampleName) >> drawStream[
        doodle.core.format.Png,
        Frame
      ](
        streamCode.stream,
        exampleName
      )
    }
    Trace
      .unchunked[IO]
      .flatMap { case given Trace[IO] =>
        write("")
      }
      .unlessA(drawChunked == DrawChunked.OnlyChunked) >>
      Trace
        .chunked[IO]
        .flatMap { case given Trace[IO] =>
          write(" (with chunks)")
        }
        .unlessA(drawChunked == DrawChunked.No)
  }

  def animate(name: String, drawChunked: DrawChunked = DrawChunked.Yes)(
      f: Trace[IO] ?=> StreamCode
  )(using parent: TestName): IO[Unit] = {
    def write(suffix: String)(using Trace[IO]): IO[Unit] = {
      val streamCode: StreamCode = f
      val exampleName: ExampleName =
        ExampleName(s"${name}${suffix}", parent, "gif")
      writeSource(streamCode.pos, exampleName) >> animateStream(
        streamCode.stream,
        exampleName
      )
    }
    Trace
      .unchunked[IO]
      .flatMap { case given Trace[IO] =>
        write("")
      }
      .unlessA(drawChunked == DrawChunked.OnlyChunked) >>
      Trace
        .chunked[IO]
        .flatMap { case given Trace[IO] =>
          write(" (with chunks)")
        }
        .unlessA(drawChunked == DrawChunked.No)
  }
}

import doodle.effect.Writer
import doodle.core.format.Format

private def drawStream[Fmt <: Format, Frame](
    stream: IO[Any],
    exampleName: ExampleName
)(using
    Trace[IO],
    Writer[aquascape.drawing.Picture.Algebra, Frame, Fmt]
): IO[Unit] =
  import aquascape.*
  import doodle.syntax.all.*
  val targetDir = exampleName.parent.imageDir
  Files[IO].createDirectories(targetDir) >>
    stream.draw().flatMap(_.writeToIO[Fmt](exampleName.imageFile.toString))

private def animateStream(
    stream: IO[Any],
    exampleName: ExampleName
)(using
    Trace[IO]
): IO[Unit] =
  import aquascape.*
  import doodle.interact.syntax.all._
  import doodle.java2d.*
  import doodle.core.format.*
  val frame = doodle.java2d.effect.Frame.default.withSize(1000, 1000)
  val targetDir = exampleName.parent.imageDir
  Files[IO].createDirectories(targetDir) >>
    (stream
      .animate() ++ Stream.exec(IO.println("Finished animating")))
      .writeToIO[Gif]
      .apply(exampleName.imageFile.toString, frame)(
        summon,
        java2dGifAnimationWriter
      )
