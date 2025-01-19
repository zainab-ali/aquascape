package myaquascapes

import cats.effect.*
import aquascape.*
import fs2.*

object App extends AquascapeApp.Batch {
  val aquascapes = List(
    new Aquascape {
      def name = "myaquascape"
      def stream(using Scape[IO]): IO[Unit] = {
        Stream(1, 2, 3)
          .stage("Stream(1, 2, 3)")
          .compile
          .toList
          .compileStage(
            "compile.toList"
          )
          .void
      }
    }
  )
}
