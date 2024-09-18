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

package docs.reference
import aquascape.*
import aquascape.examples.*
import aquascape.examples.syntax.given
import cats.Show
import cats.effect.*
import cats.effect.IO
import cats.syntax.all.*
import fs2.*
import fs2.concurrent.*

import scala.concurrent.duration.*
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("DocsReferenceTopic")
object topic {
  def constructionTime(max: Int): InputBox[Int] = InputBox.int(
    labelText = "n (construction time)",
    defaultValue = 1,
    min = 0,
    max = max
  )
  def subscriberBound(max: Int): InputBox[Int] = InputBox.int(
    labelText = "n (subscriber bound)",
    defaultValue = 0,
    min = 0,
    max = max
  )

  @JSExport
  val topic = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        Topic[IO, Char].flatMap { topic =>
          val pub = Stream('a', 'b')
            .delayBy[IO](1.second)
            .stage("pub", branch = "pub")
          val sub = topic.subscribeUnbounded
            .stage("subscribeUnbounded", branch = "sub")
          (
            sub.compile.toList
              .compileStage("sub…toList", branch = "sub"),
            pub
              .through(topic.publish)
              .compile
              .drain
              .compileStage("pub…drain", branch = "pub")
          ).parTupled
        }
      }
  }

  @JSExport
  val delayedSubscriber = new ExampleWithInput[Int] {
    val inputBox: InputBox[Int] = constructionTime(5)
    def apply(n: Int)(using Scape[IO]): StreamCode =
      code {
        Topic[IO, Char].flatMap { topic =>
          val pub = Stream('a', 'b')
            .spaced[IO](2.second, startImmediately = false)
            .stage("pub", branch = "pub")
          val sub = topic.subscribeUnbounded
            .stage("sub", branch = "sub")
          (
            sub
              .delayBy(n.seconds)
              .compile
              .toList
              .compileStage(s"sub…toList", branch = "sub"),
            pub
              .through(topic.publish)
              .compile
              .drain
              .compileStage("pub…drain", branch = "pub")
          ).parTupled
        }
      }
  }

  @JSExport
  val multipleSubscribers = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        Topic[IO, Char].flatMap { topic =>
          val pub = Stream('a', 'b')
            .delayBy[IO](1.second)
            .stage("pub", branch = "pub")
          val subA = topic.subscribeUnbounded
            .stage("subA", branch = "subA")
          val subB = topic.subscribeUnbounded
            .stage("subB", branch = "subB")

          (
            subA.compile.toList
              .compileStage("subA…toList", branch = "subA"),
            subB.compile.toList
              .compileStage("subB…toList", branch = "subB"),
            pub
              .through(topic.publish)
              .compile
              .drain
              .compileStage("pub…drain", branch = "pub")
          ).parTupled
        }
      }
  }

  @JSExport
  val slowSubscriber = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        Topic[IO, Char].flatMap { topic =>
          val pub = Stream('a', 'b')
            .delayBy[IO](1.second)
            .stage("pub", branch = "pub")
          val sub = topic.subscribeUnbounded
            .spaced(2.seconds)
            .stage("sub", branch = "sub")

          (
            sub.compile.toList
              .compileStage("sub…toList", branch = "sub"),
            pub
              .through(topic.publish)
              .compile
              .drain
              .compileStage("pub…drain", branch = "pub")
          ).parTupled
        }
      }
  }
  @JSExport
  val boundedSubscriber = new ExampleWithInput[Int] {

    val inputBox: InputBox[Int] = subscriberBound(3)

    def apply(n: Int)(using Scape[IO]): StreamCode =
      code {
        Topic[IO, Char].flatMap { topic =>
          val pub = Stream('a', 'b', 'c', 'd')
            .delayBy[IO](1.second)
            .stage("pub", branch = "pub")
          val sub = topic
            .subscribe(n)
            .spaced(1.second)
            .stage("sub", branch = "sub")
          (
            sub.compile.toList
              .compileStage("sub…toList", branch = "sub"),
            pub
              .through(topic.publish)
              .compile
              .drain
              .compileStage("pub…drain", branch = "pub")
          ).parTupled
        }
      }
  }

  @JSExport
  val multipleSubscribersBounded = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        Topic[IO, Char].flatMap { topic =>
          val pub = Stream('a', 'b', 'c')
            .delayBy[IO](1.second)
            .stage("pub", branch = "pub")
          val subA = topic.subscribeUnbounded
            .stage("subA", branch = "subA")
          val subB = topic
            .subscribe(0)
            .spaced(1.second)
            .stage("subB", branch = "subB")

          (
            subA.compile.toList
              .compileStage("subA…toList", branch = "subA"),
            subB.compile.toList
              .compileStage("subB…toList", branch = "subB"),
            pub
              .through(topic.publish)
              .compile
              .drain
              .compileStage("pub…drain", branch = "pub")
          ).parTupled
        }
      }
  }

}
