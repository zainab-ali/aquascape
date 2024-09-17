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
import fs2.*
import fs2.concurrent.*

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.concurrent.duration.*

@JSExportTopLevel("DocsReferenceTopic")
object topic {

  @JSExport
  val topic = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        Topic[IO, Char].flatMap { topic =>
          val input = Stream('a', 'b')
            .delayBy[IO](1.second)
            .stage("input", branch = "pub")
          val publisher = input
            .through(topic.publish)
            .stage("publisher", branch = "pub")
            .fork("root", "pub")
          val subscriber = topic.subscribeUnbounded
            .stage("subscriber", branch = "sub")
            .fork("root", "sub")

          subscriber
            .concurrently(publisher)
            .compile
            .toList
            .compileStage("concurrently…toList")
        }
      }
  }
  @JSExport
  val multipleSubscribers = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        Topic[IO, Char].flatMap { topic =>
          val input = Stream('a', 'b')
            .delayBy[IO](1.second)
            .stage("input", branch = "pub")
          val publisher = input
            .through(topic.publish)
            .stage("publisher", branch = "pub")
            .fork("root", "pub")
          val subscriberA = topic.subscribeUnbounded
            .stage("subscriberA", branch = "subA")
            .fork("subs", "subA")
          val subscriberB = topic.subscribeUnbounded
            .stage("subscriberB", branch = "subB")
            .fork("subs", "subB")

          subscriberA.merge(subscriberB)
            .fork("root", "subs")
            .concurrently(publisher)
            .compile
            .toList
            .compileStage("compile.toList")
        }
      }
  }

  @JSExport
  val delayedSubscriber = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        Topic[IO, Char].flatMap { topic =>
          val input = Stream('a', 'b')
            .spaced[IO](2.second)
            .stage("input", branch = "pub")
          val publisher = input
            .through(topic.publish)
            .stage("publisher", branch = "pub")
            .fork("root", "pub")
          val subscriber = topic.subscribeUnbounded
            .delayBy(1.second)
            .stage("subscriber", branch = "sub")
            .fork("root", "sub")

          subscriber
            .concurrently(publisher)
            .compile
            .toList
            .compileStage("compile.toList")
        }
      }
  }


  @JSExport
  val meteredSubscriber = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        Topic[IO, Char].flatMap { topic =>
          val input = Stream('a', 'b')
            .delayBy[IO](1.second)
            .stage("input", branch = "pub")
          val publisher = input
            .through(topic.publish)
            .stage("publisher", branch = "pub")
            .fork("root", "pub")
          val subscriber = topic.subscribeUnbounded
            .spaced(2.seconds)
            .stage("subscriber", branch = "sub")
            .fork("root", "sub")

          subscriber
            .concurrently(publisher)
            .compile
            .toList
            .compileStage("compile.toList")
        }
      }
  }
  @JSExport
  val boundedSubscriber = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        Topic[IO, Char].flatMap { topic =>
          val input = Stream('a', 'b', 'c', 'd')
            .delayBy[IO](2.second)
            .stage("input", branch = "pub")
          val publisher = input
            .through(topic.publish)
            .stage("publisher", branch = "pub")
            .fork("root", "pub")
          val subscriber = topic.subscribe(0)
            .stage("subscriber", branch = "sub")
            .spaced(1.second)
            .stage("metered", branch = "sub")
            .fork("root", "sub")

          subscriber
            .concurrently(publisher)
            .compile
            .toList
            .compileStage("compile.toList")
        }
      }
  }

  @JSExport
  val boundedSubscriberBuffer = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        Topic[IO, Char].flatMap { topic =>
          val input = Stream('a', 'b', 'c', 'd')
            .delayBy[IO](2.second)
            .stage("input", branch = "pub")
          val publisher = input
            .through(topic.publish)
            .stage("publisher", branch = "pub")
            .fork("root", "pub")
          val subscriber = topic.subscribe(1)
            .stage("subscriber", branch = "sub")
            .spaced(1.second)
            .stage("spaced", branch = "sub")
            .fork("root", "sub")

          subscriber
            .concurrently(publisher)
            .compile
            .toList
            .compileStage("compile.toList")
        }
      }
  }
  @JSExport
  val multipleSubscribersBounded = new Example {
    def apply(using Scape[IO]): StreamCode =
      code {
        Topic[IO, Char].flatMap { topic =>
          val input = Stream('a', 'b')
            .delayBy[IO](1.second)
            .stage("input", branch = "pub")
          val publisher = input
            .through(topic.publish)
            .stage("publisher", branch = "pub")
            .fork("root", "pub")
          val subscriberA = topic.subscribeUnbounded
            .stage("subscriberA", branch = "subA")
            .fork("subs", "subA")
          val subscriberB = topic.subscribe(0)
            .stage("subscriberB", branch = "subB")
            .spaced(1.second)
            .stage("spaced", branch = "subB")
            .fork("subs", "subB")

          subscriberA.merge(subscriberB)
            .fork("root", "subs")
            .concurrently(publisher)
            .compile
            .toList
            .compileStage("compile.toList")
        }
      }
  }


}
