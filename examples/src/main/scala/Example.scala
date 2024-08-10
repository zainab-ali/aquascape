package aquascape.examples

import aquascape.*
import cats.effect.*
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doodle.svg.*
import doodle.syntax.all.*
import org.scalajs.dom
import cats.syntax.all.*

import scala.scalajs.js.annotation.JSExport

trait Example {
  @JSExport
  final def draw(codeId: String, unchunkedFrameId: String, chunkedFrameId: String): Unit = {
    drawFrames(codeId, Option(unchunkedFrameId), Option(chunkedFrameId))(apply)
  }

  def apply(using Scape[IO]): StreamCode
}

private def drawFrames(
  codeId: String,
  unchunkedFrameId: Option[String],
  chunkedFrameId: Option[String]
)(stream: Scape[IO] ?=> StreamCode): Unit = {
  val program = for {
    code1 <- unchunkedFrameId.traverse(drawFrame(_, false)(stream))
    code2 <- chunkedFrameId.traverse(drawFrame(_, true)(stream))
    codeEl <- IO(dom.document.getElementById(codeId))
    _ <- IO(codeEl.textContent = code1.orElse(code2).getOrElse(throw new Error("This is a bug in the aquascape examples.")).code)
  } yield ()
  program.unsafeRunAsync {
    case Left(err) => throw err
    case Right(_)  => ()
  }
}

private def drawFrame(frameId: String, chunked: Boolean)(stream: Scape[IO] ?=> StreamCode): IO[StreamCode] = {
  val frame = Frame(frameId)
  for {
    t <- if (chunked) Scape.chunked[IO] else Scape.unchunked[IO]
    given Scape[IO] = t
    streamCode = stream(using t)
    picture <- streamCode.stream.attempt.void.draw()
    _ <- picture.drawWithFrameToIO(frame)
  } yield streamCode
}
