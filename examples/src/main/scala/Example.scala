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
import cats.effect.IO
import cats.effect.testkit.*
import cats.effect.unsafe.implicits.global
import doodle.svg.*
import doodle.syntax.all.*
import org.scalajs.dom
import cats.syntax.all.*

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
    drawFrames(codeId, frameIds)(apply).unsafeRunAsync {
      case Left(err) => throw err
      case Right(_)  => ()
    }
  }

  def apply(using Scape[IO]): StreamCode
}

/** Describes how a value encodes to and from an <input> element.
  *
  * In practice, this only works for numbers. More work is needed to support
  * coproducts (e.g. with radio buttons).
  */
trait FormCodec[A] {

  def attributes: Map[String, String]
  def inputType: String

  def decode(text: String): Option[A]
  def encode(a: A): String
}

trait ExampleWithInput[A: FormCodec] {

  def label: String
  def default: A

  @JSExport
  final def setup(
      labelId: String,
      inputId: String,
      codeId: String,
      frameIds: FrameIds
  ): Unit = {
    val program = for {
      _ <- drawFrames(codeId, frameIds)(apply(default))
      _ <- drawLabel(labelId, label)
      _ <- setValue(inputId, default)
    } yield ()
    program.unsafeRunAsync {
      case Left(err) => throw err
      case Right(_)  => ()
    }
  }

  @JSExport
  final def draw(codeId: String, frameIds: FrameIds, param: String): Unit = {
    summon[FormCodec[A]]
      .decode(param)
      .traverse_ { a => drawFrames(codeId, frameIds)(apply(a)) }
      .unsafeRunAsync {
        case Left(err) => throw err
        case Right(_)  => ()
      }
  }

  def apply(a: A)(using Scape[IO]): StreamCode
}

private def drawFrames(
    codeId: String,
    frameIds: FrameIds
)(stream: Scape[IO] ?=> StreamCode): IO[Unit] = {
  for {
    code <- {
      frameIds match {
        case FrameIds.Unchunked(id) => drawFrame(id, false)(stream)
        case FrameIds.Chunked(id)   => drawFrame(id, true)(stream)
        case FrameIds.Both(unchunkedId, chunkedId) =>
          drawFrame(unchunkedId, false)(stream) *> drawFrame(chunkedId, true)(
            stream
          )
      }
    }
    codeEl <- IO(dom.document.getElementById(codeId))
    _ <- IO(codeEl.textContent = code.code)
      .whenA(codeEl.textContent.trim.isEmpty)
  } yield ()
}

private def drawFrame(frameId: String, chunked: Boolean)(
    stream: Scape[IO] ?=> StreamCode
): IO[StreamCode] = {
  val frame = Frame(frameId)
  for {
    t <- if (chunked) Scape.chunked[IO] else Scape.unchunked[IO]
    given Scape[IO] = t
    streamCode = stream(using t)
    picture <- TestControl.executeEmbed(
      streamCode.stream.attempt.void.draw(),
      seed = Some("MTIzNDU=")
    )
    frameEl <- IO(dom.document.getElementById(frameId))
    // If there is already an image, remove it.
    _ <- IO(
      Option(frameEl.firstChild).foreach(child => frameEl.removeChild(child))
    )
    _ <- picture.drawWithFrameToIO(frame)
  } yield streamCode
}

private def drawLabel(labelId: String, label: String): IO[Unit] = {
  for {
    labelEl <- IO(dom.document.getElementById(labelId))
    _ <- IO(labelEl.textContent = label)
  } yield ()
}
private def setValue[A: FormCodec](inputId: String, default: A): IO[Unit] = {
  for {
    inputEl <- IO(dom.document.getElementById(inputId))
    codec = summon[FormCodec[A]]
    _ <- IO(inputEl.setAttribute("type", codec.inputType))
    _ <- IO(inputEl.setAttribute("value", codec.encode(default)))
    _ <- codec.attributes.toList.traverse_ { case (k, v) =>
      IO(inputEl.setAttribute(k, v))
    }
  } yield ()
}
