package aquascape.examples

import aquascape.*
import cats.effect.*
import cats.effect.IO
import cats.effect.unsafe.implicits.global
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
    drawFrames(codeId, frameIds)(apply)
  }

  def apply(using Scape[IO]): StreamCode
}

private def drawFrames(
    codeId: String,
    frameIds: FrameIds
)(stream: Scape[IO] ?=> StreamCode): Unit = {
  val program = for {
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
  } yield ()
  program.unsafeRunAsync {
    case Left(err) => throw err
    case Right(_)  => ()
  }
}

private def drawFrame(frameId: String, chunked: Boolean)(
    stream: Scape[IO] ?=> StreamCode
): IO[StreamCode] = {
  val frame = Frame(frameId)
  for {
    t <- if (chunked) Scape.chunked[IO] else Scape.unchunked[IO]
    given Scape[IO] = t
    streamCode = stream(using t)
    picture <- streamCode.stream.attempt.void.draw()
    _ <- picture.drawWithFrameToIO(frame)
  } yield streamCode
}
