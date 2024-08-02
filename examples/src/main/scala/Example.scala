package aquascape.examples

import aquascape.*
import cats.effect.*
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import doodle.svg.*
import doodle.syntax.all.*
import org.scalajs.dom

import scala.scalajs.js.annotation.JSExport
trait Example {
  @JSExport
  final def draw(frameId: String, codeId: String): Unit = {
    drawFrame(frameId, codeId)(apply)
  }

  def apply(using Scape[IO]): StreamCode
}

private def drawFrame(
    frameId: String,
    codeId: String
)(stream: Scape[IO] ?=> StreamCode): Unit = {
  val frame = Frame(frameId)
  val program = for {
    t <- Scape.unchunked[IO]
    given Scape[IO] = t
    streamCode = stream(using t)
    picture <- streamCode.stream.draw()
    _ <- picture.drawWithFrameToIO(frame)
    codeEl <- IO(dom.document.getElementById(codeId))
    _ <- IO(codeEl.textContent = streamCode.code)
  } yield ()
  program.unsafeRunAsync {
    case Left(err) => throw err
    case Right(_)  => ()
  }
}
