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

import aquascape.*
import cats.effect.*
import cats.effect.testkit.*
import cats.effect.unsafe.implicits.global
import cats.syntax.all.*
import doodle.svg.*
import doodle.syntax.all.*
import org.scalajs.dom

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

sealed trait FrameIds

@JSExportTopLevel("ExampleFrameIds")
object FrameIds {
  final case class Unchunked(id: String) extends FrameIds
  final case class Chunked(id: String) extends FrameIds
  final case class Both(unchunkedId: String, chunkedId: String) extends FrameIds

  @JSExport
  def unchunked(id: String): FrameIds = Unchunked(id)
  @JSExport
  def chunked(id: String): FrameIds = Chunked(id)
  @JSExport
  def both(unchunkedId: String, chunkedId: String): FrameIds =
    Both(unchunkedId, chunkedId)
}

trait Example {
  @JSExport
  final def draw(codeId: String, frameIds: FrameIds): Unit = {
    drawFrames(codeId, frameIds)(apply).unsafeRunAsync(getOrThrow)
  }

  def apply(using Scape[IO]): StreamCode
}

trait ExampleWithInput[A] {

  def inputBox: InputBox[A]

  @JSExport
  final def setup(
      labelId: String,
      inputId: String,
      codeId: String,
      frameIds: FrameIds
  ): Unit = {
    val program = for {
      _ <- drawFrames(codeId, frameIds)(apply(inputBox.default))
      _ <- drawLabel(labelId, inputBox.label)
      _ <- setValue(inputBox, inputId)
    } yield ()
    program.unsafeRunAsync(getOrThrow)
  }

  @JSExport
  final def draw(codeId: String, frameIds: FrameIds, param: String): Unit = {
    inputBox
      .decode(param)
      .traverse_ { a => drawFrames(codeId, frameIds)(apply(a)) }
      .unsafeRunAsync(getOrThrow)
  }

  def apply(a: A)(using Scape[IO]): StreamCode
}

final class Symbol(picture: Picture[Unit]) {
  @JSExport
  def draw(id: String): Unit = {
    picture.drawWithFrameToIO(Frame(id)).unsafeRunAsync(getOrThrow)
  }
}

private def getOrThrow(either: Either[Throwable, Unit]): Unit = either match {
  case Left(err) => throw err
  case Right(_)  => ()
}

private def drawFrames(
    codeId: String,
    frameIds: FrameIds
)(stream: Scape[IO] ?=> StreamCode): IO[Unit] = {
  for {
    code <- frameIds match {
      case FrameIds.Unchunked(id) => drawFrame(id, false)(stream)
      case FrameIds.Chunked(id)   => drawFrame(id, true)(stream)
      case FrameIds.Both(unchunkedId, chunkedId) =>
        drawFrame(unchunkedId, false)(stream) *> drawFrame(chunkedId, true)(
          stream
        )
    }
    _ <- drawCode(codeId, code.code)
  } yield ()
}

private def drawFrame(frameId: String, chunked: Boolean)(
    stream: Scape[IO] ?=> StreamCode
): IO[StreamCode] = {
  for {
    scape <- if (chunked) Scape.chunked[IO] else Scape.unchunked[IO]
    given Scape[IO] = scape
    streamCode = stream(using scape)
    picture <- TestControl.executeEmbed(
      streamCode.stream.attempt.void.draw(),
      seed = Some("MTIzNDU=")
    )
    frameEl <- IO(dom.document.getElementById(frameId))
    // If there is already an image, remove it.
    _ <- IO(
      Option(frameEl.firstChild).foreach(child => frameEl.removeChild(child))
    )
    _ <- picture.drawWithFrameToIO(Frame(frameId))
  } yield streamCode
}

private def drawCode(codeId: String, codeText: String): IO[Unit] = for {
  codeEl <- IO(dom.document.getElementById(codeId))
  _ <- IO(codeEl.textContent = codeText)
    .whenA(codeEl.textContent.trim.isEmpty)
} yield ()
private def drawLabel(labelId: String, label: String): IO[Unit] = {
  for {
    labelEl <- IO(dom.document.getElementById(labelId))
    _ <- IO(labelEl.textContent = label)
  } yield ()
}
private def setValue[A](inputBox: InputBox[A], inputId: String): IO[Unit] = {
  for {
    inputEl <- IO(dom.document.getElementById(inputId))
    _ <- IO(inputEl.setAttribute("type", inputBox.inputType))
    _ <- IO(inputEl.setAttribute("value", inputBox.encode(inputBox.default)))
    _ <- inputBox.attributes.toList.traverse_ { case (k, v) =>
      IO(inputEl.setAttribute(k, v))
    }
  } yield ()
}
